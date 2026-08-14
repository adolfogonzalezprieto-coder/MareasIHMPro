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

private val Context.favoritesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mareas_favorites"
)

data class FavoriteLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val ihmStationId: String? = null,
    val isDefault: Boolean = false
)

class FavoritesRepository(
    private val context: Context
) {

    private companion object {
        val FAVORITES = stringPreferencesKey("favorites")
        val DEFAULT_FAVORITE = stringPreferencesKey("default_favorite")
        val LAST_LOCATION = stringPreferencesKey("last_location")
        val USE_GPS = booleanPreferencesKey("use_gps")
    }

    val favorites: Flow<List<FavoriteLocation>> =
        context.favoritesDataStore.data.map { preferences ->
            val defaultId = preferences[DEFAULT_FAVORITE]
            val json = preferences[FAVORITES] ?: "[]"

            decodeFavorites(json).map { favorite ->
                favorite.copy(isDefault = favorite.id == defaultId)
            }
        }

    val defaultFavoriteId: Flow<String?> =
        context.favoritesDataStore.data.map { preferences ->
            preferences[DEFAULT_FAVORITE]
        }

    val lastLocationId: Flow<String?> =
        context.favoritesDataStore.data.map { preferences ->
            preferences[LAST_LOCATION]
        }

    val useGps: Flow<Boolean> =
        context.favoritesDataStore.data.map { preferences ->
            preferences[USE_GPS] ?: true
        }

    suspend fun addFavorite(favorite: FavoriteLocation) {
        val current = getFavoritesOnce().toMutableList()
        val existingIndex = current.indexOfFirst { item ->
            item.id == favorite.id
        }

        if (existingIndex >= 0) {
            current[existingIndex] = favorite.copy(isDefault = false)
        } else {
            current.add(favorite.copy(isDefault = false))
        }

        saveFavorites(current)

        if (favorite.isDefault) {
            setDefaultFavorite(favorite.id)
        }
    }

    suspend fun removeFavorite(favoriteId: String) {
        val current = getFavoritesOnce().filterNot { favorite ->
            favorite.id == favoriteId
        }

        saveFavorites(current)

        context.favoritesDataStore.edit { preferences ->
            if (preferences[DEFAULT_FAVORITE] == favoriteId) {
                preferences.remove(DEFAULT_FAVORITE)
            }

            if (preferences[LAST_LOCATION] == favoriteId) {
                preferences.remove(LAST_LOCATION)
            }
        }
    }

    suspend fun updateFavorite(favorite: FavoriteLocation) {
        val current = getFavoritesOnce().toMutableList()
        val index = current.indexOfFirst { item ->
            item.id == favorite.id
        }

        if (index >= 0) {
            current[index] = favorite.copy(isDefault = false)
            saveFavorites(current)

            if (favorite.isDefault) {
                setDefaultFavorite(favorite.id)
            }
        }
    }

    suspend fun setDefaultFavorite(favoriteId: String) {
        val favoriteExists = getFavoritesOnce().any { favorite ->
            favorite.id == favoriteId
        }

        if (!favoriteExists) {
            return
        }

        context.favoritesDataStore.edit { preferences ->
            preferences[DEFAULT_FAVORITE] = favoriteId
        }
    }

    suspend fun clearDefaultFavorite() {
        context.favoritesDataStore.edit { preferences ->
            preferences.remove(DEFAULT_FAVORITE)
        }
    }

    suspend fun setLastLocation(favoriteId: String) {
        context.favoritesDataStore.edit { preferences ->
            preferences[LAST_LOCATION] = favoriteId
        }
    }

    suspend fun clearLastLocation() {
        context.favoritesDataStore.edit { preferences ->
            preferences.remove(LAST_LOCATION)
        }
    }

    suspend fun setUseGps(enabled: Boolean) {
        context.favoritesDataStore.edit { preferences ->
            preferences[USE_GPS] = enabled
        }
    }

    suspend fun getDefaultFavorite(): FavoriteLocation? {
        val preferences = context.favoritesDataStore.data.first()
        val favoriteId = preferences[DEFAULT_FAVORITE] ?: return null

        return decodeFavorites(preferences[FAVORITES] ?: "[]")
            .firstOrNull { favorite ->
                favorite.id == favoriteId
            }
            ?.copy(isDefault = true)
    }

    suspend fun getLastLocation(): FavoriteLocation? {
        val preferences = context.favoritesDataStore.data.first()
        val favoriteId = preferences[LAST_LOCATION] ?: return null
        val defaultId = preferences[DEFAULT_FAVORITE]

        return decodeFavorites(preferences[FAVORITES] ?: "[]")
            .firstOrNull { favorite ->
                favorite.id == favoriteId
            }
            ?.copy(isDefault = favoriteId == defaultId)
    }

    suspend fun getFavoritesOnce(): List<FavoriteLocation> {
        val preferences = context.favoritesDataStore.data.first()
        val json = preferences[FAVORITES] ?: "[]"
        val defaultId = preferences[DEFAULT_FAVORITE]

        return decodeFavorites(json).map { favorite ->
            favorite.copy(isDefault = favorite.id == defaultId)
        }
    }

    suspend fun clearAllFavorites() {
        context.favoritesDataStore.edit { preferences ->
            preferences.remove(FAVORITES)
            preferences.remove(DEFAULT_FAVORITE)
            preferences.remove(LAST_LOCATION)
        }
    }

    private suspend fun saveFavorites(favorites: List<FavoriteLocation>) {
        val normalizedFavorites = favorites.map { favorite ->
            favorite.copy(isDefault = false)
        }

        context.favoritesDataStore.edit { preferences ->
            preferences[FAVORITES] = encodeFavorites(normalizedFavorites)
        }
    }

    private fun encodeFavorites(favorites: List<FavoriteLocation>): String {
        val array = JSONArray()

        favorites.forEach { favorite ->
            val item = JSONObject()
            item.put("id", favorite.id)
            item.put("name", favorite.name)
            item.put("latitude", favorite.latitude)
            item.put("longitude", favorite.longitude)

            if (favorite.ihmStationId != null) {
                item.put("ihmStationId", favorite.ihmStationId)
            }

            array.put(item)
        }

        return array.toString()
    }

    private fun decodeFavorites(json: String): List<FavoriteLocation> {
        return try {
            val array = JSONArray(json)
            val result = mutableListOf<FavoriteLocation>()

            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val stationId = if (item.has("ihmStationId") && !item.isNull("ihmStationId")) {
                    item.getString("ihmStationId")
                } else {
                    null
                }

                result.add(
                    FavoriteLocation(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        latitude = item.getDouble("latitude"),
                        longitude = item.getDouble("longitude"),
                        ihmStationId = stationId,
                        isDefault = false
                    )
                )
            }

            result
        } catch (_: Exception) {
            emptyList()
        }
    }
}
