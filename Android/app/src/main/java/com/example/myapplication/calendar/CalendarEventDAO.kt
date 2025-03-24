package com.example.myapplication.calendar

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CalendarEventDAO {
    @Query("SELECT * FROM CalendarEvents WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun getEventsForMonth(userId: Int, month: Int, year: Int): List<CalendarEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CalendarEvent)

    @Delete
    suspend fun delete(event: CalendarEvent)

    @Query("DELETE FROM CalendarEvents WHERE userId = :userId AND date = :date AND month = :month AND year = :year")
    suspend fun deleteEvent(userId: Int, date: String, month: Int, year: Int)
}