package com.example.appwidget

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class PowerConnectionReceiver : BroadcastReceiver() {

    private fun sendNotification(context: Context?) {
        if (context == null) {
            return
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)

        val builder = Notification.Builder(context, MainActivity.NOTIFY_CHANNEL_NAME)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Clock")
            .setContentText("Set alarm clock")
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        val notifyMana =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notifyMana.notify(intent.hashCode(), builder.build())
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) {
            return
        }

        if (intent.action == Intent.ACTION_POWER_CONNECTED) {

            Toast.makeText(context, "connected", Toast.LENGTH_SHORT).show()

            context?.let {
                sendNotification(context)
            }

        }
    }
}
