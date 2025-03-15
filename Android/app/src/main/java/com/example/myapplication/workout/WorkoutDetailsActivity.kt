package com.example.myapplication.workout

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkoutDetailsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExerciseAdapter
    private lateinit var database: AppDatabase
    private var workoutId: Int = -1
    private var workoutName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_details)

        database = AppDatabase.getInstance(application)
        workoutId = intent.getIntExtra("WORKOUT_ID", -1)
        workoutName = intent.getStringExtra("WORKOUT_NAME") ?: ""

        findViewById<TextView>(R.id.tvWorkoutTitle).text = workoutName

        recyclerView = findViewById(R.id.rvExercises)
        adapter = ExerciseAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadExercises()
    }

    private fun loadExercises() {
        lifecycleScope.launch(Dispatchers.IO) {
            val exercises = database.customWorkoutCustomExerciseDAO().getExercisesForWorkout(workoutId)

            val activityDao = database.activityDAO()
            val exercisesWithActivities = exercises.map { exercise ->
                val activity = activityDao.getActivityById(exercise.activityId)
                Pair(exercise, activity)
            }

            withContext(Dispatchers.Main) {
                adapter.setData(exercisesWithActivities)
            }
        }
    }
}
