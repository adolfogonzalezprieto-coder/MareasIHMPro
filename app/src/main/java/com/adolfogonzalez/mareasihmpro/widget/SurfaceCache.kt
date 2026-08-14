package com.adolfogonzalez.mareasihmpro.widget

import android.content.Context

object SurfaceCache {
    private const val FILE = "mareas_surface_cache"

    fun save(context: Context, station: String, height: Double, nextLabel: String, uv: Double, wind: Double) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putString("station", station)
            .putFloat("height", height.toFloat())
            .putString("next", nextLabel)
            .putFloat("uv", uv.toFloat())
            .putFloat("wind", wind.toFloat())
            .apply()
    }

    fun read(context: Context): Snapshot {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        return Snapshot(
            station = prefs.getString("station", "Mareas IHM Pro") ?: "Mareas IHM Pro",
            height = prefs.getFloat("height", 0f).toDouble(),
            next = prefs.getString("next", "Abre la app para actualizar") ?: "Abre la app para actualizar",
            uv = prefs.getFloat("uv", 0f).toDouble(),
            wind = prefs.getFloat("wind", 0f).toDouble()
        )
    }
}

data class Snapshot(val station: String, val height: Double, val next: String, val uv: Double, val wind: Double)
