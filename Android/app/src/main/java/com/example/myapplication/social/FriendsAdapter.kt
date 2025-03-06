package com.example.myapplication.social

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.user.Friends

class FriendsAdapter(private val onDeleteClick: (Friends) -> Unit) :
    RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    private var friendsList = emptyList<Friends>()

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.tvFriendName)
        val deleteButton: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_friends, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val currentFriend = friendsList[position]
        holder.usernameTextView.text = "Friend ID: ${currentFriend.friendsId}"

        holder.deleteButton.setOnClickListener {
            onDeleteClick(currentFriend)
        }
    }

    override fun getItemCount() = friendsList.size

    fun setData(friends: List<Friends>) {
        this.friendsList = friends
        notifyDataSetChanged()
    }
}