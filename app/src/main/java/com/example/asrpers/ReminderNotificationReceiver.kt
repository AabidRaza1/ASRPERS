package com.example.asrpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
private val CHANNEL_ID = "ReminderChannel"
class ReminderNotificationReceiver : BroadcastReceiver() {

    private lateinit var notificationManager: NotificationManagerCompat


    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", -1)
        val reminderType = intent.getStringExtra("reminder_type")

        // Handle the reminder based on its type
        when (reminderType) {
            "medication" -> showMedicationReminderNotification(context, reminderId)
            "doctor_appointment" -> showDoctorAppointmentReminderNotification(context, reminderId)
        }
    }

    private fun showMedicationReminderNotification(context: Context, reminderId: Int) {
        notificationManager = NotificationManagerCompat.from(context)

        // TODO: Customize the notification as per your requirements
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_notification)
            .setContentTitle("Medicine Reminder")
            .setContentText("Time to take your medicine")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(reminderId, notificationBuilder.build())
    }

    private fun showDoctorAppointmentReminderNotification(context: Context, reminderId: Int) {
        notificationManager = NotificationManagerCompat.from(context)

        // TODO: Customize the notification as per your requirements
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_notification)
            .setContentTitle("Doctor Appointment")
            .setContentText("You have a doctor appointment")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(reminderId, notificationBuilder.build())
    }
}
