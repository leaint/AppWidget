package com.example.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * Implementation of App Widget functionality.
 */
class LockScreenWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent == null || context == null) {
            return
        }
        intent.run {
            if (action == MyAccessibilityService.LOCK_SCREEN_ACTION) {
                context.startService(
                    Intent(context, MyAccessibilityService::class.java).setAction(
                        MyAccessibilityService.LOCK_SCREEN_ACTION
                    )
                )
            }
            val id = getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                updateAppWidget(context, AppWidgetManager.getInstance(context), id)
            }
        }

    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
//    val widgetText = "Lock"
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.lock_screen_widget)
    views.setImageViewResource(R.id.appwidget_text, R.drawable.round_lock_48)
//    views.setTextViewText(R.id.appwidget_text, widgetText)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, LockScreenWidget::class.java).apply {
            action = MyAccessibilityService.LOCK_SCREEN_ACTION
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent)
    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}