package com.adolfogonzalez.mareasihmpro.ui
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adolfogonzalez.mareasihmpro.data.*
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
sealed interface AppState { data object Loading:AppState; data class Ready(val data:AppData):AppState; data class Error(val message:String):AppState }
class AppViewModel(app:Application):AndroidViewModel(app){private val repo=Repository();private val gps=LocationServices.getFusedLocationProviderClient(app);private val _state=MutableStateFlow<AppState>(AppState.Loading);val state:StateFlow<AppState> = _state
 fun load()=viewModelScope.launch{if(ContextCompat.checkSelfPermission(getApplication(),android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){_state.value=AppState.Error("Permiso GPS no concedido");return@launch};try{val request=CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).setDurationMillis(25000).setMaxUpdateAgeMillis(0).build();val location=gps.getCurrentLocation(request,CancellationTokenSource().token).await()?:error("GPS no disponible");_state.value=AppState.Ready(repo.load(location.latitude,location.longitude))}catch(e:Exception){_state.value=AppState.Error(e.message?:"Error al cargar")}}
}
