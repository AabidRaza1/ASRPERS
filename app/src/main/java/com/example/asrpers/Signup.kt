package com.example.asrpers


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase



class Signup : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        nameEditText = findViewById(R.id.nameInput)
        emailEditText = findViewById(R.id.emailInput)
        passwordEditText = findViewById(R.id.passwordInput)
        signUpButton = findViewById(R.id.signUpButton)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        signUpButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Validate fields
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                saveUserToDatabase(user, name, email)
                            } else {
                                Toast.makeText(this, "Failed to send email verification: ${verificationTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Failed to register user: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun saveUserToDatabase(user: FirebaseUser?, name: String, email: String) {
        if (user != null) {
            val userId = user.uid
            val newUser = User(name, email)
            database.child(userId).setValue(newUser)
                .addOnSuccessListener {
                    nameEditText.text.clear()
                    emailEditText.text.clear()
                    passwordEditText.text.clear()
                    Toast.makeText(this, "User registered successfully! Please check your email for verification.", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to register user: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }


}
