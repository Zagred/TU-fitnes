package com.example.myapplication.datamanager.user
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "Post",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE // Optional: deletes posts when user is deleted
        )
    ]
)
data class Post(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name="post_id") val postId: Int = 0,
    @ColumnInfo(name="user_id") val userId: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "message") val message: String,
)
data class PostWithUsername(
    val postId: Int,
    val userId: Int,
    val title: String,
    val message: String,
    val username: String
)