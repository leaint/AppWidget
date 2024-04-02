package com.example.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.RemoteViews
import android.widget.Toast
import java.time.LocalTime

const val PLUS_ACTION = "com.example.clock.AlarmAppWidget.PLUS_ACTION"
const val RESET_ACTION = "com.example.clock.AlarmAppWidget.RESET_ACTION"
const val SET_ACTION = "com.example.clock.AlarmAppWidget.SET_ACTION"

const val EXTRA_MINUTES = "minutes"

/**
 * Implementation of App Widget functionality.
 */
class AlarmAppWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            val currentMinutes =
                appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(EXTRA_MINUTES, 0)
            updateAppWidget(context, appWidgetManager, appWidgetId, currentMinutes)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent == null || context == null) {
            return
        }

        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return
        }

        val mgr = AppWidgetManager.getInstance(context)
        val options = mgr.getAppWidgetOptions(appWidgetId)
        var currentMinutes = options.getInt(EXTRA_MINUTES, 0)

        when (intent.action) {
            PLUS_ACTION -> {
                val minutes = intent.getIntExtra(EXTRA_MINUTES, 0)
                if (minutes > 0) {
                    currentMinutes += minutes
                }
            }

            RESET_ACTION -> {
                currentMinutes = 0
            }

            SET_ACTION -> {
                setAlarm(context, currentMinutes.toLong())
                currentMinutes = 0
            }

            else -> return
        }

        with(Bundle()) {
            putInt(EXTRA_MINUTES, currentMinutes)
            mgr.updateAppWidgetOptions(appWidgetId, this)
        }

        updateAppWidget(context, mgr, appWidgetId, currentMinutes)

    }

    private fun setAlarm(context: Context?, minutes: Long) {

        if (minutes <= 0) {
            return
        }

        val alarmTime = LocalTime.now().plusMinutes(minutes)
        val alarmIntent = Intent().apply {
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK
            action = AlarmClock.ACTION_SET_ALARM
            putExtra(AlarmClock.EXTRA_HOUR, alarmTime.hour)
            putExtra(AlarmClock.EXTRA_MINUTES, alarmTime.minute)
            putExtra(AlarmClock.EXTRA_VIBRATE, false)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        }

        try {
            context?.startActivity(alarmIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, e.message ?: "ActivityNotFoundException", Toast.LENGTH_SHORT)
                .show()
        }
    }
}


internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    currentMinutes: Int
) {
    val views = RemoteViews(context.packageName, R.layout.alarm_app_widget)
    val setPendingIntent = fun(
        act: String,
        minutes: Int?,
        id: Int
    ) {
        Intent(context, AlarmAppWidget::class.java).run {
            action = act
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            minutes?.let {
                putExtra(EXTRA_MINUTES, it)
            }
            PendingIntent.getBroadcast(
                context,
                minutes ?: 0,
                this,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }.let {
            views.setOnClickPendingIntent(id, it)
        }
    }
    val widgetText = currentMinutes.toString()
    // Construct the RemoteViews object
    views.setTextViewText(R.id.appwidget_text, widgetText)

    setPendingIntent(RESET_ACTION, null, R.id.appwidget_text)
    setPendingIntent(PLUS_ACTION, 5, R.id.button4)
    setPendingIntent(PLUS_ACTION, 10, R.id.button5)
    setPendingIntent(SET_ACTION, null, R.id.button6)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}