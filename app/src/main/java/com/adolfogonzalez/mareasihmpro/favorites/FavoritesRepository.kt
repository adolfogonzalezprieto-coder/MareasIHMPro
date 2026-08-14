package com.adolfogonzalez.mareasihmpro.favorites

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.favoritesDataStore: DataStore<Preferences> by preferencesDataStore("mareas_favorites")

data class FavoriteLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val ihmStationId: String? = null,
    val isDefault: Boolean = false
)

class FavoritesRepository(private val context: Context) {
    private companion object {
        val FAVORITES = stringPreferencesKey("favorites")
        val DEFAULT_ID = stringPreferencesKey("default_id")
        val USE_GPS = booleanPreferencesKey("use_gps")
    }

    val favorites: Flow<List<FavoriteLocation>> = context.favoritesDataStore.data.map { preferences ->
        val defaultId = preferences[DEFAULT_ID]
        decode(preferences[FAVORITES] ?: "[]").map { it.copy(isDefault = it.id == defaultId) }
    }

    val useGps: Flow<Boolean> = context.favoritesDataStore.data.map { preferences ->
        preferences[USE_GPS] ?: true
    }

    suspend fun save(favorite: FavoriteLocation) {
        val list = getAll().toMutableList()
        val index = list.indexOfFirst { it.id == favorite.id }
        if (index >= 0) list[index] = favorite.copy(isDefault = false)
        else list.add(favorite.copy(isDefault = false))
        write(list)
        if (favorite.isDefault) setDefault(favorite.id)
    }

    suspend fun remove(id: String) {
        write(getAll().filterNot { it.id == id })
        context.favoritesDataStore.edit { preferences ->
            if (preferences[DEFAULT_ID] == id) preferences.remove(DEFAULT_ID)
        }
    }

    suspend fun setDefault(id: String) {
        if (getAll().none { it.id == id }) return
        context.favoritesDataStore.edit { preferences -> preferences[DEFAULT_ID] = id }
    }

    suspend fun setUseGps(value: Boolean) {
        context.favoritesDataStore.edit { preferences -> preferences[USE_GPS] = value }
    }

    suspend fun getDefault(): FavoriteLocation? {
        val preferences = context.favoritesDataStore.data.first()
        val id = preferences[DEFAULT_ID] ?: return null
        return decode(preferences[FAVORITES] ?: "[]").firstOrNull { it.id == id }?.copy(isDefault = true)
    }

    suspend fun getAll(): List<FavoriteLocation> {
        val preferences = context.favoritesDataStore.data.first()
        val defaultId = preferences[DEFAULT_ID]
        return decode(preferences[FAVORITES] ?: "[]").map { it.copy(isDefault = it.id == defaultId) }
    }

    private suspend fun write(list: List<FavoriteLocation>) {
        context.favoritesDataStore.edit { preferences -> preferences[FAVORITES] = encode(list) }
    }

    private fun encode(list: List<FavoriteLocation>): String {
        val array = JSONArray()
        list.forEach { favorite ->
            array.put(JSONObject().apply {
                put("id", favorite.id)
                put("name", favorite.name)
                put("latitude", favorite.latitude)
                put("longitude", favorite.longitude)
                if (favorite.ihmStationId != null) put("ihmStationId", favorite.ihmStationId)
            })
        }
        return array.toString()
    }

    private fun decode(json: String): List<FavoriteLocation> {
        return runCatching {
            val array = JSONArray(json)
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                FavoriteLocation(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    latitude = item.getDouble("latitude"),
                    longitude = item.getDouble("longitude"),
                    ihmStationId = if (item.has("ihmStationId") && !item.isNull("ihmStationId")) item.getString("ihmStationId") else null
                )
            }
        }.getOrDefault(emptyList())
    }
}
