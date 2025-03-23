// PostAdapter.kt
package com.example.myapplication.social

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.user.Post
import com.example.myapplication.datamanager.user.PostWithUsername

class PostAdapter(
    private val loggedUserId: Int,
    private val isAdmin: Boolean,
    private val onDeleteClick: (PostWithUsername) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private var postsList = emptyList<PostWithUsername>()

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tvTitle)
        val messageTextView: TextView = itemView.findViewById(R.id.tvMessage)
        val usernameTextView: TextView = itemView.findViewById(R.id.tvUsername)
        val deleteButton: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentPost = postsList[position]
        holder.titleTextView.text = currentPost.title
        holder.messageTextView.text = currentPost.message
        holder.usernameTextView.text = "Posted by: ${currentPost.username}"

        // Show delete button if admin or if post belongs to logged-in user
        if (isAdmin || currentPost.userId == loggedUserId) {
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                onDeleteClick(currentPost)
            }
        } else {
            holder.deleteButton.visibility = View.GONE
        }
    }

    override fun getItemCount() = postsList.size

    fun setData(posts: List<PostWithUsername>) {
        this.postsList = posts
        notifyDataSetChanged()
    }
}