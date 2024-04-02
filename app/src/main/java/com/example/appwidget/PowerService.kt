package com.example.appwidget

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder

class PowerService : Service() {

    private val myReceiver = PowerConnectionReceiver()
    override fun onCreate() {
        super.onCreate()
        registerReceiver(myReceiver, IntentFilter(Intent.ACTION_POWER_CONNECTED))
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myReceiver)
    }
}