package com.example.myapplication.social

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.user.Friends
import com.example.myapplication.datamanager.user.User

class FriendsAdapter(
    private val onDeleteClick: (Any) -> Unit,
    private val isAdmin: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var friendsList = emptyList<FriendWithName>()
    private var usersList = emptyList<User>()

    companion object {
        private const val VIEW_TYPE_FRIEND = 0
        private const val VIEW_TYPE_USER = 1
    }

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.tvFriendName)
        val deleteButton: Button = itemView.findViewById(R.id.btnDelete)
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.tvFriendName)
        val roleTextView: TextView = itemView.findViewById(R.id.tvRole)
        val deleteButton: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun getItemViewType(position: Int): Int {
        return if (isAdmin) VIEW_TYPE_USER else VIEW_TYPE_FRIEND
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.list_users, parent, false)
                UserViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.list_friends, parent, false)
                FriendViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isAdmin) {
            val currentUser = usersList[position]
            (holder as UserViewHolder).apply {
                usernameTextView.text = currentUser.username
                roleTextView.text = "Role: ${currentUser.role}"

                deleteButton.setOnClickListener {
                    onDeleteClick(currentUser)
                }
            }
        } else {
            val currentFriend = friendsList[position]
            (holder as FriendViewHolder).apply {
                usernameTextView.text = currentFriend.username

                deleteButton.setOnClickListener {
                    onDeleteClick(currentFriend.friend)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isAdmin) usersList.size else friendsList.size
    }

    fun setFriendsData(friends: List<FriendWithName>) {
        this.friendsList = friends
        notifyDataSetChanged()
    }

    fun setUsersData(users: List<User>) {
        this.usersList = users
        notifyDataSetChanged()
    }
}