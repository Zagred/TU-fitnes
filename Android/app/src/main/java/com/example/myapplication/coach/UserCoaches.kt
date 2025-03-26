package com.example.myapplication.coach

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.coach.CoachDAO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserCoachesActivity : AppCompatActivity() {
    private lateinit var coachRecyclerView: RecyclerView
    private lateinit var coachDAO: CoachDAO
    private lateinit var coachAdapter: CoachAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_coaches)

        val database = AppDatabase.getInstance(applicationContext)
        coachDAO = database.coachDAO()

        coachRecyclerView = findViewById(R.id.userCoachRecyclerView)

        coachAdapter = CoachAdapter(
            mutableListOf(),
            isAdminMode = false
        )
        coachRecyclerView.layoutManager = LinearLayoutManager(this)
        coachRecyclerView.adapter = coachAdapter

        loadCoaches()
    }

    private fun loadCoaches() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val coaches = coachDAO.getAllActiveCoaches()

                withContext(Dispatchers.Main) {
                    coachAdapter.updateCoaches(coaches)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UserCoachesActivity, "Failed to load coaches: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}