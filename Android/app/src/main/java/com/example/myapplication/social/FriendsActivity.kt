package com.example.myapplication.social

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.HomePage
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

    // Store the complete list of users for filtering
    private var allUsers: List<User> = emptyList()

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

        lifecycleScope.launch(Dispatchers.IO) {
            val currentUser = database.userDAO().findById(loggedUserId)
            isAdmin = currentUser?.role == "admin"

            withContext(Dispatchers.Main) {
                setupUI()
            }
        }
        val home=findViewById<Button>(R.id.btHome)
        home.setOnClickListener{
            val intent = Intent(this, HomePage::class.java)
            intent.putExtra("USER_ID", loggedUserId)
            startActivity(intent)
            finish()
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

            // Setup search functionality for admin
            etFriendUsername.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    filterUsers(s.toString())
                }
            })

            btnClear.setOnClickListener {
                etFriendUsername.setText("")
                // Reset to display all users when cleared
                adapter.setUsersData(allUsers)
            }

        } else {
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

            btnClear.setOnClickListener {
                etFriendUsername.setText("")
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun filterUsers(query: String) {
        if (query.isEmpty()) {
            adapter.setUsersData(allUsers)
            return
        }

        val filteredList = allUsers.filter { user ->
            user.username.contains(query, ignoreCase = true) ||
                    user.email.contains(query, ignoreCase = true) ||
                    user.role.contains(query, ignoreCase = true)
        }

        adapter.setUsersData(filteredList)
    }

    private fun loadFriends() {
        lifecycleScope.launch(Dispatchers.IO) {
            val friendsList = database.friendsDAO().getFriendsForUser(loggedUserId)

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
            allUsers = database.userDAO().getAll()

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
            if (user.uid == loggedUserId) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FriendsActivity, "You cannot delete yourself", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            database.userDAO().delete(user)

            // Reload and update the filtered list as well
            allUsers = database.userDAO().getAll()

            withContext(Dispatchers.Main) {
                // Apply current filter after deleting
                filterUsers(etFriendUsername.text.toString())
                Toast.makeText(this@FriendsActivity, "User deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

data class FriendWithName(
    val friend: Friends,
    val username: String
)