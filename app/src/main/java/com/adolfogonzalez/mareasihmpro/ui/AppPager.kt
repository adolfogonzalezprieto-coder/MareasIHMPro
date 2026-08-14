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
@Composable fun AppPager(data:AppData){val compact=LocalContext.current.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH);val pages=9;val state=rememberPagerState(pageCount={pages});Box(Modifier.fillMaxSize()){HorizontalPager(state=state,modifier=Modifier.fillMaxSize()){page->when(page){0->Home(data,compact);1->Tides(data,compact);2->Weather(data,compact);3->Wind(data,compact);4->Sea(data,compact);5->UV(data,compact);6->Beach(data,compact);7->Fishing(data,compact);else->Moon(data,compact)}};Row(Modifier.align(Alignment.BottomCenter).padding(bottom=if(compact)10.dp else 14.dp),horizontalArrangement=Arrangement.spacedBy(if(compact)4.dp else 7.dp)){repeat(pages){index->Box(Modifier.size(if(index==state.currentPage)10.dp else 5.dp).background(if(index==state.currentPage)Cyan else Muted,CircleShape))}}}}
