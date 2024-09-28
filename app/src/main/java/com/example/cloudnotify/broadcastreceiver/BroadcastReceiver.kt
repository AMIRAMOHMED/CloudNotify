package com.example.cloudnotify.broadcastreceiver

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.cloudnotify.R
import com.example.cloudnotify.receivers.AlarmServices
import com.example.cloudnotify.ui.activity.MainActivity

class BroadcastReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("AlarmReceiver", "Alarm Received with Action: ${intent?.action}")

        when (intent?.action) {
            "Alarm" -> {
                val title = intent.getStringExtra("ALARM_TITLE") ?: "Alarm"

                Log.d("BroadcastReceiver", "Received Title: $title")

                // Start the overlay service for alarm
                val serviceIntent = Intent(context, AlarmServices::class.java)
                serviceIntent.putExtra("TITLE", title)
                context?.startService(serviceIntent)
                Log.d("AlarmReceiver", "Alarm Triggered with Title: $title")
            }
            "Notification" -> {
                val title = intent.getStringExtra("ALARM_TITLE") ?: "Notification"
                // Show a notification for the scheduled time
                showNotification(context, title)
                Log.d("AlarmReceiver", "Notification Triggered with Title: $title")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification(context: Context?, title: String) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = Notification.Builder(context, "channel_id")
            .setContentTitle(title)
            .setContentText("Check Current Weather Now!")
            .setSmallIcon(R.drawable.alarm)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }
}
