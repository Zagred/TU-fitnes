package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.activity.Activity
import com.example.myapplication.datamanager.activity.ActivityDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope

class Workout : AppCompatActivity() {

    private lateinit var activityDao: ActivityDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        val db = AppDatabase.getInstance(applicationContext)
        activityDao = db.activityDAO()

        //insertActivities()
        populateDatabaseIfNeeded(applicationContext)
        displayActivities()
    }

    private fun insertActivities() {
        lifecycleScope.launch(Dispatchers.IO) {
            activityDao.insert(Activity(name = "Running", metabolicEquivalent = 8, description = "Running at a moderate pace"))
            activityDao.insert(Activity(name = "Cycling", metabolicEquivalent = 6, description = "Cycling at a moderate pace"))
        }
    }

    private fun displayActivities() {
        lifecycleScope.launch(Dispatchers.IO) {
            val activities = activityDao.getAll()
            withContext(Dispatchers.Main) {
                val activityTextView = findViewById<TextView>(R.id.activityTextView)
                val activitiesText = activities.joinToString("\n\n") { activity ->
                    "Name: ${activity.name}\nMET: ${activity.metabolicEquivalent}\nDescription: ${activity.description}"
                }
                activityTextView.text = activitiesText
            }
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
}