package com.games.bugs_game

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class GoldWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_CLICK = "com.games.bugs_game.ACTION_WIDGET_CLICK"
        private const val PREFS_NAME = "GoldWidgetPrefs"
        private const val KEY_LAST_PRICE = "last_known_gold_price"

        private var lastClickTime: Long = 0
        private val clickHandler = Handler(Looper.getMainLooper())
        private var pendingSingleClickRunnable: Runnable? = null
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_gold)

            val intent = Intent(context, GoldWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_CLICK
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                widgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)

            val savedPrice = getSavedPrice(context)
            views.setTextViewText(R.id.tvWidgetGoldPrice, String.format("%.2f ₽", savedPrice))
            appWidgetManager.updateAppWidget(widgetId, views)

            CoroutineScope(Dispatchers.IO).launch {
                val freshPrice = GoldServiceRetrofit.fetchGoldPrice()
                savePrice(context, freshPrice)
                withContext(Dispatchers.Main) {
                    showDataOnWidget(context, freshPrice)
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_WIDGET_CLICK) {
            val currentTime = System.currentTimeMillis()
            val timeDiff = currentTime - lastClickTime

            if (timeDiff < 400) {
                pendingSingleClickRunnable?.let { clickHandler.removeCallbacks(it) }
                pendingSingleClickRunnable = null
                lastClickTime = 0

                handleDoubleTap(context)

            } else {
                lastClickTime = currentTime

                val runnable = Runnable {
                    launchApp(context)
                }
                pendingSingleClickRunnable = runnable
                clickHandler.postDelayed(runnable, 400)
            }
        }
    }

    private fun launchApp(context: Context) {
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(appIntent)
    }

    private fun handleDoubleTap(context: Context) {
        val lastKnownPrice = getSavedPrice(context)

        showSmileyOnWidget(context)

        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)

            val freshPrice = withTimeoutOrNull(5000L) {
                GoldServiceRetrofit.fetchGoldPrice()
            }

            withContext(Dispatchers.Main) {
                if (freshPrice != null && freshPrice > 0f) {
                    savePrice(context, freshPrice)
                    showDataOnWidget(context, freshPrice)
                } else {
                    showDataOnWidget(context, lastKnownPrice)
                    Toast.makeText(context, "ЦБ не ответил. Возвращен прежний курс!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showSmileyOnWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, GoldWidgetProvider::class.java)
        val views = RemoteViews(context.packageName, R.layout.widget_gold)

        views.setViewVisibility(R.id.tvWidgetTitle, View.GONE)
        views.setViewVisibility(R.id.tvWidgetGoldPrice, View.GONE)
        views.setViewVisibility(R.id.tvWidgetUnits, View.GONE)
        views.setViewVisibility(R.id.tvWidgetSmiley, View.VISIBLE)

        appWidgetManager.updateAppWidget(thisWidget, views)
    }

    private fun showDataOnWidget(context: Context, price: Float) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, GoldWidgetProvider::class.java)
        val views = RemoteViews(context.packageName, R.layout.widget_gold)

        views.setViewVisibility(R.id.tvWidgetSmiley, View.GONE)
        views.setViewVisibility(R.id.tvWidgetTitle, View.VISIBLE)
        views.setViewVisibility(R.id.tvWidgetGoldPrice, View.VISIBLE)
        views.setViewVisibility(R.id.tvWidgetUnits, View.VISIBLE)

        views.setTextViewText(R.id.tvWidgetGoldPrice, String.format("%.2f ₽", price))
        appWidgetManager.updateAppWidget(thisWidget, views)
    }

    private fun savePrice(context: Context, price: Float) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_LAST_PRICE, price).apply()
    }

    private fun getSavedPrice(context: Context): Float {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_LAST_PRICE, GoldServiceRetrofit.cachedGoldPrice)
    }
}