package com.example.myapplication.challenge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.HomePage
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityChallengeBinding
import com.example.myapplication.datamanager.AppDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myapplication.datamanager.LoggedUser
import com.example.myapplication.datamanager.user.UserScoreManager
import com.example.myapplication.datamanager.user.UserChallengeManager

class ChallengeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChallengeBinding
    private lateinit var challengeDao: ChallengeDAO
    private var selectedChallenge: Challenge? = null
    private val loadedChallenges = mutableListOf<Challenge>()
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallengeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUserGreeting()

        challengeDao = AppDatabase.getInstance(this).challengeDAO()

        // Load and display the dumbbell count
        loadDumbbellCount()

        // Check if we need to load new daily challenges or use existing ones
        checkAndLoadDailyChallenges()
        userId = intent.getIntExtra("USER_ID", -1)

        binding.btnConfirm.setOnClickListener {
            if (selectedChallenge != null) {
                val isChallengeCompleted = UserChallengeManager.isCurrentUserChallengeCompleted(
                    this,
                    selectedChallenge!!.id
                )

                if (isChallengeCompleted) {
                    Toast.makeText(this, "This challenge is already completed!", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(this, ChallengePROOFActivity::class.java)
                    intent.putExtra("challenge_id", selectedChallenge!!.id)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, "Please select a challenge", Toast.LENGTH_SHORT).show()
            }
        }
        val home=findViewById<Button>(R.id.btHome)
        home.setOnClickListener{
            val intent = Intent(this, HomePage::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload the dumbbell count when returning to this activity
        loadDumbbellCount()

        // Also refresh the challenges list to update completed status
        refreshChallengesUI()
    }

    private fun loadDumbbellCount() {
        // Get the current dumbbell count using our manager
        val dumbbellCount = UserScoreManager.getCurrentUserDumbbellCount(this)

        // Update the TextView displaying the count
        binding.tvDumbbellCount.text = dumbbellCount.toString()
    }

    private fun setupUserGreeting() {
        val username = LoggedUser.getUsername() ?: "User"
        val greeting = "Hello, $username!\nAre you ready for your challenges of the day? :)"

        val spannable = android.text.SpannableString(greeting)

        val start = greeting.indexOf(username)
        val end = start + username.length

        spannable.setSpan(
            android.text.style.ForegroundColorSpan(ContextCompat.getColor(this, R.color.yellow)),
            start,
            end,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvGreeting.text = spannable
    }

    private fun checkAndLoadDailyChallenges() {
        // Use username-specific preferences for challenge dates
        val username = LoggedUser.getUsername() ?: "default"
        val sharedPref = getSharedPreferences("user_${username}_challenge_prefs", Context.MODE_PRIVATE)
        val lastChallengeDate = sharedPref.getString("last_challenge_date", "")
        val currentDate = getCurrentDate()

        if (lastChallengeDate != currentDate) {
            // It's a new day, load new challenges
            loadNewDailyChallenges(currentDate)
        } else {
            // Same day, load saved challenges
            loadSavedDailyChallenges()
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun loadNewDailyChallenges(currentDate: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val username = LoggedUser.getUsername() ?: "default"
            // 1. Reset any previously saved daily challenge IDs
            val sharedPref = getSharedPreferences("user_${username}_challenge_prefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("last_challenge_date", currentDate)
                putInt("daily_easy_id", -1)
                putInt("daily_medium_id", -1)
                putInt("daily_hard_id", -1)
                apply()
            }

            // 2. Get new random challenges
            val easy = challengeDao.getRandomByDifficulty("EASY", 1)
            val medium = challengeDao.getRandomByDifficulty("MEDIUM", 1)
            val hard = challengeDao.getRandomByDifficulty("HARD", 1)

            // 3. Save these challenge IDs
            if (easy.isNotEmpty()) {
                sharedPref.edit().putInt("daily_easy_id", easy[0].id).apply()
            }
            if (medium.isNotEmpty()) {
                sharedPref.edit().putInt("daily_medium_id", medium[0].id).apply()
            }
            if (hard.isNotEmpty()) {
                sharedPref.edit().putInt("daily_hard_id", hard[0].id).apply()
            }

            // 4. Load challenges into UI
            loadedChallenges.clear()
            loadedChallenges.addAll(easy + medium + hard)

            withContext(Dispatchers.Main) {
                showRadioButtons()
            }
        }
    }

    private fun loadSavedDailyChallenges() {
        CoroutineScope(Dispatchers.IO).launch {
            // Get saved challenge IDs from user-specific prefs
            val username = LoggedUser.getUsername() ?: "default"
            val sharedPref = getSharedPreferences("user_${username}_challenge_prefs", Context.MODE_PRIVATE)
            val easyId = sharedPref.getInt("daily_easy_id", -1)
            val mediumId = sharedPref.getInt("daily_medium_id", -1)
            val hardId = sharedPref.getInt("daily_hard_id", -1)

            loadedChallenges.clear()

            // Load challenges by their IDs
            if (easyId != -1) {
                challengeDao.findById(easyId)?.let { loadedChallenges.add(it) }
            }
            if (mediumId != -1) {
                challengeDao.findById(mediumId)?.let { loadedChallenges.add(it) }
            }
            if (hardId != -1) {
                challengeDao.findById(hardId)?.let { loadedChallenges.add(it) }
            }

            // If no challenges were loaded (first run), get random ones
            if (loadedChallenges.isEmpty()) {
                val currentDate = getCurrentDate()
                withContext(Dispatchers.Main) {
                    loadNewDailyChallenges(currentDate)
                    return@withContext
                }
            }

            withContext(Dispatchers.Main) {
                showRadioButtons()
            }
        }
    }

    private fun refreshChallengesUI() {
        CoroutineScope(Dispatchers.IO).launch {
            // Reload the current challenge status from database
            val refreshedChallenges = mutableListOf<Challenge>()

            for (challenge in loadedChallenges) {
                val refreshedChallenge = challengeDao.findById(challenge.id)
                if (refreshedChallenge != null) {
                    refreshedChallenges.add(refreshedChallenge)
                }
            }

            // Update the loaded challenges list
            loadedChallenges.clear()
            loadedChallenges.addAll(refreshedChallenges)

            withContext(Dispatchers.Main) {
                showRadioButtons()
            }
        }
    }

    private fun showRadioButtons() {
        binding.radioGroupChallenges.removeAllViews()

        loadedChallenges.forEach { challenge ->
            // Check if this challenge is completed for the current user
            val isCompleted = UserChallengeManager.isCurrentUserChallengeCompleted(this, challenge.id)

            val radioButton = RadioButton(this).apply {
                text = "${challenge.title}"
                textSize = 18f

                val leftIcon = when (challenge.difficulty.uppercase()) {
                    "EASY", "MEDIUM", "HARD" -> {
                        if (isCompleted) R.drawable.fitness_green else R.drawable.fitness
                    }
                    else -> 0
                }

                val rightIcon = if (isCompleted) R.drawable.check else 0

                setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0, rightIcon, 0)
                compoundDrawablePadding = 16

                if (isCompleted) {
                    setTextColor(ContextCompat.getColor(context, R.color.greenTick))
                }
            }


            radioButton.setOnClickListener {
                selectedChallenge = challenge
                binding.btnConfirm.isEnabled = true

                // If challenge is completed, disable the confirm button
                val isChallengeCompleted = UserChallengeManager.isCurrentUserChallengeCompleted(this@ChallengeActivity, challenge.id)
                if (isChallengeCompleted) {
                    binding.btnConfirm.isEnabled = false
                    Toast.makeText(this@ChallengeActivity, "This challenge is already completed!", Toast.LENGTH_SHORT).show()
                }
            }

            binding.radioGroupChallenges.addView(radioButton)
        }
    }
}