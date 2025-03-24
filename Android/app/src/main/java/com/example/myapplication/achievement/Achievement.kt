package com.example.myapplication.achievement

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.achievement.Achievement
import com.example.myapplication.datamanager.achievement.AchievementDAO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AchievementActivity : AppCompatActivity() {

    private lateinit var goalNameInput: EditText
    private lateinit var targetValueInput: EditText
    private lateinit var addGoalButton: Button
    private lateinit var goalRecyclerView: RecyclerView
    private lateinit var achievementDAO: AchievementDAO
    private lateinit var achievementAdapter: AchievementAdapter
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievement)

        userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val database = AppDatabase.getInstance(applicationContext)
        achievementDAO = database.achievementDAO()

        goalNameInput = findViewById(R.id.goalNameInput)
        targetValueInput = findViewById(R.id.targetValueInput)
        addGoalButton = findViewById(R.id.addGoalButton)
        goalRecyclerView = findViewById(R.id.goalRecyclerView)

        achievementAdapter = AchievementAdapter(
            mutableListOf(),
            onProgressUpdate = { achievement, progress ->
                updateAchievementProgress(achievement, progress)
            },
            onDeleteClick = { achievement ->
                deleteAchievement(achievement)
            }
        )
        goalRecyclerView.layoutManager = LinearLayoutManager(this)
        goalRecyclerView.adapter = achievementAdapter

        addGoalButton.setOnClickListener {
            addNewGoal()
        }

        loadUserGoals()
    }

    private fun addNewGoal() {
        val goalName = goalNameInput.text.toString().trim()
        val targetValueText = targetValueInput.text.toString().trim()

        if (goalName.isEmpty() || targetValueText.isEmpty()) {
            Toast.makeText(this, "Please enter goal name and target value", Toast.LENGTH_SHORT).show()
            return
        }

        val targetValue = targetValueText.toIntOrNull() ?: run {
            Toast.makeText(this, "Invalid target value", Toast.LENGTH_SHORT).show()
            return
        }

        val newAchievement = Achievement(
            userId = userId,
            goalName = goalName,
            targetValue = targetValue
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                achievementDAO.insertAchievement(newAchievement)

                withContext(Dispatchers.Main) {
                    goalNameInput.text.clear()
                    targetValueInput.text.clear()

                    loadUserGoals()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AchievementActivity, "Failed to add goal: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadUserGoals() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val achievements = achievementDAO.getAchievementsForUser(userId)

                withContext(Dispatchers.Main) {
                    achievementAdapter.updateAchievements(achievements)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AchievementActivity, "Failed to load goals: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateAchievementProgress(achievement: Achievement, progress: Int) {
        val updatedAchievement = achievement.copy(
            currentValue = minOf(progress, achievement.targetValue),
            isCompleted = progress >= achievement.targetValue
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                achievementDAO.updateAchievement(updatedAchievement)

                withContext(Dispatchers.Main) {
                    loadUserGoals()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AchievementActivity, "Failed to update goal: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteAchievement(achievement: Achievement) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                achievementDAO.deleteAchievement(achievement)

                withContext(Dispatchers.Main) {
                    loadUserGoals()
                    Toast.makeText(this@AchievementActivity, "Goal deleted", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AchievementActivity, "Failed to delete goal: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}