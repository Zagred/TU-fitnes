package com.example.myapplication.datamanager.custom

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CustomExerciseDAO {
    @Query("SELECT * FROM CustomExercise")
    fun getAll(): List<CustomExercise>

    @Query("SELECT * FROM CustomExercise WHERE id = :id LIMIT 1")
    fun findById(id: Int): CustomExercise

    @Query("UPDATE CustomExercise SET weights_in_kg = :weights WHERE id LIKE :id ")
    fun updateWeightsById(id: Int, weights: Int)

    @Query("UPDATE CustomExercise SET reps = :reps WHERE id LIKE :id ")
    fun updateRepsById(id: Int, reps: Int)

    @Query("UPDATE CustomExercise SET sets = :sets WHERE id LIKE :id ")
    fun updateSetsById(id: Int, sets: Int)

    @Query("DELETE FROM CustomExercise WHERE id = :id")
    fun deleteCustomExerciseById(id: Int)

    @Update
    fun update(customExercise: CustomExercise)

    @Insert
    fun insert(customExercise: CustomExercise)

    @Delete
    fun delete(customExercise: CustomExercise)
}
