package com.adolfogonzalez.mareasihmpro
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adolfogonzalez.mareasihmpro.ui.*
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{App()}}}
@Composable fun App(vm:AppViewModel= viewModel()){val state by vm.state.collectAsState();val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){vm.load()};LaunchedEffect(Unit){permission.launch(Manifest.permission.ACCESS_FINE_LOCATION)};MaterialTheme{Surface(Modifier.fillMaxSize(),color=Bg){when(val current=state){AppState.Loading->Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){CircularProgressIndicator(color=Cyan)};is AppState.Error->Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Button(onClick=vm::load){Text(current.message)}};is AppState.Ready->AppPager(current.data){favorite->vm.loadAt(favorite.latitude,favorite.longitude)}}}}}
