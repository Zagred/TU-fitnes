package com.example.myapplication.datamanager.coach
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CoachDAO {
    @androidx.room.Insert
    suspend fun insertCoach(coach: Coach)

    @androidx.room.Query("SELECT * FROM coaches WHERE isActive = 1")
    suspend fun getAllActiveCoaches(): List<Coach>

    @androidx.room.Update
    suspend fun updateCoach(coach: Coach)

    @androidx.room.Delete
    suspend fun deleteCoach(coach: Coach)
}