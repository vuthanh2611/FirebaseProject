package com.example.firebaseproject

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaseproject.DataClass
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask


class UploadActivity : ComponentActivity() {

    private lateinit var uploadButton: FloatingActionButton
    private lateinit var uploadImage: ImageView
    private lateinit var uploadCaption: EditText
    private lateinit var progressBar: ProgressBar
    private var imageUri: Uri? = null
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Images")
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        uploadButton = findViewById(R.id.uploadButton)
        uploadCaption = findViewById(R.id.uploadCaption)
        uploadImage = findViewById(R.id.uploadImage)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.INVISIBLE

        val activityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUri = data?.data
                uploadImage.setImageURI(imageUri)
            } else {
                Toast.makeText(this@UploadActivity, "No Image Selected", Toast.LENGTH_SHORT).show()
            }
        }
        uploadImage.setOnClickListener {
            val photoPicker = Intent()
            photoPicker.action = Intent.ACTION_GET_CONTENT
            photoPicker.type = "image/*"
            activityResultLauncher.launch(photoPicker)
        }
//        uploadImage.setOnClickListener {
//
//            val photoPicker = Intent().apply {
//                action = Intent.ACTION_GET_CONTENT
//                type = "image/*"
//            }
//            activityResultLauncher.launch(photoPicker)
//        }

        uploadButton.setOnClickListener {
            if (imageUri != null) {
                uploadToFirebase(imageUri!!)
            } else {
                Toast.makeText(this@UploadActivity, "Please select an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadToFirebase(uri: Uri) {
        val caption = uploadCaption.text.toString()
        val imageReference = storageReference.child("${System.currentTimeMillis()}.${getFileExtension(uri)}")

        imageReference.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                imageReference.downloadUrl.addOnSuccessListener { uri ->
                    val dataClass = DataClass(uri.toString(), caption)
                    val key = databaseReference.push().key
                    key?.let {
                        databaseReference.child(it).setValue(dataClass)
                    }
                    progressBar.visibility = View.INVISIBLE
                    Toast.makeText(this@UploadActivity, "Uploaded", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@UploadActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            .addOnProgressListener { snapshot ->
                progressBar.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.INVISIBLE
                Toast.makeText(this@UploadActivity, "Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getFileExtension(fileUri: Uri): String? {
        val contentResolver: ContentResolver = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(fileUri))
    }
}