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

}
