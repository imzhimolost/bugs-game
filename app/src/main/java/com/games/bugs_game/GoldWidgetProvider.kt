package com.games.bugs_game

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoldWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_CLICK_REFRESH = "com.games.bugs_game.ACTION_CLICK_REFRESH"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_gold)

            val intent = Intent(context, GoldWidgetProvider::class.java).apply {
                action = ACTION_CLICK_REFRESH
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                widgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)

            CoroutineScope(Dispatchers.IO).launch {
                val price = GoldServiceRetrofit.fetchGoldPrice()
                withContext(Dispatchers.Main) {
                    views.setTextViewText(R.id.tvWidgetGoldPrice, String.format("%.2f ₽", price))
                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_CLICK_REFRESH) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, GoldWidgetProvider::class.java)
            val views = RemoteViews(context.packageName, R.layout.widget_gold)

            views.setViewVisibility(R.id.tvWidgetTitle, View.GONE)
            views.setViewVisibility(R.id.tvWidgetGoldPrice, View.GONE)
            views.setViewVisibility(R.id.tvWidgetUnits, View.GONE)
            views.setViewVisibility(R.id.tvWidgetSmiley, View.VISIBLE)
            appWidgetManager.updateAppWidget(thisWidget, views)

            CoroutineScope(Dispatchers.IO).launch {
                delay(1200)
                val freshPrice = GoldServiceRetrofit.fetchGoldPrice()

                withContext(Dispatchers.Main) {
                    views.setViewVisibility(R.id.tvWidgetSmiley, View.GONE)
                    views.setViewVisibility(R.id.tvWidgetTitle, View.VISIBLE)
                    views.setViewVisibility(R.id.tvWidgetGoldPrice, View.VISIBLE)
                    views.setViewVisibility(R.id.tvWidgetUnits, View.VISIBLE)

                    views.setTextViewText(R.id.tvWidgetGoldPrice, String.format("%.2f ₽", freshPrice))
                    appWidgetManager.updateAppWidget(thisWidget, views)
                }
            }
        }
    }
}