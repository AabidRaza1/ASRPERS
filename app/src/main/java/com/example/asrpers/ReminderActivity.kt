package com.example.asrpers
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

import java.util.*

class ReminderActivity : AppCompatActivity() {
    private lateinit var medNameET: EditText
    private lateinit var medDoseET: EditText
    private lateinit var medTimingET: EditText

    private lateinit var docNameET: EditText
    private lateinit var docLocationET: EditText
    private lateinit var docTimingET: EditText

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("reminders")

        // Initialize views
        medNameET = findViewById(R.id.et_med_name)
        medDoseET = findViewById(R.id.et_med_dose)
        medTimingET = findViewById(R.id.et_med_timing)

        docNameET = findViewById(R.id.et_doc_name)
        docLocationET = findViewById(R.id.et_doc_location)
        docTimingET = findViewById(R.id.et_doc_timing)

        // Set click listener for save buttons
        val saveButtonMed: Button = findViewById(R.id.save_med)
        saveButtonMed.setOnClickListener {
            saveReminderMedData()
        }

        val saveButtonDoc: Button = findViewById(R.id.save_doc)
        saveButtonDoc.setOnClickListener {
            saveReminderDocData()
        }
    }
    private fun saveReminderMedData() {
        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        val medName = medNameET.text.toString().trim()
        val medDose = medDoseET.text.toString().trim()
        val medTiming = medTimingET.text.toString().trim()

        if (medName.isNotEmpty() && medDose.isNotEmpty() && medTiming.isNotEmpty()) {
            // Save medicine data to Firebase Realtime Database
            currentUser?.let {
                val userId = it.uid
                val medReminder = MedReminder("", medName, medDose, medTiming) // Set medid as empty string

                // Generate a unique key for the new medicine reminder
                val medReminderKey = databaseReference.child(userId).child("medicine").push().key

                // Store the medicine reminder under the generated key
                val medReminderRef = databaseReference.child(userId).child("medicine").child(medReminderKey ?: "")
                medReminderRef.setValue(medReminder)
                    .addOnSuccessListener {
                        // Set the medid with the generated key after successful save
                        medReminderRef.child("medid").setValue(medReminderKey)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Medicine reminder data saved successfully", Toast.LENGTH_SHORT).show()
                                clearMedFields()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save medicine reminder data", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save medicine reminder data", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "Please fill all medicine fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveReminderDocData() {
        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        val docName = docNameET.text.toString().trim()
        val docLocation = docLocationET.text.toString().trim()
        val docTiming = docTimingET.text.toString().trim()

        if (docName.isNotEmpty() && docLocation.isNotEmpty() && docTiming.isNotEmpty()) {
            // Save doctor appointment data to Firebase Realtime Database
            currentUser?.let {
                val userId = it.uid
                val docReminder = DocReminder("", docName, docLocation, docTiming) // Set docid as empty string

                // Generate a unique key for the new doctor appointment reminder
                val docReminderKey = databaseReference.child(userId).child("doctor").push().key

                // Store the doctor appointment reminder under the generated key
                val docReminderRef = databaseReference.child(userId).child("doctor").child(docReminderKey ?: "")
                docReminderRef.setValue(docReminder)
                    .addOnSuccessListener {
                        // Set the docid with the generated key after successful save
                        docReminderRef.child("docid").setValue(docReminderKey)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Doctor appointment data saved successfully", Toast.LENGTH_SHORT).show()
                                clearDocFields()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save doctor appointment data", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save doctor appointment data", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "Please fill all doctor appointment fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearMedFields() {
        medNameET.text.clear()
        medDoseET.text.clear()
        medTimingET.text.clear()
    }

    private fun clearDocFields() {
        docNameET.text.clear()
        docLocationET.text.clear()
        docTimingET.text.clear()
    }

    fun showTimePickerDialog(view: View) {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                val time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                when (view.id) {
                    R.id.et_med_timing -> medTimingET.setText(time)
                    R.id.et_doc_timing -> docTimingET.setText(time)
                }
            }, hour, minute, DateFormat.is24HourFormat(this)
        )

        timePicker.show()
    }
}

data class DocReminder(
    val docid:String="",
    val docName: String = "",
    val docLocation: String = "",
    val docTiming: String = ""
)

data class MedReminder(
    val medid:String="",
    val medName: String = "",
    val medDose: String = "",
    val medTiming: String = ""
)


