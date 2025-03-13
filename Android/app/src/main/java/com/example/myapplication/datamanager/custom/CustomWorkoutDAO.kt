package com.example.myapplication.datamanager.custom

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CustomWorkoutDAO {
    @Query("SELECT * FROM CustomWorkout")
    fun getAll(): List<CustomWorkout>
/*
    @Query("SELECT * FROM CustomWorkout WHERE user_id IN " +
            "(SELECT * FROM USER WHERE username LIKE :username)")
    fun getAllUserWorkoutsByUsername(id: Int, username: String): List<CustomWorkout>

    @Query("SELECT * FROM CustomWorkout WHERE id = :id LIMIT 1")
    fun findById(id: Int): CustomWorkout

    @Query("UPDATE CustomWorkout SET name = :name WHERE id LIKE :id ")
    fun updateNameById(id: Int, name: String)

    @Query("UPDATE CustomWorkout SET rest_in_seconds = :rest WHERE id LIKE :id ")
    fun updateRestById(id: Int, rest: Int)

    @Query("UPDATE CustomWorkout SET rest_in_seconds = :rest WHERE name LIKE :name ")
    fun updateRestByName(name: String, rest: Int)

    @Query("DELETE FROM CustomWorkout WHERE id = :id")
    fun deleteCustomWorkoutById(id: Int)

*/
@Query("SELECT COUNT(*) FROM CustomWorkout")
fun getWorkoutCount(): Int

    @Update
    fun update(customWorkout: CustomWorkout)

    @Insert
    fun insert(customWorkout: CustomWorkout)

    @Delete
    fun delete(customWorkout: CustomWorkout)
}