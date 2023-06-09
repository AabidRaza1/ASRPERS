package com.example.asrpers

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*


class ReminderViewActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var reminderList: MutableList<String>
    private lateinit var addButton: ImageButton
    private lateinit var databaseReference: DatabaseReference
    private lateinit var currentUser: FirebaseUser

    companion object {
        private const val CHANNEL_ID = "REMINDER_CHANNEL"

    }
    private var notificationIdCounter = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_view)
        addButton = findViewById(R.id.add_button)
        listView = findViewById(R.id.reminders_list_view)
        reminderList = mutableListOf()

        // Initialize Firebase

        val firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!
        val database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("reminders").child(currentUser.uid)

        // Request the necessary permissions if not already granted
        requestPermissions()

        // Set up the list view adapter
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, reminderList)
        listView.adapter = adapter

        // Set up item click listener to delete reminders
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedReminder = adapter.getItem(position).toString()
                deleteReminder(selectedReminder)
                (listView.adapter as ArrayAdapter<*>).notifyDataSetChanged()

            }

        // Retrieve reminders from the database
        retrieveReminders()

        addButton = findViewById(R.id.add_button)
        addButton.setOnClickListener {
            // Start the ReminderActivity
            val intent = Intent(this, ReminderActivity::class.java)
            startActivity(intent)
        }
    }

    private fun requestPermissions() {
        val permission = Manifest.permission.VIBRATE
        val granted = PackageManager.PERMISSION_GRANTED
        if (ContextCompat.checkSelfPermission(this, permission) != granted) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 0)
        }
    }

    private fun retrieveReminders() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Clear the existing list
                reminderList.clear()

                // Iterate through each child node
                for (childSnapshot in dataSnapshot.children) {
                    // Check if the child node represents a medicine reminder
                    if (childSnapshot.key == "medicine") {
                        // Retrieve medicine data
                        for (medicineSnapshot in childSnapshot.children) {
                            val medReminder = medicineSnapshot.getValue(MedReminder::class.java)
                            if (medReminder != null) {
                                val reminder =
                                    "Medicine: ${medReminder.medName}, Dose: ${medReminder.medDose}, Timing: ${medReminder.medTiming}"
                                reminderList.add(reminder)

                                // Schedule reminder if the timing is valid
                                if (isValidTiming(medReminder.medTiming)) {
                                    scheduleReminder(
                                        "medicine",
                                        reminder
                                    )
                                }
                            }
                        }
                    }

                    // Check if the child node represents a doctor appointment reminder
                    if (childSnapshot.key == "doctor") {
                        // Retrieve doctor appointment data
                        for (doctorSnapshot in childSnapshot.children) {
                            val docReminder = doctorSnapshot.getValue(DocReminder::class.java)
                            if (docReminder != null) {
                                val reminder =
                                    "Doctor: ${docReminder.docName}, Location: ${docReminder.docLocation}, Timing: ${docReminder.docTiming}"
                                reminderList.add(reminder)

                                // Schedule reminder if the timing is valid
                                if (isValidTiming(docReminder.docTiming)) {
                                    scheduleReminder(
                                        "doctor",
                                        reminder
                                    )
                                }
                            }
                        }
                    }
                }

                // Notify the adapter that the data has changed
                (listView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Toast.makeText(
                    applicationContext,
                    "Failed to retrieve reminders",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun isValidTiming(timing: String): Boolean {
        val currentTime = Calendar.getInstance()
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timingDate = formatter.parse(timing)

        // Set the current time to the desired hour and minute
        val desiredTime = Calendar.getInstance().apply {
            time = currentTime.time
            set(Calendar.HOUR_OF_DAY, timingDate?.hours ?: 0)
            set(Calendar.MINUTE, timingDate?.minutes ?: 0)
            set(Calendar.SECOND, 0)
        }

        return currentTime.before(desiredTime)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun scheduleReminder(reminderType: String, reminderDescription: String) {
        val intent = Intent(applicationContext, ReminderBroadcastReceiver::class.java).apply {
            putExtra("reminderType", reminderType)
            putExtra("reminderDescription", reminderDescription)
        }
        val notificationId = notificationIdCounter++
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timingDate = formatter.parse(reminderDescription.substringAfterLast("Timing: ").trim())

        if (timingDate != null) {
            calendar.time = timingDate

            // Set up the notification for the specified timing
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager


            // Calculate the time differences for notification intervals
            val timeDiff = calendar.timeInMillis - System.currentTimeMillis()
            val oneHourBefore = timeDiff - AlarmManager.INTERVAL_HOUR
            val halfHourBefore = timeDiff - AlarmManager.INTERVAL_HALF_HOUR

            // Schedule notification at 1 hour before the reminder time
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + oneHourBefore,
                pendingIntent
            )

            // Schedule repeated notifications every 5 minutes starting from half an hour before the reminder time
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + halfHourBefore,
                    pendingIntent
                )
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + halfHourBefore,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    pendingIntent
                )
            }

            // Create the notification channel (required for Android 8.0 and above)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Reminder Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

        }
    }
    private fun deleteReminder(selectedReminder: String) {
        // Determine the reminder type (medicine or doctor)
        val reminderType = if (selectedReminder.contains("Medicine")) "medicine" else "doctor"

        // Get the reference to the reminders node
        val remindersRef = FirebaseDatabase.getInstance().reference.child("reminders")

        // Print reminder type and selected reminder for debugging
        Log.d("ReminderViewActivity", "Reminder Type: $reminderType")
        Log.d("ReminderViewActivity", "Selected Reminder: $selectedReminder")

        // Remove the reminder from the Realtime Database
        val reminderRef = remindersRef.child(currentUser.uid).child(reminderType).child(selectedReminder)
        Log.d("ReminderViewActivity", "reminderRef : $reminderRef")
        reminderRef.removeValue()
            .addOnSuccessListener {

                Toast.makeText(applicationContext, "Reminder deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->

                Log.e("ReminderViewActivity", "Failed to delete reminder: ${e.message}")
                Toast.makeText(applicationContext, "Failed to delete reminder", Toast.LENGTH_SHORT).show()
            }
    }




}
