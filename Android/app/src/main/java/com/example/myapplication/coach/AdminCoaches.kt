package com.example.myapplication.coach

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.HomePage
import com.example.myapplication.R
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.coach.Coach
import com.example.myapplication.datamanager.coach.CoachDAO
import com.example.myapplication.datamanager.coach.CoachValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminCoachesActivity : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var specializationSpinner: AutoCompleteTextView
    private lateinit var contactInfoInput: EditText
    private lateinit var addCoachButton: Button
    private lateinit var coachRecyclerView: RecyclerView
    private lateinit var coachDAO: CoachDAO
    private lateinit var coachAdapter: CoachAdapter
    private val validator = CoachValidator()
    private var selectedSpecialization: String = ""

    // List of specializations
    private val specializations = listOf(
        "Select Specialization",  // Default prompt item
        "Strength Training",
        "Cardio",
        "Yoga",
        "Pilates",
        "Nutrition",
        "Weight Loss",
        "Body Building",
        "CrossFit",
        "Rehabilitation",
        "Sports Performance",
        "Functional Training",
        "General Fitness"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_coaches)

        val database = AppDatabase.getInstance(applicationContext)
        coachDAO = database.coachDAO()

        nameInput = findViewById(R.id.coachNameInput)
        specializationSpinner = findViewById(R.id.coachSpecializationDropdown)
        contactInfoInput = findViewById(R.id.coachContactInfoInput)
        addCoachButton = findViewById(R.id.addCoachButton)
        coachRecyclerView = findViewById(R.id.coachRecyclerView)

        setupSpecializationSpinner()

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
        val home=findViewById<Button>(R.id.btHome)
        home.setOnClickListener{
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupSpecializationSpinner() {
        // Create an ArrayAdapter using the string array and the custom dropdown item layout
        val adapter = ArrayAdapter(
            this,
            R.layout.dropdown_item_layout,  // Use your custom dropdown layout
            specializations
        )

        // Apply the adapter to the AutoCompleteTextView
        (specializationSpinner as? AutoCompleteTextView)?.setAdapter(adapter)

        // Set item click listener for selection
        specializationSpinner.setOnItemClickListener { parent, _, position, _ ->
            selectedSpecialization = if (position > 0) {
                specializations[position]
            } else {
                ""
            }
        }
    }

    private fun addNewCoach() {
        val name = nameInput.text.toString().trim()
        val contactInfo = contactInfoInput.text.toString().trim()

        // Validate individual fields
        val nameValidation = validator.validateName(name)
        if (!nameValidation.isValid) {
            nameInput.error = nameValidation.errorMessage
            return
        }

        // Validate specialization (now using spinner selection)
        if (selectedSpecialization.isEmpty()) {
            Toast.makeText(this, "Please select a specialization", Toast.LENGTH_SHORT).show()
            return
        }

        val contactValidation = validator.validateContactInfo(contactInfo)
        if (!contactValidation.isValid) {
            contactInfoInput.error = contactValidation.errorMessage
            return
        }

        // Check for duplicates and add the coach
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check for duplicate coaches
                val duplicateValidation = validator.checkDuplicate(coachDAO, name, contactInfo)
                if (!duplicateValidation.isValid) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AdminCoachesActivity, duplicateValidation.errorMessage, Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // Create and insert the coach
                val newCoach = Coach(
                    name = name,
                    specialization = selectedSpecialization,
                    contactInfo = contactInfo
                )

                coachDAO.insertCoach(newCoach)

                withContext(Dispatchers.Main) {
                    // Clear input fields
                    nameInput.text.clear()
                    nameInput.error = null
                    specializationSpinner.setSelection(0)  // Reset spinner to default
                    contactInfoInput.text.clear()
                    contactInfoInput.error = null

                    // Show success message
                    Toast.makeText(this@AdminCoachesActivity, "Coach added successfully", Toast.LENGTH_SHORT).show()

                    // Reload coaches list
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