package com.example.myapplication.social

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.user.Post
import com.example.myapplication.datamanager.user.PostWithUsername
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostActivity : AppCompatActivity() {
    private lateinit var etTitle: EditText
    private lateinit var etMessage: EditText
    private lateinit var btnPost: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private lateinit var database: AppDatabase

    private var loggedUserId: Int = -1
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_post)

        etTitle = findViewById(R.id.etTitle)
        etMessage = findViewById(R.id.etMessage)
        btnPost = findViewById(R.id.btnPost)
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
                addPost(title, message)
            } else {
                Toast.makeText(this, "Title and message cannot be empty", Toast.LENGTH_SHORT).show()
            }
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

    private fun addPost(title: String, message: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.postDAO().insertPost(Post(userId = loggedUserId, title = title, message = message))
            loadPosts()
            withContext(Dispatchers.Main) {
                etTitle.text.clear()
                etMessage.text.clear()
            }
        }
    }

    private fun deletePost(post: PostWithUsername) {
        if (!isAdmin && post.userId != loggedUserId) {
            Toast.makeText(this, "You can only delete your own posts", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            database.postDAO().deletePostById(post.postId)
            loadPosts()
        }
    }
}