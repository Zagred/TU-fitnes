package com.example.myapplication.workout

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.activity.Activity
import com.example.myapplication.datamanager.custom.CustomExercise
import com.example.myapplication.datamanager.custom.CustomExerciseDAO
import com.example.myapplication.datamanager.custom.CustomWorkout
import com.example.myapplication.datamanager.custom.CustomWorkoutCustomExercise
import com.example.myapplication.datamanager.custom.CustomWorkoutCustomExerciseDAO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class YourWorkout : AppCompatActivity() {
    private lateinit var customExerciseDAO: CustomExerciseDAO
    private lateinit var customWorkoutCustomExerciseDAO: CustomWorkoutCustomExerciseDAO
    private lateinit var customWorkoutDAO: CustomWorkoutCustomExerciseDAO
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_your_workout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun populateDatabaseIfNeeded(context: Context) {
        val db = AppDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            if (db.activityDAO().getAll().isEmpty()) {
                db.activityDAO().insert(
                    Activity(
                        name = "Running",
                        metabolicEquivalent = 8,
                        description = "Running at moderate pace"
                    )
                )
                db.activityDAO().insert(
                    Activity(
                        name = "Cycling",
                        metabolicEquivalent = 6,
                        description = "Casual cycling"
                    )
                )
                db.activityDAO().insert(
                    Activity(
                        name = "Swimming",
                        metabolicEquivalent = 10,
                        description = "Freestyle swimming"
                    )
                )
                db.activityDAO().insert(
                    Activity(
                        name = "Walking",
                        metabolicEquivalent = 3,
                        description = "Brisk walking"
                    )
                )

            }
        }
    }

    fun populateEXDatabaseIfNeeded(context: Context) {
        val db = AppDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            if (db.activityDAO().getAll().isEmpty()) {
                db.activityDAO().insert(
                    Activity(
                        name = "BenchPress",
                        metabolicEquivalent = 8,
                        description = "bench press lifting"
                    )
                )

            }
            if (db.customExerciseDAO().getAll().isEmpty()) {
                db.customExerciseDAO()
                    .insert(CustomExercise(activityId = 1, weightsInKg = 6, reps = 8, sets = 3))
            }
            if (db.customWorkoutDAO().getWorkoutCount() == 1) {
                db.customWorkoutDAO()
                    .insert(CustomWorkout(userId = 1, name = "first worckout", restInSeconds = 180))
            }
            if (db.customWorkoutCustomExerciseDAO().getAll().isEmpty()) {
                db.customWorkoutCustomExerciseDAO()
                    .insert(CustomWorkoutCustomExercise(customWorkoutId = 2, customExerciseId = 1))
            }
        }
    }
}