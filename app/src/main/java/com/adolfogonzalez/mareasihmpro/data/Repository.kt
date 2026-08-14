package com.adolfogonzalez.mareasihmpro.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.*

class Repository {
    private val zone = ZoneId.of("Europe/Madrid")

    suspend fun load(lat: Double, lon: Double): AppData = coroutineScope {
        val station = Apis.ihm.stations().estaciones.puertos.minByOrNull {
            distance(lat, lon, it.lat.toDouble(), it.lon.toDouble())
        } ?: error("No se encontró estación IHM")

        val now = ZonedDateTime.now(zone).withSecond(0).withNano(0)
        val requestDate = DateTimeFormatter.ofPattern("yyyyMMdd")
        val today = async { Apis.ihm.tides(id = station.id, date = now.toLocalDate().format(requestDate)) }
        val tomorrow = async { Apis.ihm.tides(id = station.id, date = now.toLocalDate().plusDays(1).format(requestDate)) }
        val weather = async { Apis.weather.forecast(lat, lon) }
        val marine = async { Apis.marine.current(lat, lon) }

        val tideEvents = listOf(today.await().mareas, tomorrow.await().mareas)
            .flatMap { response -> response.datos.marea.mapNotNull { toEvent(it, response.fecha, now) } }
            .sortedBy { it.offset }
        val anchors = tideEvents.filter { it.offset in -12.0..36.0 }.map { it.offset to it.height }
        if (anchors.size < 4) error("Datos IHM insuficientes")
        val tideCurve = (0..96).map { index ->
            val hour = index / 4.0
            Point(hour, interpolate(anchors, hour))
        }

        val weatherResponse = weather.await()
        val hourly = weatherResponse.hourly
        val marineCurrent = marine.await().current
        val lunar = lunarInfo(now.toLocalDate())
        val height = interpolate(anchors, 0.0)

        AppData(
            latitude = lat,
            longitude = lon,
            station = "${station.puerto} (IHM ${station.id})",
            tideCurve = tideCurve,
            tideEvents = tideEvents.filter { it.offset in 0.0..24.0 },
            tideHeight = height,
            rising = interpolate(anchors, 0.1) >= height,
            temperature = weatherResponse.current?.temperature_2m ?: 0.0,
            feels = weatherResponse.current?.apparent_temperature ?: 0.0,
            temperatureCurve = hourlyCurve(hourly?.time, hourly?.temperature_2m, now),
            humidity = weatherResponse.current?.relative_humidity_2m ?: 0,
            pressure = weatherResponse.current?.surface_pressure ?: 0.0,
            uv = weatherResponse.current?.uv_index ?: 0.0,
            uvCurve = hourlyCurve(hourly?.time, hourly?.uv_index, now),
            wind = weatherResponse.current?.wind_speed_10m ?: 0.0,
            windDirection = weatherResponse.current?.wind_direction_10m ?: 0.0,
            gusts = weatherResponse.current?.wind_gusts_10m ?: 0.0,
            windCurve = hourlyCurve(hourly?.time, hourly?.wind_speed_10m, now),
            gustCurve = hourlyCurve(hourly?.time, hourly?.wind_gusts_10m, now),
            wave = marineCurrent?.wave_height ?: 0.0,
            period = marineCurrent?.wave_period ?: 0.0,
            waveDirection = marineCurrent?.wave_direction ?: 0.0,
            swell = marineCurrent?.swell_wave_height ?: 0.0,
            seaTemperature = marineCurrent?.sea_surface_temperature ?: 0.0,
            coefficient = lunar.first,
            moonLabel = lunar.second,
            moonIllumination = lunar.third
        )
    }

    private fun toEvent(raw: IhmEvent, responseDate: String?, now: ZonedDateTime): TideEvent? {
        val date = parseDate(raw.fecha ?: responseDate ?: return null) ?: return null
        val time = parseTime(raw.hora) ?: return null
        val local = LocalDateTime.of(date, time).atZone(ZoneOffset.UTC).withZoneSameInstant(zone)
        val offset = ChronoUnit.MINUTES.between(now, local).toDouble() / 60.0
        return TideEvent(local.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")), raw.tipo, raw.altura, offset)
    }

    private fun hourlyCurve(times: List<String>?, values: List<Double>?, now: ZonedDateTime): List<Point> {
        return times.orEmpty().zip(values.orEmpty()).mapNotNull { pair ->
            runCatching {
                val local = LocalDateTime.parse(pair.first).atZone(zone)
                val offset = ChronoUnit.MINUTES.between(now, local).toDouble() / 60.0
                if (offset in 0.0..24.0) Point(offset, pair.second) else null
            }.getOrNull()
        }
    }

    private fun parseDate(value: String): LocalDate? {
        val clean = value.trim().substringBefore("T").substringBefore(" ")
        return listOf("yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy/MM/dd").firstNotNullOfOrNull { format ->
            runCatching { LocalDate.parse(clean, DateTimeFormatter.ofPattern(format)) }.getOrNull()
        }
    }

    private fun parseTime(value: String): LocalTime? {
        return listOf("H:mm", "HH:mm", "H:mm:ss", "HH:mm:ss").firstNotNullOfOrNull { format ->
            runCatching { LocalTime.parse(value.trim(), DateTimeFormatter.ofPattern(format)) }.getOrNull()
        }
    }

    private fun interpolate(points: List<Pair<Double, Double>>, hour: Double): Double {
        val pair = points.zipWithNext().firstOrNull { hour >= it.first.first && hour <= it.second.first }
            ?: return if (hour < points.first().first) points.first().second else points.last().second
        val fraction = ((hour - pair.first.first) / (pair.second.first - pair.first.first)).coerceIn(0.0, 1.0)
        return pair.first.second + (pair.second.second - pair.first.second) * (1 - cos(PI * fraction)) / 2
    }

    private fun lunarInfo(date: LocalDate): Triple<Int, String, Int> {
        val reference = LocalDate.of(2024, 1, 11)
        val age = ((ChronoUnit.DAYS.between(reference, date) % 30 + 30) % 30).toDouble()
        val phase = age / 29.53058867
        val illumination = (((1 - cos(phase * 2 * PI)) / 2) * 100).roundToInt()
        val coefficient = (35 + abs(cos(phase * 2 * PI)) * 75).roundToInt()
        val label = when {
            age < 2 -> "Luna nueva"
            age < 8 -> "Creciente"
            age < 17 -> "Luna llena"
            age < 24 -> "Menguante"
            else -> "Luna nueva"
        }
        return Triple(coefficient, label, illumination)
    }

    private fun distance(a: Double, b: Double, c: Double, d: Double): Double {
        val p = Math.toRadians(c - a)
        val q = Math.toRadians(d - b)
        val value = sin(p / 2).pow(2) + cos(Math.toRadians(a)) * cos(Math.toRadians(c)) * sin(q / 2).pow(2)
        return 12742.0 * asin(sqrt(value))
    }
}
