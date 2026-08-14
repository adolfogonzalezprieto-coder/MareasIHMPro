package com.adolfogonzalez.mareasihmpro.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    repository: FavoritesRepository,
    onOpen: (FavoriteLocation) -> Unit
) {
    val favorites by repository.favorites.collectAsState(initial = emptyList())
    val useGps by repository.useGps.collectAsState(initial = true)
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("FAVORITOS", style = MaterialTheme.typography.headlineSmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Usar GPS al iniciar")
            Switch(
                checked = useGps,
                onCheckedChange = { enabled ->
                    scope.launch { repository.setUseGps(enabled) }
                }
            )
        }
        favorites.forEach { favorite ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(if (favorite.isDefault) "★ ${favorite.name}" else favorite.name)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onOpen(favorite) }) { Text("Abrir") }
                        TextButton(onClick = {
                            scope.launch {
                                repository.setDefault(favorite.id)
                                repository.setUseGps(false)
                            }
                        }) { Text("Principal") }
                        TextButton(onClick = { scope.launch { repository.remove(favorite.id) } }) { Text("Eliminar") }
                    }
                }
            }
        }
    }
}
