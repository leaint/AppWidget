package com.example.appwidget

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log

class BatteryChangedReceiver : BroadcastReceiver() {

    private var lastPercent = -1

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("update", "BatteryChangedReceiver")

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

        val batteryPercent = (level / scale.toFloat() * 100).toInt()

        if (batteryPercent == lastPercent) return

        lastPercent = batteryPercent

        val intent = Intent(context, BatteryWidget::class.java).apply {
            action = "android.appwidget.action.APPWIDGET_UPDATE"
            putExtra("percent", batteryPercent)
        }

        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
            .send()
    }

    companion object {
        val instanse  = BatteryChangedReceiver()
    }
}