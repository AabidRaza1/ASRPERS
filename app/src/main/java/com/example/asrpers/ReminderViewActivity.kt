package com.example.asrpers

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ReminderViewActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var reminderList: MutableList<String>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_view)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!

        val addButton: ImageButton = findViewById(R.id.add_button)
        addButton.setOnClickListener {
            // Start the Add Reminder activity here
            startActivity(Intent(this, ReminderActivity::class.java))
        }

        if (currentUser != null) {
            databaseReference = FirebaseDatabase.getInstance().reference.child("reminders").child(currentUser.uid)
        }

        // Initialize views
        listView = findViewById(R.id.reminders_list_view)

        // Initialize reminder list
        reminderList = mutableListOf()

        // Set up ListView
        val remindersAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, reminderList)
        listView.adapter = remindersAdapter

        // Set click listener for list items
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedReminder = listView.getItemAtPosition(position) as String
            deleteReminder(selectedReminder)
        }

        // Retrieve reminders from Firebase
        retrieveReminders()
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
                                val reminder = "Medicine: ${medReminder.medName}, Dose: ${medReminder.medDose}, Timing: ${medReminder.medTiming}"
                                reminderList.add(reminder)
                            }
                        }
                    }

                    // Check if the child node represents a doctor appointment reminder
                    if (childSnapshot.key == "doctor") {
                        // Retrieve doctor appointment data
                        for (doctorSnapshot in childSnapshot.children) {
                            val docReminder = doctorSnapshot.getValue(DocReminder::class.java)
                            if (docReminder != null) {
                                val reminder = "Doctor: ${docReminder.docName}, Location: ${docReminder.docLocation}, Timing: ${docReminder.docTiming}"
                                reminderList.add(reminder)
                            }
                        }
                    }
                }

                // Notify the adapter that the data has changed
                (listView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Toast.makeText(applicationContext, "Failed to retrieve reminders", Toast.LENGTH_SHORT).show()
            }
        })
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
                // Deletion from the database was successful
                // Apply your logic here...
                Toast.makeText(applicationContext, "Reminder deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Deletion from the database failed
                Log.e("ReminderViewActivity", "Failed to delete reminder: ${e.message}")
                Toast.makeText(applicationContext, "Failed to delete reminder", Toast.LENGTH_SHORT).show()
            }
    }

}
