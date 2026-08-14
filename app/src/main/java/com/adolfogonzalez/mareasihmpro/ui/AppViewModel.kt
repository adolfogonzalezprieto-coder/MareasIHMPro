package com.adolfogonzalez.mareasihmpro.ui

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adolfogonzalez.mareasihmpro.data.*
import com.adolfogonzalez.mareasihmpro.favorites.FavoritesRepository
import com.adolfogonzalez.mareasihmpro.widget.MareasWidgetProvider
import com.adolfogonzalez.mareasihmpro.widget.SurfaceCache
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AppState { data object Loading:AppState;data class Ready(val data:AppData):AppState;data class Error(val message:String):AppState }

class AppViewModel(app:Application):AndroidViewModel(app){
 private val repository=Repository();private val favorites=FavoritesRepository(app);private val gps=LocationServices.getFusedLocationProviderClient(app);private val _state=MutableStateFlow<AppState>(AppState.Loading);val state:StateFlow<AppState> = _state
 fun load()=viewModelScope.launch{try{val useGps=favorites.useGps.first();val favorite=if(useGps)null else favorites.getDefault();if(favorite!=null)loadCoordinates(favorite.latitude,favorite.longitude)else loadGps()}catch(error:Exception){_state.value=AppState.Error(error.message?:"Error al cargar")}}
 fun loadAt(latitude:Double,longitude:Double)=viewModelScope.launch{try{loadCoordinates(latitude,longitude)}catch(error:Exception){_state.value=AppState.Error(error.message?:"Error al cargar")}}
 private suspend fun loadGps(){if(ContextCompat.checkSelfPermission(getApplication(),android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){_state.value=AppState.Error("Permiso GPS no concedido");return};val request=CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).setDurationMillis(25000).setMaxUpdateAgeMillis(0).build();val location=gps.getCurrentLocation(request,CancellationTokenSource().token).await()?:error("GPS no disponible");loadCoordinates(location.latitude,location.longitude)}
 private suspend fun loadCoordinates(latitude:Double,longitude:Double){_state.value=AppState.Loading;val data=repository.load(latitude,longitude);cacheAndRefresh(data);_state.value=AppState.Ready(data)}
 private fun cacheAndRefresh(data:AppData){val next=data.tideEvents.firstOrNull()?.let{event->val label=if(event.type.lowercase().startsWith("pl"))"Pleamar" else "Bajamar";"$label ${event.time}"}?:"Sin evento próximo";SurfaceCache.save(getApplication(),data.station,data.tideHeight,next,data.uv,data.wind);val context=getApplication<Application>();val manager=AppWidgetManager.getInstance(context);val ids=manager.getAppWidgetIds(ComponentName(context,MareasWidgetProvider::class.java));MareasWidgetProvider().onUpdate(context,manager,ids)}
}
