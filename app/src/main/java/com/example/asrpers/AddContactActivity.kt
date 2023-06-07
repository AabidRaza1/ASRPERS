@file:Suppress("DEPRECATION")

package com.example.asrpers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException
import java.util.*

class AddContactActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var mobileEditText: EditText
    private lateinit var contactImageView: ImageView
    private lateinit var saveButton: Button

    private lateinit var database: DatabaseReference
    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        database = FirebaseDatabase.getInstance().getReference("contacts")
            .child(FirebaseAuth.getInstance().currentUser?.uid.orEmpty())

        nameEditText = findViewById(R.id.nameEditText)
        mobileEditText = findViewById(R.id.mobileEditText)
        contactImageView = findViewById(R.id.contactImageView)
        saveButton = findViewById(R.id.saveButton)

        contactImageView.setOnClickListener {
            openImagePicker()
        }

        saveButton.setOnClickListener {
            saveContact()
        }
    }

    private fun openImagePicker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                contactImageView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun saveContact() {
        val name = nameEditText.text.toString().trim()
        val mobile = mobileEditText.text.toString().trim()

        if (name.isEmpty() || mobile.isEmpty()) {
            // Fields are empty, show an error toast or message
            Toast.makeText(this, "Fields are empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri != null) {
            // If an image is selected, upload it to Firebase Storage and save the contact details including the image URL to the database
            uploadImageToFirebaseStorage(selectedImageUri!!)
        } else {
            // If no image is selected, save the contact details without an image URL
            saveContactToDatabase("")
        }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference.child("contact_images")
        val imageRef = storageRef.child("${UUID.randomUUID()}.jpg")

        val uploadTask = imageRef.putFile(imageUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val imageUrl = downloadUri.toString()

                // Save the contact details including the image URL to the database
                saveContactToDatabase(imageUrl)
            } else {
                // Handle the image upload failure
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveContactToDatabase(imageUrl: String) {
        val name = nameEditText.text.toString()
        val phone = mobileEditText.text.toString()

        // Validate fields
        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val contactId = database.push().key.orEmpty()
        val contact = Contact(contactId, name, phone, imageUrl)

        database.child(contactId).setValue(contact).addOnSuccessListener {
            // Contact saved successfully
            Toast.makeText(this, "Contact saved successfully", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { e ->
            // Failed to save contact
            Toast.makeText(this, "Failed to save contact: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("AddContactActivity", "Failed to save contact: ${e.message}")
        }
    }
}
