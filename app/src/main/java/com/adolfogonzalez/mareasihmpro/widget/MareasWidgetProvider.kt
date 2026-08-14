package com.adolfogonzalez.mareasihmpro.widget
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.adolfogonzalez.mareasihmpro.MainActivity
import com.adolfogonzalez.mareasihmpro.R
class MareasWidgetProvider:AppWidgetProvider(){override fun onUpdate(context:Context,manager:AppWidgetManager,ids:IntArray){val data=SurfaceCache.read(context);ids.forEach{id->val views=RemoteViews(context.packageName,R.layout.widget_mareas);views.setTextViewText(R.id.widget_station,data.station);views.setTextViewText(R.id.widget_height,"%.2f m".format(data.height));views.setTextViewText(R.id.widget_next,data.next);views.setTextViewText(R.id.widget_weather,"UV %.1f · Viento %.0f km/h".format(data.uv,data.wind));val pending=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT);views.setOnClickPendingIntent(R.id.widget_root,pending);manager.updateAppWidget(id,views)}}}
