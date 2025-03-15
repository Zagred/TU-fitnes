package com.example.myapplication.workout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.activity.Activity
import com.example.myapplication.datamanager.activity.ActivityDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.custom.CustomExercise
import com.example.myapplication.datamanager.custom.CustomExerciseDAO
import com.example.myapplication.datamanager.custom.CustomWorkout
import com.example.myapplication.datamanager.custom.CustomWorkoutCustomExercise
import com.example.myapplication.datamanager.custom.CustomWorkoutCustomExerciseDAO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

class MainWorkout : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var activityDao: ActivityDAO
    private lateinit var adapter: WorkoutAdapter
    private lateinit var database: AppDatabase

    private var loggedUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_workout)

        database = AppDatabase.getInstance(application)
        loggedUserId = intent.getIntExtra("USER_ID", -1)

        // Initialize recyclerView before setting adapter
        recyclerView = findViewById(R.id.rvWorkout)

        adapter = WorkoutAdapter(
            onDeleteClick = { customWorkout -> deleteWorkout(customWorkout) },
            onEditClick = { customWorkout -> editWorkout(customWorkout) },
            onOpenClick = { customWorkout -> openWorkout(customWorkout) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadWorkout()

        val addWorkoutButton = findViewById<TextView>(R.id.addWorkout)
        addWorkoutButton.setOnClickListener {
            showAddWorkoutDialog(applicationContext)
        }
    }

    private fun loadWorkout() {
        lifecycleScope.launch(Dispatchers.IO) {
            val workoutList = database.customWorkoutDAO().getWorkouts(loggedUserId)
            withContext(Dispatchers.Main) {
                adapter.setData(workoutList)
            }
        }
    }

    private fun showAddWorkoutDialog(context: Context) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_workout, null)
        val etWorkoutName = dialogView.findViewById<EditText>(R.id.etWorkoutName)
        val etWorkoutTimer = dialogView.findViewById<EditText>(R.id.etWorkoutTimer)
        val btnSaveWorkout = dialogView.findViewById<Button>(R.id.btnSaveWorkout)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.show()

        btnSaveWorkout.setOnClickListener {
            val workoutName = etWorkoutName.text.toString().trim()
            val workoutTimer = etWorkoutTimer.text.toString().trim()

            if (workoutName.isEmpty() || workoutTimer.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save to database
            val db = AppDatabase.getInstance(context)
            CoroutineScope(Dispatchers.IO).launch {
                db.customWorkoutDAO()
                    .insert(CustomWorkout(userId = loggedUserId, name = workoutName, restInSeconds = workoutTimer.toInt()))
                loadWorkout()
            }
            dialog.dismiss()
        }
    }

    private fun deleteWorkout(customWorkout: CustomWorkout) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.customWorkoutDAO().delete(customWorkout)
            loadWorkout()
        }
    }

    private fun editWorkout(customWorkout: CustomWorkout) {
        Toast.makeText(this, "Editing workout: ${customWorkout.name}", Toast.LENGTH_SHORT).show()
    }

    private fun openWorkout(customWorkout: CustomWorkout) {
        val intent = Intent(this, WorkoutDetailsActivity::class.java).apply {
            putExtra("WORKOUT_ID", customWorkout.id)
            putExtra("WORKOUT_NAME", customWorkout.name)
        }
        startActivity(intent)
    }

}