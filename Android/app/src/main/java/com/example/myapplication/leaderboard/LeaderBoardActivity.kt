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
import android.widget.Button
import android.widget.ImageView


class LeaderBoardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderBoardBinding
    private val userScores = mutableListOf<UserScore>()
    private val friendUsernames = mutableListOf<String>()
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("USER_ID", -1)

        val home=findViewById<Button>(R.id.btHome)
        home.setOnClickListener{
            val intent = Intent(this, HomePage::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
            finish()
        }

        loadLeaderboardData()
    }

    private fun loadLeaderboardData() {
        CoroutineScope(Dispatchers.IO).launch {
            val userDao = AppDatabase.getInstance(this@LeaderBoardActivity).userDAO()
            val users = userDao.getAll()

            userScores.clear()

            val loggedUsername = LoggedUser.getUsername()
            val loggedUser = loggedUsername?.let { userDao.findByUsername(it) }

            if (loggedUser != null) {
                val friendsDao = AppDatabase.getInstance(this@LeaderBoardActivity).friendsDAO()
                friendUsernames.clear()
                friendUsernames.addAll(friendsDao.getFriendsNames(loggedUser.uid))
            }

            users.forEach { user ->
                val dumbbellCount = UserScoreManager.getDumbbellCount(this@LeaderBoardActivity, user.username)
                userScores.add(UserScore(user.username, dumbbellCount))
            }

            userScores.sortByDescending { it.score }

            withContext(Dispatchers.Main) {
                displayLeaderboard()
            }
        }
    }

    private fun getUserDumbbellCount(username: String): Int {
        val sharedPref = getSharedPreferences("user_${username}_prefs", MODE_PRIVATE)
        return sharedPref.getInt("dumbbell_count", 0)
    }

    private fun displayLeaderboard() {
        val gridLayout = (binding.root.findViewById<LinearLayout>(R.id.leaderboard_grid)
            ?: LinearLayout(this))

        gridLayout.removeAllViews()

        userScores.forEachIndexed { index, userScore ->
            val rankingView = LayoutInflater.from(this).inflate(
                R.layout.item_leaderboard_entry, gridLayout, false
            )

            rankingView.findViewById<TextView>(R.id.tvRank).text = "${index + 1}"

            rankingView.findViewById<TextView>(R.id.tvUsername).text = userScore.username

            rankingView.findViewById<TextView>(R.id.tvPoints).text = "${userScore.score}"

            val friendIcon = rankingView.findViewById<ImageView>(R.id.FriendAsset)

            if (friendUsernames.contains(userScore.username)) {
                friendIcon.visibility = View.VISIBLE
                friendIcon.setImageResource(R.drawable.friends)
            } else {
                friendIcon.visibility = View.GONE
            }

            if (userScore.username == LoggedUser.getUsername()) {
                rankingView.findViewById<CardView>(R.id.cardViewRanking).setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.yellow)
                )
            }

            gridLayout.addView(rankingView)
        }
    }

    data class UserScore(val username: String, val score: Int)
}