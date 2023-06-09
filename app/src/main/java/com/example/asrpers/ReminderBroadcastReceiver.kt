package com.example.asrpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat




class ReminderBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "REMINDER_CHANNEL"
        const val NOTIFICATION_ID = 123
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderType = intent.getStringExtra("reminderType")
        val reminderDescription = intent.getStringExtra("reminderDescription")
        // Set notification title and description based on the reminder type and data
        val title: String
        val description: String

        if (reminderType == "medicine") {
            title = "Medicine Reminder"
            description = reminderDescription ?: ""
        } else {
            title = "Doctor Reminder"
            description = reminderDescription ?: ""
        }
        // Create the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_notification)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Create the notification channel (required for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reminder Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Show the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}
