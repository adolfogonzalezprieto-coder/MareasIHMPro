package com.adolfogonzalez.mareasihmpro.ui
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.adolfogonzalez.mareasihmpro.data.AppData
import com.adolfogonzalez.mareasihmpro.favorites.*
@Composable fun AppPager(data:AppData,onOpenFavorite:(FavoriteLocation)->Unit){val context=LocalContext.current;val compact=context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH);val pages=10;val state=rememberPagerState(pageCount={pages});val repository=FavoritesRepository(context.applicationContext);val current=FavoriteLocation("${data.latitude},${data.longitude}",data.station,data.latitude,data.longitude,isDefault=false);Box(Modifier.fillMaxSize()){HorizontalPager(state=state,modifier=Modifier.fillMaxSize()){page->when(page){0->Home(data,compact);1->Tides(data,compact);2->Weather(data,compact);3->Wind(data,compact);4->Sea(data,compact);5->UV(data,compact);6->Beach(data,compact);7->Fishing(data,compact);8->Moon(data,compact);else->FavoritesScreen(repository,current,onOpenFavorite)}};Row(Modifier.align(Alignment.BottomCenter).padding(bottom=if(compact)10.dp else 14.dp),horizontalArrangement=Arrangement.spacedBy(if(compact)3.dp else 6.dp)){repeat(pages){index->Box(Modifier.size(if(index==state.currentPage)9.dp else 5.dp).background(if(index==state.currentPage)Cyan else Muted,CircleShape))}}}}
