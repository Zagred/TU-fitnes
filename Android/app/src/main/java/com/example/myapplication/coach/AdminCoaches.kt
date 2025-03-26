package com.example.myapplication.coach

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.coach.Coach
import com.example.myapplication.datamanager.coach.CoachDAO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminCoachesActivity : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var specializationInput: EditText
    private lateinit var contactInfoInput: EditText
    private lateinit var addCoachButton: Button
    private lateinit var coachRecyclerView: RecyclerView
    private lateinit var coachDAO: CoachDAO
    private lateinit var coachAdapter: CoachAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_coaches)

        val database = AppDatabase.getInstance(applicationContext)
        coachDAO = database.coachDAO()

        nameInput = findViewById(R.id.coachNameInput)
        specializationInput = findViewById(R.id.coachSpecializationInput)
        contactInfoInput = findViewById(R.id.coachContactInfoInput)
        addCoachButton = findViewById(R.id.addCoachButton)
        coachRecyclerView = findViewById(R.id.coachRecyclerView)

        coachAdapter = CoachAdapter(
            mutableListOf(),
            isAdminMode = true,
            onDeleteClick = { coach -> deleteCoach(coach) }
        )
        coachRecyclerView.layoutManager = LinearLayoutManager(this)
        coachRecyclerView.adapter = coachAdapter

        addCoachButton.setOnClickListener {
            addNewCoach()
        }

        loadCoaches()
    }

    private fun addNewCoach() {
        val name = nameInput.text.toString().trim()
        val specialization = specializationInput.text.toString().trim()
        val contactInfo = contactInfoInput.text.toString().trim()

        if (name.isEmpty() || specialization.isEmpty() || contactInfo.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val newCoach = Coach(
            name = name,
            specialization = specialization,
            contactInfo = contactInfo
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                coachDAO.insertCoach(newCoach)

                withContext(Dispatchers.Main) {
                    // Clear input fields
                    nameInput.text.clear()
                    specializationInput.text.clear()
                    contactInfoInput.text.clear()

                    loadCoaches()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminCoachesActivity, "Failed to add coach: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
                    Toast.makeText(this@AdminCoachesActivity, "Failed to load coaches: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteCoach(coach: Coach) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Soft delete by setting isActive to false
                val updatedCoach = coach.copy(isActive = false)
                coachDAO.updateCoach(updatedCoach)

                withContext(Dispatchers.Main) {
                    loadCoaches()
                    Toast.makeText(this@AdminCoachesActivity, "Coach removed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminCoachesActivity, "Failed to remove coach: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}