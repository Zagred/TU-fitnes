package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.achievement.AchievementActivity
import com.example.myapplication.calculator.CalculatorPage
import com.example.myapplication.coach.AdminCoachesActivity
import com.example.myapplication.coach.UserCoachesActivity
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.location.AdminLocationsActivity
import com.example.myapplication.location.UserLocationsActivity
import com.example.myapplication.mealplan.MealPlanPage
import com.example.myapplication.social.FriendsActivity
import com.example.myapplication.social.PostActivity
import com.example.myapplication.workout.MainWorkout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast
import com.example.myapplication.challenge.ChallengeActivity
import com.example.myapplication.datamanager.quotes.QuotesManager
import com.example.myapplication.leaderboard.LeaderBoardActivity
import de.hdodenhof.circleimageview.CircleImageView

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        val profile = findViewById<CircleImageView>(R.id.btProfileHomeP)
        val meals = findViewById<Button>(R.id.btMealsHomeP)
        val calendar = findViewById<Button>(R.id.btCalendarHomeP)
        val workout = findViewById<Button>(R.id.btWorkoutHomeP)
        val challenge = findViewById<Button>(R.id.btChallengeHomeP)
        val trainer = findViewById<Button>(R.id.btTrainerHomeP)
        val friends = findViewById<Button>(R.id.btFriendsHomeP)
        val community = findViewById<Button>(R.id.btCommunityHomeP)
        val calculator = findViewById<Button>(R.id.btCalculatorHomeP)
        val locations = findViewById<Button>(R.id.btLocationsHomeP)
        val leaderboard = findViewById<Button>(R.id.btLeaderBoardP)

        displayRandomMotivationalQuote()

        val userId = intent.getIntExtra("USER_ID", -1)
        loadUserAvatar(userId, profile)

        leaderboard.setOnClickListener {
            val intent = Intent(this, LeaderBoardActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
        profile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        calendar.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        workout.setOnClickListener{
            val intent = Intent(this, MainWorkout::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        friends.setOnClickListener {
            val intent = Intent(this, FriendsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        community.setOnClickListener {
            val intent = Intent(this, PostActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        calculator.setOnClickListener{
            val intent = Intent(this, CalculatorPage::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        meals.setOnClickListener{
            val intent = Intent(this, MealPlanPage::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        challenge.setOnClickListener{
            val intent = Intent(this, ChallengeActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        trainer.setOnClickListener {
            val userDAO = AppDatabase.getInstance(applicationContext).userDAO()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val user = userDAO.getUserById(userId)

                    withContext(Dispatchers.Main) {
                        val intent = if (user?.role == "admin") {
                            Intent(this@HomePage, AdminCoachesActivity::class.java)
                        } else {
                            Intent(this@HomePage, UserCoachesActivity::class.java)
                        }
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@HomePage,
                            "Error accessing coaches: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        locations.setOnClickListener {
            val userDAO = AppDatabase.getInstance(applicationContext).userDAO()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val user = userDAO.getUserById(userId)

                    withContext(Dispatchers.Main) {
                        val intent = if (user?.role == "admin") {
                            Intent(this@HomePage, AdminLocationsActivity::class.java)
                        } else {
                            Intent(this@HomePage, UserLocationsActivity::class.java)
                        }
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@HomePage,
                            "Error accessing locations: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        displayRandomMotivationalQuote()
        val profileImageView = findViewById<CircleImageView>(R.id.btProfileHomeP)
        val userId = intent.getIntExtra("USER_ID", -1)
        loadUserAvatar(userId, profileImageView)
    }

    private fun displayRandomMotivationalQuote() {
        val tvQuote = findViewById<TextView>(R.id.tvMotivationalQuote)
        val quote = QuotesManager.getRandomQuote()

        val words = quote.split(" ")
        val firstWordEndIndex = quote.indexOf(words.first()) + words.first().length
        val lastWordStartIndex = quote.lastIndexOf(words.last())

        val spannableString = SpannableString(quote)

        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#FCCF1F")),
            0,
            firstWordEndIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            ForegroundColorSpan(Color.BLACK),
            firstWordEndIndex,
            lastWordStartIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#FCCF1F")),
            lastWordStartIndex,
            quote.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvQuote.text = spannableString
    }

    private fun loadUserAvatar(userId: Int, imageView: CircleImageView) {
        if (userId == -1) return

        val db = AppDatabase.getInstance(applicationContext)
        val userInfoDAO = db.userInfoDAO()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userInfo = userInfoDAO.getUserInfoByUserId(userId)

                withContext(Dispatchers.Main) {
                    userInfo?.let {
                        try {
                            val resourceId = resources.getIdentifier(it.avatar, "drawable", packageName)
                            if (resourceId != 0) {
                                imageView.setImageResource(resourceId)
                            } else {
                                // Fallback to default avatar
                                imageView.setImageResource(R.drawable.panda)
                            }
                        } catch (e: Exception) {
                            // If there's an error, use default avatar
                            imageView.setImageResource(R.drawable.panda)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@HomePage,
                        "Error loading avatar: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}