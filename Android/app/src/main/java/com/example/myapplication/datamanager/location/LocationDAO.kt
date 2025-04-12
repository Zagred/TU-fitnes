package com.example.myapplication.datamanager.location

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LocationDAO {
    @Query("SELECT * FROM Location")
    fun getAll(): List<Location>

    @Query("SELECT * FROM Location WHERE id = :locationId LIMIT 1")
    fun findById(locationId: Int): Location?

    @Query("SELECT * FROM Location WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): Location?

    @Query("SELECT * FROM Location WHERE addedBy = :userId")
    fun getLocationsByAdmin(userId: Int): List<Location>

    @Insert
    fun insert(location: Location): Long

    @Update
    fun update(location: Location)

    @Delete
    fun delete(location: Location)

    @Query("DELETE FROM Location WHERE id = :locationId")
    fun deleteById(locationId: Int)
}