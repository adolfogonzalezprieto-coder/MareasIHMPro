package com.adolfogonzalez.mareasihmpro.data

data class Station(val id:String,val code:String,val puerto:String,val lat:String,val lon:String)
data class StationList(val puertos:List<Station>)
data class StationEnvelope(val estaciones:StationList)
data class IhmEvent(val fecha:String?=null,val hora:String,val tipo:String,val altura:Double)
data class IhmEventList(val marea:List<IhmEvent>)
data class IhmTides(val puerto:String,val fecha:String?=null,val datos:IhmEventList)
data class IhmResponse(val mareas:IhmTides)
data class WeatherCurrent(val temperature_2m:Double?,val apparent_temperature:Double?,val relative_humidity_2m:Int?,val surface_pressure:Double?,val uv_index:Double?,val wind_speed_10m:Double?,val wind_direction_10m:Double?,val wind_gusts_10m:Double?)
data class WeatherHourly(val time:List<String>?,val temperature_2m:List<Double>?,val uv_index:List<Double>?,val wind_speed_10m:List<Double>?,val wind_gusts_10m:List<Double>?)
data class WeatherResponse(val current:WeatherCurrent?,val hourly:WeatherHourly?)
data class MarineCurrent(val wave_height:Double?,val wave_period:Double?,val wave_direction:Double?,val swell_wave_height:Double?,val sea_surface_temperature:Double?)
data class MarineResponse(val current:MarineCurrent?)
data class Point(val hour:Double,val value:Double)
data class TideEvent(val time:String,val type:String,val height:Double,val offset:Double)
data class AppData(
 val station:String,val tideCurve:List<Point>,val tideEvents:List<TideEvent>,val tideHeight:Double,val rising:Boolean,
 val temperature:Double,val feels:Double,val temperatureCurve:List<Point>,val humidity:Int,val pressure:Double,
 val uv:Double,val uvCurve:List<Point>,val wind:Double,val windDirection:Double,val gusts:Double,val windCurve:List<Point>,val gustCurve:List<Point>,
 val wave:Double,val period:Double,val waveDirection:Double,val swell:Double,val seaTemperature:Double,
 val coefficient:Int,val moonLabel:String,val moonIllumination:Int
)
