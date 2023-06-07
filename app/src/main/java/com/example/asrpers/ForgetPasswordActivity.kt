package com.example.asrpers

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var retrievePasswordButton: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forget_password_activity)

        emailEditText = findViewById(R.id.emailEditText)
        retrievePasswordButton = findViewById(R.id.retrievePasswordButton)

        auth = FirebaseAuth.getInstance()

        retrievePasswordButton.setOnClickListener {
            val email = emailEditText.text.toString()

            // Perform email validation here
            if (isEmailValid(email)) {
                sendPasswordResetEmail(email)
            } else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        // Add your email validation logic here
        // For simplicity, we'll use a basic pattern match
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Password reset email sent successfully
                    Toast.makeText(this, "Password reset email sent to your email", Toast.LENGTH_SHORT).show()
                    Intent(this, LoginActivity::class.java)
                    finish()
                } else {
                    // Error occurred while sending the password reset email
                    val exception = task.exception
                    Toast.makeText(this, "Failed to send password reset email: ${exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
