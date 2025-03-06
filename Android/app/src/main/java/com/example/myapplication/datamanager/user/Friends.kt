package com.example.myapplication.datamanager.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "Friends",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"],
            childColumns = ["loggedUser"],
            onDelete = ForeignKey.CASCADE // Optional: deletes friendship entries when user is deleted
        )
    ]
)
data class Friends(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name="id") val uid: Int = 0,
    @ColumnInfo(name="loggedUser") val id: Int,
    @ColumnInfo(name = "frinedOfUser") val friendsId: Int,
)