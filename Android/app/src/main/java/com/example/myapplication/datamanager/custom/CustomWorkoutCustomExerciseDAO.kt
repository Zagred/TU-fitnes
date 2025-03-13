package com.example.myapplication.datamanager.custom

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CustomWorkoutCustomExerciseDAO {
    @Query("SELECT * FROM CustomWorkoutCustomExercise")
    fun getAll(): List<CustomWorkoutCustomExercise>

    @Insert
    fun insert(customWorkoutCustomExercise: CustomWorkoutCustomExercise)
    /*@Query("SELECT * FROM CustomWorkout WHERE user_id IN " +
            "(SELECT uid, username FROM User WHERE username LIKE :username)")
    fun getWorkoutsExerciseByUsername(username: String): List<WorkoutExercise>

    @Query("SELECT * FROM CustomExercise WHERE id IN " +
            "(SELECT * FROM CustomWorkout WHERE user_id IN " +
            "(SELECT uid, username FROM User WHERE username LIKE :username))")
    fun getExerciseWorkoutByUsername(username: String): List<ExerciseWorkout>*/
}
