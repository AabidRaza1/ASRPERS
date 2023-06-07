package com.example.asrpers

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var btnContactList: CardView
    private lateinit var btnLogout: CardView
    private lateinit var btnSOS: CardView
    private lateinit var btnReminder: CardView
    private lateinit var btnMic: CardView
    private lateinit var btnMenu: CardView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btnContactList = findViewById(R.id.contact_list_button)
        btnLogout = findViewById(R.id.btn_Logout)
        btnSOS = findViewById(R.id.sos_button)
        btnReminder = findViewById(R.id.reminder_button)
        btnMic = findViewById(R.id.mic_button)
        btnMenu = findViewById(R.id.menu_button)

        // Set up click listeners for the buttons
        btnContactList.setOnClickListener {
            // Open the ContactListActivity
            val intent = Intent(this, ContactListActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            // Open the CommunicationActivity
            auth = FirebaseAuth.getInstance()
            auth.signOut()
            val sharedPreferences = getSharedPreferences("login_state", MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
            redirectToLogin()

        }

        btnSOS.setOnClickListener {
            // Perform SOS action here
            // For example, show an emergency contact number or call a service
        }

        btnReminder.setOnClickListener {
            // Open the ReminderActivity
            val intent = Intent(this,ReminderViewActivity::class.java)
            startActivity(intent)
        }

        btnMic.setOnClickListener {
            // Perform voice recognition action here
        }

        btnMenu.setOnClickListener {
            // Show menu options here
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
    private fun redirectToLogin() {

        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
