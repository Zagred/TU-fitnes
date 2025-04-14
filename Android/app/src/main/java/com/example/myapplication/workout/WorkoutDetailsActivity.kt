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
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import androidx.core.app.NotificationCompat
import android.media.RingtoneManager

class WorkoutDetailsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExerciseAdapter
    private lateinit var database: AppDatabase
    private var workoutId: Int = -1
    private var workoutName: String = ""
    private lateinit var restTimer: CountDownTimer
    private var restInSeconds: Int = 0
    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_details)
        requestNotificationPermission()

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
        lifecycleScope.launch(Dispatchers.IO) {
            val workout = database.customWorkoutDAO().findById(workoutId)
            withContext(Dispatchers.Main) {
                restInSeconds = workout?.restInSeconds ?: 60
            }
        }
        createNotificationChannel()
        findViewById<TextView>(R.id.startTimer).setOnClickListener {
            if (!isTimerRunning) {
                startRestTimer()
            } else {
                cancelRestTimer()
            }
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
    private fun startRestTimer() {
        val timerButton = findViewById<TextView>(R.id.startTimer)
        isTimerRunning = true
        timerButton.text = "Cancel"

        restTimer = object : CountDownTimer(restInSeconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                runOnUiThread {
                    timerButton.text = "${secondsRemaining}s"
                }
            }

            override fun onFinish() {
                isTimerRunning = false
                timerButton.text = "Start Rest"
                sendRestCompletedNotification()
            }
        }.start()
    }

    private fun cancelRestTimer() {
        if (::restTimer.isInitialized) {
            restTimer.cancel()
        }
        isTimerRunning = false
        findViewById<TextView>(R.id.startTimer).text = "Start Rest"
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Rest Timer"
            val descriptionText = "Notifications for workout rest timer"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("REST_TIMER_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendRestCompletedNotification() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, "REST_TIMER_CHANNEL")
            .setContentTitle("Rest Time Complete!")
            .setContentText("Time to start your next exercise")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use an appropriate icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::restTimer.isInitialized) {
            restTimer.cancel()
        }
    }
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
    }
}
