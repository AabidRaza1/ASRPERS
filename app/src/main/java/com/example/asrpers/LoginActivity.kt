package com.example.asrpers
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpText: TextView
    private lateinit var forgetPasswordText: TextView
    private lateinit var loginButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.name_field)
        passwordEditText = findViewById(R.id.password_field)
        signUpText = findViewById(R.id.sign_up_text)
        forgetPasswordText = findViewById(R.id.forget_password_text)
        loginButton = findViewById(R.id.login_button)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("login_state", MODE_PRIVATE)

        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            redirectToHome()
            return
        }
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Validate fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate email format
            if (!isValidEmail(email)) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null && user.isEmailVerified) {
                            loginSuccess(user)
                        } else {
                            Toast.makeText(this, "Email not verified. Please verify your email address.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                }
        }


        forgetPasswordText.setOnClickListener {
            // forget password screen
            Toast.makeText(this, "Forget password clicked", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ForgetPasswordActivity::class.java))
        }

        signUpText.setOnClickListener {
            // sign up screen
            startActivity(Intent(this, Signup::class.java))
        }
    }

    private fun loginSuccess(user: FirebaseUser?) {
        if (user != null) {
            if (user.isEmailVerified) {
                sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                redirectToHome()
            } else {
                Toast.makeText(this, "Email not verified. Please verify your email address.", Toast.LENGTH_SHORT).show()
                // Add code to handle email verification, e.g., sending a verification email or displaying a verification prompt.
            }
        }
    }
    private fun redirectToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun isValidEmail(email: String): Boolean {
            val pattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            return email.matches(Regex(pattern))
        }
    }

