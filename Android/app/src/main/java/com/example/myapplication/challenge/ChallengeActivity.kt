package com.example.myapplication.challenge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

class ChallengeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChallengeBinding
    private lateinit var challengeDao: ChallengeDAO
    private var selectedChallenge: Challenge? = null
    private val loadedChallenges = mutableListOf<Challenge>()

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

        binding.btnConfirm.setOnClickListener {
            if (selectedChallenge != null) {
                if (selectedChallenge!!.isCompleted) {
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
    }

    override fun onResume() {
        super.onResume()
        // Reload the dumbbell count when returning to this activity
        loadDumbbellCount()

        // Also refresh the challenges list to update completed status
        refreshChallengesUI()
    }

    private fun loadDumbbellCount() {
        // Get the current dumbbell count from SharedPreferences
        val sharedPref = getSharedPreferences("fitness_app_prefs", Context.MODE_PRIVATE)
        val dumbbellCount = sharedPref.getInt("dumbbell_count", 0)

        // Update the TextView displaying the count
        binding.tvDumbbellCount.text = dumbbellCount.toString()
    }

    private fun setupUserGreeting() {
        val username = LoggedUser.getUsername() ?: "User"
        val greeting = "Hello, $username!\nAre you ready for your challenges of the day? :)"

        //binding.tvGreeting.text
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
        val sharedPref = getSharedPreferences("challenge_prefs", Context.MODE_PRIVATE)
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
            // 1. Reset any previously saved daily challenge IDs
            val sharedPref = getSharedPreferences("challenge_prefs", Context.MODE_PRIVATE)
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
            // Get saved challenge IDs
            val sharedPref = getSharedPreferences("challenge_prefs", Context.MODE_PRIVATE)
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
            val radioButton = RadioButton(this).apply {
                text = "${challenge.title}"
                textSize = 18f

                val iconRes = when (challenge.difficulty.uppercase()) {
                    "EASY" -> if (challenge.isCompleted) R.drawable.fitness_green else R.drawable.fitness
                    "MEDIUM" -> if (challenge.isCompleted) R.drawable.fitness_green else R.drawable.fitness
                    "HARD" -> if (challenge.isCompleted) R.drawable.fitness_green else R.drawable.fitness
                    else -> 0
                }
                // Add green check mark for completed challenges
                if (challenge.isCompleted) {
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0)
                    setTextColor(ContextCompat.getColor(context, R.color.greenTick))
                }

                setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0)
                compoundDrawablePadding = 16
            }

            radioButton.setOnClickListener {
                selectedChallenge = challenge
                binding.btnConfirm.isEnabled = true

                // If challenge is completed, disable the confirm button
                if (challenge.isCompleted) {
                    binding.btnConfirm.isEnabled = false
                    Toast.makeText(this, "This challenge is already completed!", Toast.LENGTH_SHORT).show()
                }
            }

            binding.radioGroupChallenges.addView(radioButton)
        }
    }
}