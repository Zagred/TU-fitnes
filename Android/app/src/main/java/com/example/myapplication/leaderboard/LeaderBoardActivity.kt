package com.example.myapplication.leaderboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.myapplication.HomePage
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityLeaderBoardBinding
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.LoggedUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myapplication.datamanager.user.UserScoreManager
import android.view.View
import android.widget.ImageView


class LeaderBoardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderBoardBinding
    private val userScores = mutableListOf<UserScore>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up home button click listener
        binding.btHome.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
            finish()
        }

        // Load leaderboard data
        loadLeaderboardData()
    }

    private fun loadLeaderboardData() {
        CoroutineScope(Dispatchers.IO).launch {
            // Get all users from the database
            val userDao = AppDatabase.getInstance(this@LeaderBoardActivity).userDAO()
            val users = userDao.getAll()

            userScores.clear()

            // For each user, get their dumbbell count
            users.forEach { user ->
                val dumbbellCount = UserScoreManager.getDumbbellCount(this@LeaderBoardActivity, user.username)
                userScores.add(UserScore(user.username, dumbbellCount))
            }

            // Sort users by dumbbell count (descending)
            userScores.sortByDescending { it.score }

            // Update UI on main thread
            withContext(Dispatchers.Main) {
                displayLeaderboard()
            }
        }
    }

    private fun getUserDumbbellCount(username: String): Int {
        // Get the specific SharedPreferences file for this user
        val sharedPref = getSharedPreferences("user_${username}_prefs", MODE_PRIVATE)
        return sharedPref.getInt("dumbbell_count", 0)
    }

    private fun displayLeaderboard() {
        // Get the GridLayout from our ScrollView
        val gridLayout = (binding.root.findViewById<LinearLayout>(R.id.leaderboard_grid)
            ?: LinearLayout(this))

        // Clear previous entries
        gridLayout.removeAllViews()

        // Create entries for each user
        userScores.forEachIndexed { index, userScore ->
            val rankingView = LayoutInflater.from(this).inflate(
                R.layout.item_leaderboard_entry, gridLayout, false
            )

            // Set user ranking
            rankingView.findViewById<TextView>(R.id.tvRank).text = "${index + 1}"

            // Set username
            rankingView.findViewById<TextView>(R.id.tvUsername).text = userScore.username

            // Set score
            rankingView.findViewById<TextView>(R.id.tvPoints).text = "${userScore.score}"

            // Highlight current user
            if (userScore.username == LoggedUser.getUsername()) {
                rankingView.findViewById<CardView>(R.id.cardViewRanking).setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.yellow)
                )
            }

            // Add to the grid
            gridLayout.addView(rankingView)
        }
    }

    // Data class to hold user score information
    data class UserScore(val username: String, val score: Int)
}