package com.example.myapplication.datamanager.activity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ActivityDAO {
    @Query("SELECT * FROM Activity")
    suspend fun getAll(): List<Activity>

    @Query("SELECT * FROM Activity WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): Activity

    @Query("UPDATE Activity SET description = :description WHERE name LIKE :name ")
    fun updateDescriptionByName(name: String, description: String)

    @Query("DELETE FROM Activity WHERE name LIKE :name")
    fun deleteActivityByName(name: String)

    @Update
    fun update(activity: Activity)

    @Insert
    fun insert(activity: Activity)

    @Delete
    fun delete(activity: Activity)
}
