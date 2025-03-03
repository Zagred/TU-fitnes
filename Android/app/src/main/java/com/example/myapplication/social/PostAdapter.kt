package com.example.myapplication.social

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.user.Post


class PostAdapter(private val loggedUserId: Int, private val onDeleteClick: (Post) -> Unit) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private var postsList = emptyList<Post>()

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tvTitle)
        val messageTextView: TextView = itemView.findViewById(R.id.tvMessage)
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

        // Allow delete only if the logged-in user created the post
        if (currentPost.userId == loggedUserId) {
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                onDeleteClick(currentPost)
            }
        } else {
            holder.deleteButton.visibility = View.GONE
        }
    }

    override fun getItemCount() = postsList.size

    fun setData(posts: List<Post>) {
        this.postsList = posts
        notifyDataSetChanged()
    }
}
