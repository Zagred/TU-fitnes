package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.activity.Activity
import com.example.myapplication.datamanager.activity.ActivityDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.calculator.BMICalculator
import com.example.myapplication.datamanager.custom.CustomExercise
import com.example.myapplication.datamanager.custom.CustomExerciseDAO
import com.example.myapplication.datamanager.custom.CustomWorkout
import com.example.myapplication.datamanager.custom.CustomWorkoutCustomExercise
import com.example.myapplication.datamanager.custom.CustomWorkoutCustomExerciseDAO
import kotlinx.coroutines.CoroutineScope

class MainWorkout : AppCompatActivity() {

    private lateinit var activityDao: ActivityDAO
    private lateinit var customExerciseDAO: CustomExerciseDAO
    private lateinit var  customWorkoutCustomExerciseDAO: CustomWorkoutCustomExerciseDAO
    private lateinit var  customWorkoutDAO: CustomWorkoutCustomExerciseDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_workout)

        val db = AppDatabase.getInstance(applicationContext)
        activityDao = db.activityDAO()

        //insertActivities()
        //populateDatabaseIfNeeded(applicationContext)
        populateEXDatabaseIfNeeded(applicationContext)
        //displayActivities()

        val addWorkoutButton = findViewById<TextView>(R.id.addWorkout)
        addWorkoutButton.setOnClickListener{
            val intent= Intent(this, YourWorkout::class.java)
            startActivity(intent)
        }
    }

    private fun insertActivities() {
        lifecycleScope.launch(Dispatchers.IO) {
            activityDao.insert(Activity(name = "Running", metabolicEquivalent = 8, description = "Running at a moderate pace"))
            activityDao.insert(Activity(name = "Cycling", metabolicEquivalent = 6, description = "Cycling at a moderate pace"))
        }
    }


    fun populateDatabaseIfNeeded(context: Context) {
        val db = AppDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            if (db.activityDAO().getAll().isEmpty()) {
                db.activityDAO().insert(Activity(name = "Running", metabolicEquivalent = 8, description = "Running at moderate pace"))
                db.activityDAO().insert(Activity(name = "Cycling", metabolicEquivalent = 6, description = "Casual cycling"))
                db.activityDAO().insert(Activity(name = "Swimming", metabolicEquivalent = 10, description = "Freestyle swimming"))
                db.activityDAO().insert(Activity(name = "Walking", metabolicEquivalent = 3, description = "Brisk walking"))

            }
        }
    }
    fun populateEXDatabaseIfNeeded(context: Context) {
        val db = AppDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            if (db.activityDAO().getAll().isEmpty()) {
                db.activityDAO().insert(Activity(name = "BenchPress", metabolicEquivalent = 8, description = "bench press lifting"))

            }
            if (db.customExerciseDAO().getAll().isEmpty()) {
                db.customExerciseDAO().insert(CustomExercise(activityId = 1, weightsInKg = 6 , reps = 8 , sets = 3 ))
            }
            if ( db.customWorkoutDAO().getWorkoutCount() == 1) {
                db.customWorkoutDAO().insert(CustomWorkout(userId = 1, name = 1, restInSeconds = 180 ))
            }
            if (db.customWorkoutCustomExerciseDAO().getAll().isEmpty()) {
                db.customWorkoutCustomExerciseDAO().insert(CustomWorkoutCustomExercise(customWorkoutId = 2, customExerciseId = 1))
            }
        }
    }
}