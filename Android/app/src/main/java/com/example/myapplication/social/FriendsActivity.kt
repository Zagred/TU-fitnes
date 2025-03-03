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
import com.example.myapplication.datamanager.user.Friends
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendsActivity : AppCompatActivity() {
    private lateinit var btnAddFriend: Button
    private lateinit var btnClear: Button
    private lateinit var etFriendUsername: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendsAdapter
    private lateinit var database: AppDatabase

    private var loggedUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_friends)

        btnAddFriend = findViewById(R.id.btnAdd)
        btnClear = findViewById(R.id.btnClear)
        etFriendUsername = findViewById(R.id.etName)
        recyclerView = findViewById(R.id.rvFriends)

        database = AppDatabase.getInstance(application)

        loggedUserId = intent.getIntExtra("USER_ID", -1)
        if (loggedUserId == -1) {
            Toast.makeText(this, "Invalid User", Toast.LENGTH_SHORT).show()
            finish()
        }

        adapter = FriendsAdapter { friend -> deleteFriend(friend) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadFriends()

        btnAddFriend.setOnClickListener {
            val friendUsername = etFriendUsername.text.toString().trim()
            if (friendUsername.isNotEmpty()) {
                addFriend(friendUsername)
            } else {
                Toast.makeText(this, "Enter a username", Toast.LENGTH_SHORT).show()
            }
        }

        btnClear.setOnClickListener {
            etFriendUsername.setText("")
        }
    }

    private fun loadFriends() {
        lifecycleScope.launch(Dispatchers.IO) {
            val friendsList = database.friendsDAO().getFriendsForUser(loggedUserId)
            withContext(Dispatchers.Main) {
                adapter.setData(friendsList)
            }
        }
    }

    private fun addFriend(friendUsername: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val friendUser = database.userDAO().findByUsername(friendUsername)

            if (friendUser == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FriendsActivity, "User not found", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            if (friendUser.uid == loggedUserId) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FriendsActivity, "You cannot add yourself", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val exists = database.friendsDAO().friendshipExists(loggedUserId, friendUser.uid)
            if (exists > 0) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FriendsActivity, "Already added", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            database.friendsDAO().insert(Friends(id = loggedUserId, friendsId = friendUser.uid))
            loadFriends()
            withContext(Dispatchers.Main) {
                etFriendUsername.setText("") // Clear input after adding
            }
        }
    }

    private fun deleteFriend(friend: Friends) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.friendsDAO().delete(friend)
            loadFriends()
        }
    }
}