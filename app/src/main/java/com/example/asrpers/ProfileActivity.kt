package com.example.asrpers
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

data class User(
    var name: String? = null,
    var email: String? = null,
    var profileImage: String? = null
)
class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var generalSettingsButton: Button

    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImageView = findViewById(R.id.profileImageView)
        nameTextView = findViewById(R.id.nameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        editProfileButton = findViewById(R.id.editProfileButton)
        generalSettingsButton = findViewById(R.id.generalSettingsButton)

        database = FirebaseDatabase.getInstance().getReference("users")
        storage = FirebaseStorage.getInstance().reference

        // Retrieve user data from the database
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            retrieveUserData(userId)
        }

        // Set click listeners for buttons
        editProfileButton.setOnClickListener {
            // Open the EditProfileActivity
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        generalSettingsButton.setOnClickListener {
            // Open the GeneralSettingsActivity
            val intent = Intent(this, GeneralSettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun retrieveUserData(userId: String) {
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        // User data retrieved successfully
                        // Update the UI with the retrieved user data
                        nameTextView.text = user.name
                        emailTextView.text = user.email

                        // Retrieve and set the profile image from Firebase Storage
                        val profileImageRef = storage.child("profile_images").child(userId)
                        profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                            // Load the image into the ImageView using your preferred image loading library
                            // For example, you can use Glide or Picasso
                            // Here, we assume you are using Glide
                            Glide.with(applicationContext)
                                .load(uri)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(profileImageView)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error case when data retrieval is canceled or fails
                Log.d("ProfileActivity", "Failed to retrieve user data: ${databaseError.message}")
            }
        })
    }
}
