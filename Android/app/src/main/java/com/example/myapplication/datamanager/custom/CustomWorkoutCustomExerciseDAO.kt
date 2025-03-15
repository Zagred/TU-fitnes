package com.example.myapplication.datamanager.custom

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CustomWorkoutCustomExerciseDAO {
    @Query("SELECT * FROM CustomWorkoutCustomExercise")
    fun getAll(): List<CustomWorkoutCustomExercise>

    @Insert
    fun insert(customWorkoutCustomExercise: CustomWorkoutCustomExercise)

    @Transaction
    @Query("SELECT * FROM CustomExercise WHERE id IN (SELECT customExerciseId FROM CustomWorkoutCustomExercise WHERE customWorkoutId = :workoutId)")
    fun getExercisesForWorkout(workoutId: Int): List<CustomExercise>
}
