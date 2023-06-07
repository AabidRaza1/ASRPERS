package com.example.asrpers
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var saveButton: Button

    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var imageRef: StorageReference
    private var profileImageUri: Uri? = null

    companion object {
        private const val REQUEST_IMAGE_UPLOAD = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profileImageView = findViewById(R.id.profileImageView)
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        saveButton = findViewById(R.id.saveButton)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        imageRef = storage.reference.child("profile_images")
        // Retrieve and display the current user data
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            retrieveUserData(userId)
        }
        profileImageView.setOnClickListener {
            showImageOptions()
        }
        saveButton.setOnClickListener {
            saveChanges()
        }
    }

    private fun retrieveUserData(userId: String) {
        val databaseRef = database.getReference("users").child(userId)
        databaseRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val name = snapshot.child("name").value.toString()
                val email = snapshot.child("email").value.toString()
                val profileImageUrl = snapshot.child("profileImage").value.toString()

                nameEditText.setText(name)
                emailEditText.setText(email)

                // Load the profile image using Glide or any other image loading library
                // For example, using Glide:
                // Glide.with(this).load(profileImageUrl).into(profileImageView)
            }
        }.addOnFailureListener { exception ->
            Log.e("EditProfileActivity", "Failed to retrieve user data: ${exception.message}")
        }
    }

    private fun showImageOptions() {
        val options = arrayOf("Upload New Image", "Remove Image", "Cancel")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Profile Image Options")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, REQUEST_IMAGE_UPLOAD)
                }
                1 -> {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    val databaseRef = database.getReference("users").child(userId!!)
                    databaseRef.child("profileImage").removeValue()
                        .addOnSuccessListener {
                            profileImageView.setImageResource(R.mipmap.add_image)
                            Toast.makeText(this, "Image Removed", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("EditProfileActivity", "Failed to remove image: ${exception.message}")
                        }
                }
                2 -> {
                    // Cancel option selected
                }
            }
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun saveChanges() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()

            val databaseRef = database.getReference("users").child(userId)
            databaseRef.child("name").setValue(name)
                .addOnSuccessListener {
                    Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Log.e("EditProfileActivity", "Failed to update name: ${exception.message}")
                }

            databaseRef.child("email").setValue(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Log.e("EditProfileActivity", "Failed to update email: ${exception.message}")
                }

            if (profileImageUri != null) {
                val imageRef = imageRef.child(userId)
                imageRef.putFile(profileImageUri!!)
                    .addOnSuccessListener { taskSnapshot ->
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            databaseRef.child("profileImage").setValue(uri.toString())
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("EditProfileActivity", "Failed to update profile image: ${exception.message}")
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("EditProfileActivity", "Failed to upload profile image: ${exception.message}")
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_UPLOAD && resultCode == Activity.RESULT_OK) {
            profileImageUri = data?.data
            profileImageView.setImageURI(profileImageUri)
        }
    }
}
