package com.example.appwidget

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder

class BatteryService : Service() {
    private val batteryChangedReceiver = BatteryChangedReceiver()

    override fun onCreate() {
        super.onCreate()
        registerReceiver(batteryChangedReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryChangedReceiver)
    }
}