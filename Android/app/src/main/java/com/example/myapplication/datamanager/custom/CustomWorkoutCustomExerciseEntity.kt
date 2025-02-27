package com.example.myapplication.datamanager.custom

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Junction
import androidx.room.Relation

@Entity(
    tableName = "CustomWorkoutCustomExercise",
    primaryKeys = ["custom_workout_id", "custom_exercise_id"],
    foreignKeys = [
        ForeignKey(
            entity = CustomWorkout::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("custom_workout_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CustomExercise::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("custom_exercise_id"),
            onDelete = ForeignKey.CASCADE
        )
    ])
data class CustomWorkoutCustomExercise(
    val customWorkoutId: Int,
    val customExerciseId: Int
)


data class WorkoutExercise(
    @Embedded val student: CustomWorkout,
    @Relation(
        parentColumn = "id",
        entityColumn = "custom_workout_id",
        associateBy = Junction(CustomWorkoutCustomExercise::class)
    )
    val courses: List<CustomExercise>
)


data class ExerciseWorkout(
    @Embedded val student: CustomExercise,
    @Relation(
        parentColumn = "id",
        entityColumn = "custom_exercise_id",
        associateBy = Junction(CustomWorkoutCustomExercise::class)
    )
    val courses: List<CustomExercise>
)

