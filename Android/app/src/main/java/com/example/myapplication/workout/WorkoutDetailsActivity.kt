package com.example.myapplication.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.activity.Activity
import com.example.myapplication.datamanager.custom.CustomExercise
import com.example.myapplication.datamanager.custom.CustomWorkoutCustomExercise
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
        adapter = ExerciseAdapter(
            onEditClick = { exercise, activity -> editExercise(exercise, activity) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadExercises()

        // Add the button to add exercises
        findViewById<TextView>(R.id.addExercise).setOnClickListener {
            showAddExerciseDialog()
        }
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
    private fun showAddExerciseDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_exercise, null)
        val spinnerActivity = dialogView.findViewById<Spinner>(R.id.spinnerActivity)
        val etWeight = dialogView.findViewById<EditText>(R.id.etWeight)
        val etReps = dialogView.findViewById<EditText>(R.id.etReps)
        val etSets = dialogView.findViewById<EditText>(R.id.etSets)
        val btnSaveExercise = dialogView.findViewById<Button>(R.id.btnSaveExercise)

        lifecycleScope.launch(Dispatchers.IO) {
            val activities = database.activityDAO().getActivities()
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(
                    this@WorkoutDetailsActivity,
                    android.R.layout.simple_spinner_item,
                    activities.map { it.name }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerActivity.adapter = adapter

                val dialog = AlertDialog.Builder(this@WorkoutDetailsActivity)
                    .setTitle("Add Exercise")
                    .setView(dialogView)
                    .create()
                dialog.show()

                btnSaveExercise.setOnClickListener {
                    val selectedPosition = spinnerActivity.selectedItemPosition
                    if (selectedPosition < 0 || selectedPosition >= activities.size) {
                        Toast.makeText(this@WorkoutDetailsActivity, "Please select an activity", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val weight = etWeight.text.toString().trim()
                    val reps = etReps.text.toString().trim()
                    val sets = etSets.text.toString().trim()

                    if (weight.isEmpty() || reps.isEmpty() || sets.isEmpty()) {
                        Toast.makeText(this@WorkoutDetailsActivity, "Please enter all fields", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val selectedActivity = activities[selectedPosition]

                    lifecycleScope.launch(Dispatchers.IO) {
                        val customExercise = CustomExercise(
                            activityId = selectedActivity.id,
                            weightsInKg = weight.toInt(),
                            reps = reps.toInt(),
                            sets = sets.toInt()
                        )

                        val exerciseId = database.customExerciseDAO().insert(customExercise)

                        val relationship = CustomWorkoutCustomExercise(
                            customWorkoutId = workoutId,
                            customExerciseId = exerciseId.toString().toInt()
                        )
                        database.customWorkoutCustomExerciseDAO().insert(relationship)

                        loadExercises()
                    }
                    dialog.dismiss()
                }
            }
        }
    }
    private fun editExercise(exercise: CustomExercise, activity: Activity?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_exercise, null)
        val tvActivityName = dialogView.findViewById<TextView>(R.id.tvActivityName)
        val etWeight = dialogView.findViewById<EditText>(R.id.etWeight)
        val etReps = dialogView.findViewById<EditText>(R.id.etReps)
        val etSets = dialogView.findViewById<EditText>(R.id.etSets)
        val btnSaveExercise = dialogView.findViewById<Button>(R.id.btnSaveExercise)

        tvActivityName.text = activity?.name ?: "Unknown Activity"
        etWeight.setText(exercise.weightsInKg.toString())
        etReps.setText(exercise.reps.toString())
        etSets.setText(exercise.sets.toString())

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Exercise")
            .setView(dialogView)
            .create()
        dialog.show()

        btnSaveExercise.setOnClickListener {
            val weight = etWeight.text.toString().trim()
            val reps = etReps.text.toString().trim()
            val sets = etSets.text.toString().trim()

            if (weight.isEmpty() || reps.isEmpty() || sets.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val updatedExercise = exercise.copy(
                    weightsInKg = weight.toInt(),
                    reps = reps.toInt(),
                    sets = sets.toInt()
                )
                database.customExerciseDAO().update(updatedExercise)
                loadExercises()
            }
            dialog.dismiss()
        }
    }
}
