package com.example.myapplication.datamanager.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PostDAO {

    @Insert
    suspend fun insertPost(post: Post)

    @Query("SELECT * FROM Post ORDER BY post_id DESC")
    suspend fun getAllPosts(): List<Post>

    @Query("SELECT * FROM Post WHERE user_id = :userId")
    suspend fun getPostsByUser(userId: Int): List<Post>

    @Delete
    suspend fun deletePost(post: Post)

    @Query("SELECT Post.post_id as postId, Post.user_id as userId, Post.title, Post.message, " +
            "Post.image_path as imagePath, User.username " +
            "FROM Post INNER JOIN User ON Post.user_id = User.uid " +
            "ORDER BY Post.post_id DESC")
    suspend fun getAllPostsWithUsername(): List<PostWithUsername>

    @Query("DELETE FROM Post WHERE post_id = :postId")
    suspend fun deletePostById(postId: Int)
}