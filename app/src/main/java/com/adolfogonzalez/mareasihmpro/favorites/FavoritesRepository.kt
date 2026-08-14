package com.adolfogonzalez.mareasihmpro.favorites

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.favoritesDataStore by preferencesDataStore(
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

    companion object {

        private val FAVORITES =
            stringPreferencesKey("favorites")

        private val DEFAULT_FAVORITE =
            stringPreferencesKey("default_favorite")

        private val LAST_LOCATION =
            stringPreferencesKey("last_location")

        private val USE_GPS =
            booleanPreferencesKey("use_gps")
    }

    val favorites: Flow<List<FavoriteLocation>> =
        context.favoritesDataStore.data.map { prefs ->

            val json =
                prefs[FAVORITES] ?: "[]"

            decodeFavorites(json)
        }

    val defaultFavoriteId: Flow<String?> =
        context.favoritesDataStore.data.map {
            it[DEFAULT_FAVORITE]
        }

    val lastLocationId: Flow<String?> =
        context.favoritesDataStore.data.map {
            it[LAST_LOCATION]
        }

    val useGps: Flow<Boolean> =
        context.favoritesDataStore.data.map {
            it[USE_GPS] ?: true
        }

    suspend fun addFavorite(
        favorite: FavoriteLocation
    ) {

        val current =
            getFavoritesOnce().toMutableList()

        if (current.none { it.id == favorite.id }) {
            current.add(favorite)
        }

        saveFavorites(current)
    }

    suspend fun removeFavorite(
        favoriteId: String
    ) {

        val current =
            getFavoritesOnce()
                .filterNot {
                    it.id == favoriteId
                }

        saveFavorites(current)

        context.favoritesDataStore.edit {

            if (
                it[DEFAULT_FAVORITE]
                == favoriteId
            ) {
                it.remove(DEFAULT_FAVORITE)
            }

            if (
                it[LAST_LOCATION]
                == favoriteId
            ) {
                it.remove(LAST_LOCATION)
            }
        }
    }

    suspend fun updateFavorite(
        favorite: FavoriteLocation
    ) {

        val current =
            getFavoritesOnce()
                .toMutableList()

        val index =
            current.indexOfFirst {
                it.id == favorite.id
            }

        if (index >= 0) {
            current[index] = favorite
            saveFavorites(current)
        }
    }

    suspend fun setDefaultFavorite(
        favoriteId: String
    ) {

        context.favoritesDataStore.edit {

            it[DEFAULT_FAVORITE] =
                favoriteId
        }
    }

    suspend fun setLastLocation(
        favoriteId: String
    ) {

        context.favoritesDataStore.edit {

            it[LAST_LOCATION] =
                favoriteId
        }
    }

    suspend fun setUseGps(
        enabled: Boolean
    ) {

        context.favoritesDataStore.edit {

            it[USE_GPS] =
                enabled
        }
    }

    suspend fun getDefaultFavorite(): FavoriteLocation? {

        val favoriteId =
            context.favoritesDataStore.data
                .map {
                    it[DEFAULT_FAVORITE]
                }
                .firstOrNull()

        return getFavoritesOnce()
            .firstOrNull {
                it.id == favoriteId
            }
    }

    suspend fun getFavoritesOnce():
            List<FavoriteLocation> {

        val prefs =
            context.favoritesDataStore.data
                .map {
                    it[FAVORITES] ?: "[]"
                }
                .firstOrNull()
                ?: "[]"

        return decodeFavorites(prefs)
    }

    private suspend fun saveFavorites(
        favorites: List<FavoriteLocation>
    ) {

        context.favoritesDataStore.edit {

            it[FAVORITES] =
                encodeFavorites(favorites)
        }
    }

    private fun encodeFavorites(
        favorites: List<FavoriteLocation>
    ): String {

        val array = JSONArray()

        favorites.forEach { favorite ->

            val obj = JSONObject()

            obj.put(
                "id",
                favorite.id
            )

            obj.put(
                "name",
                favorite.name
            )

            obj.put(
                "latitude",
                favorite.latitude
            )

            obj.put(
                "longitude",
                favorite.longitude
            )

            obj.put(
                "ihmStationId",
                favorite.ihmStationId
            )

            array.put(obj)
        }

        return array.toString()
    }

    private fun decodeFavorites(
        json: String
    ): List<FavoriteLocation> {

        return try {

            val array =
                JSONArray(json)

            buildList {

                for (
                    i in 0 until array.length()
                ) {

                    val item =
                        array.getJSONObject(i)

                    add(
                        FavoriteLocation(
                            id =
                                item.getString("id"),

                            name =
                                item.getString("name"),

                            latitude =
                                item.getDouble("latitude"),

                            longitude =
                                item.getDouble("longitude"),

                            ihmStationId =
                                item.optString(
                                    "ihmStationId",
                                    null
                                )
                        )
                    )
                }
            }

        } catch (
            _: Exception
        ) {
            emptyList()
        }
    }
}
