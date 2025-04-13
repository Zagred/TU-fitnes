package com.example.myapplication.challenge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityChallengeBinding
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.challenge.Challenge
import com.example.myapplication.challenge.ChallengeDAO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChallengeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChallengeBinding
    private lateinit var challengeDao: ChallengeDAO
    private var selectedChallenge: Challenge? = null
    private val loadedChallenges = mutableListOf<Challenge>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallengeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        challengeDao = AppDatabase.getInstance(this).challengeDAO()

        // Load and display the dumbbell count
        loadDumbbellCount()

        loadChallenges()

        binding.btnConfirm.setOnClickListener {
            if (selectedChallenge != null) {
                val intent = Intent(this, ChallengePROOFActivity::class.java)
                intent.putExtra("challenge_id", selectedChallenge!!.id)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Моля, избери предизвикателство", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload the dumbbell count when returning to this activity
        loadDumbbellCount()
    }

    private fun loadDumbbellCount() {
        // Get the current dumbbell count from SharedPreferences
        val sharedPref = getSharedPreferences("fitness_app_prefs", Context.MODE_PRIVATE)
        val dumbbellCount = sharedPref.getInt("dumbbell_count", 0)

        // Update the TextView displaying the count
        binding.tvDumbbellCount.text = dumbbellCount.toString()
    }

    private fun loadChallenges() {
        CoroutineScope(Dispatchers.IO).launch {
            val easy = challengeDao.getRandomByDifficulty("EASY", 1)
            val medium = challengeDao.getRandomByDifficulty("MEDIUM", 1)
            val hard = challengeDao.getRandomByDifficulty("HARD", 1)

            loadedChallenges.clear()
            loadedChallenges.addAll(easy + medium + hard)

            withContext(Dispatchers.Main) {
                showRadioButtons()
            }
        }
    }

    private fun showRadioButtons() {
        binding.radioGroupChallenges.removeAllViews()

        loadedChallenges.forEach { challenge ->
            val radioButton = RadioButton(this).apply {
                text = challenge.title
                textSize = 18f
            }
            radioButton.setOnClickListener {
                selectedChallenge = challenge
                binding.btnConfirm.isEnabled = true
            }

            binding.radioGroupChallenges.addView(radioButton)
        }
    }
}