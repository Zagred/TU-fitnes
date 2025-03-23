package com.example.myapplication.social

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.user.Friends
import com.example.myapplication.datamanager.user.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendsActivity : AppCompatActivity() {
    private lateinit var btnAddFriend: Button
    private lateinit var btnClear: Button
    private lateinit var etFriendUsername: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTitle: TextView
    private lateinit var database: AppDatabase

    private var loggedUserId: Int = -1
    private var isAdmin: Boolean = false
    private lateinit var adapter: FriendsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_friends)

        btnAddFriend = findViewById(R.id.btnAdd)
        btnClear = findViewById(R.id.btnClear)
        etFriendUsername = findViewById(R.id.etName)
        recyclerView = findViewById(R.id.rvFriends)
        tvTitle = findViewById(R.id.tvTitle)

        database = AppDatabase.getInstance(application)

        loggedUserId = intent.getIntExtra("USER_ID", -1)
        if (loggedUserId == -1) {
            Toast.makeText(this, "Invalid User", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Check if user is admin
        lifecycleScope.launch(Dispatchers.IO) {
            val currentUser = database.userDAO().findById(loggedUserId)
            isAdmin = currentUser?.role == "admin"

            withContext(Dispatchers.Main) {
                setupUI()
            }
        }
    }

    private fun setupUI() {
        if (isAdmin) {
            // Admin mode setup
            tvTitle.text = "Manage Users"
            etFriendUsername.hint = "Search users..."
            btnAddFriend.visibility = View.GONE

            adapter = FriendsAdapter(
                onDeleteClick = { item ->
                    if (item is User) {
                        deleteUser(item)
                    }
                },
                isAdmin = true
            )

            loadAllUsers()
        } else {
            // Regular user mode setup
            tvTitle.text = "My Friends"
            etFriendUsername.hint = "Add friend by username..."

            adapter = FriendsAdapter(
                onDeleteClick = { item ->
                    if (item is Friends) {
                        deleteFriend(item)
                    }
                },
                isAdmin = false
            )

            loadFriends()

            btnAddFriend.setOnClickListener {
                val friendUsername = etFriendUsername.text.toString().trim()
                if (friendUsername.isNotEmpty()) {
                    addFriend(friendUsername)
                } else {
                    Toast.makeText(this, "Enter a username", Toast.LENGTH_SHORT).show()
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnClear.setOnClickListener {
            etFriendUsername.setText("")
        }
    }

    private fun loadFriends() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Get the friendship relationships
            val friendsList = database.friendsDAO().getFriendsForUser(loggedUserId)

            // Get usernames for each friend
            val friendsWithNames = friendsList.map { friend ->
                val username = database.userDAO().findById(friend.friendsId)?.username ?: "Unknown User"
                FriendWithName(friend, username)
            }

            withContext(Dispatchers.Main) {
                adapter.setFriendsData(friendsWithNames)
            }
        }
    }

    private fun loadAllUsers() {
        lifecycleScope.launch(Dispatchers.IO) {
            val allUsers = database.userDAO().getAll()

            withContext(Dispatchers.Main) {
                adapter.setUsersData(allUsers)
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
                etFriendUsername.setText("")
            }
        }
    }

    private fun deleteFriend(friend: Friends) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.friendsDAO().delete(friend)
            loadFriends()
        }
    }

    private fun deleteUser(user: User) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Prevent deleting yourself
            if (user.uid == loggedUserId) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FriendsActivity, "You cannot delete yourself", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            // Delete the user from database
            database.userDAO().delete(user)

            // Reload users list
            loadAllUsers()

            withContext(Dispatchers.Main) {
                Toast.makeText(this@FriendsActivity, "User deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// Data class to hold friend info with username
data class FriendWithName(
    val friend: Friends,
    val username: String
)