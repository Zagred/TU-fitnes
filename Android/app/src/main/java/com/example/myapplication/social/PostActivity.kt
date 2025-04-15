package com.example.myapplication.social

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.HomePage
import com.example.myapplication.R
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.user.Post
import com.example.myapplication.datamanager.user.PostWithUsername
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostActivity : AppCompatActivity() {
    private lateinit var etTitle: EditText
    private lateinit var etMessage: EditText
    private lateinit var btnPost: Button
    private lateinit var btnTakePhoto: Button
    private lateinit var ivImagePreview: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private lateinit var database: AppDatabase

    private var loggedUserId: Int = -1
    private var isAdmin: Boolean = false
    private var currentPhotoPath: String? = null

    companion object {
        private const val PERMISSION_REQUEST_CAMERA = 100
    }

    // Result launcher for camera
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Image captured successfully, display it in preview
                currentPhotoPath?.let { path ->
                    ivImagePreview.setImageURI(Uri.fromFile(File(path)))
                    ivImagePreview.visibility = ImageView.VISIBLE
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_post)

        etTitle = findViewById(R.id.etTitle)
        etMessage = findViewById(R.id.etMessage)
        btnPost = findViewById(R.id.btnPost)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        ivImagePreview = findViewById(R.id.ivImagePreview)
        recyclerView = findViewById(R.id.rvPosts)

        database = AppDatabase.getInstance(application)

        loggedUserId = intent.getIntExtra("USER_ID", -1)
        if (loggedUserId == -1) {
            Toast.makeText(this, "Invalid User", Toast.LENGTH_SHORT).show()
            finish()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val user = database.userDAO().getUserById(loggedUserId)
            isAdmin = user?.role == "admin"

            withContext(Dispatchers.Main) {
                // Initialize adapter after determining admin status
                adapter = PostAdapter(loggedUserId, isAdmin) { post -> deletePost(post) }
                recyclerView.layoutManager = LinearLayoutManager(this@PostActivity)
                recyclerView.adapter = adapter

                loadPosts()
            }
        }

        btnPost.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val message = etMessage.text.toString().trim()
            if (title.isNotEmpty() && message.isNotEmpty()) {
                addPost(title, message, currentPhotoPath)
            } else {
                Toast.makeText(this, "Title and message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        btnTakePhoto.setOnClickListener {
            if (checkCameraPermission()) {
                dispatchTakePictureIntent()
            } else {
                requestCameraPermission()
            }
        }

        val home = findViewById<Button>(R.id.btHome)
        home.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            intent.putExtra("USER_ID", loggedUserId)
            startActivity(intent)
            finish()
        }
    }

    private fun loadPosts() {
        lifecycleScope.launch(Dispatchers.IO) {
            val postsList = database.postDAO().getAllPostsWithUsername()
            withContext(Dispatchers.Main) {
                adapter.setData(postsList)
            }
        }
    }

    private fun addPost(title: String, message: String, imagePath: String? = null) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.postDAO().insertPost(
                Post(
                    userId = loggedUserId,
                    title = title,
                    message = message,
                    imagePath = imagePath
                )
            )
            loadPosts()
            withContext(Dispatchers.Main) {
                etTitle.text.clear()
                etMessage.text.clear()
                ivImagePreview.visibility = ImageView.GONE
                currentPhotoPath = null
                Toast.makeText(this@PostActivity, "Post created successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deletePost(post: PostWithUsername) {
        if (!isAdmin && post.userId != loggedUserId) {
            Toast.makeText(this, "You can only delete your own posts", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            // Delete the associated image file if it exists
            post.imagePath?.let { path ->
                val imageFile = File(path)
                if (imageFile.exists()) {
                    imageFile.delete()
                }
            }
            database.postDAO().deletePostById(post.postId)
            loadPosts()
        }
    }

    // Check camera permission
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request camera permission
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CAMERA
        )
    }

    // Handle permission results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Create a file for storing the camera image
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {
            // Create the File where the photo should go
            val photoFile = createImageFile()

            // Create content URI
            val photoURI = FileProvider.getUriForFile(
                this,
                "com.example.myapplication.fileprovider",
                photoFile
            )

            // Add output URI to the intent
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

            // Launch camera activity
            cameraLauncher.launch(takePictureIntent)

        } catch (ex: Exception) {
            // Handle any exception
            Toast.makeText(
                this,
                "Camera error: ${ex.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
            ex.printStackTrace()
        }
    }
}