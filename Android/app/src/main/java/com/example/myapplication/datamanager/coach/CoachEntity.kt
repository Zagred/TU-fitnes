package com.example.myapplication.datamanager.coach

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coaches")
data class Coach(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val specialization: String,
    val contactInfo: String,
    val isActive: Boolean = true
)