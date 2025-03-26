package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.achievement.AchievementActivity
import com.example.myapplication.calculator.CalculatorPage
import com.example.myapplication.datamanager.achievement.Achievement
import com.example.myapplication.mealplan.MealPlanPage
import com.example.myapplication.social.FriendsActivity
import com.example.myapplication.social.PostActivity
import com.example.myapplication.workout.MainWorkout

import android.widget.Toast
import com.example.myapplication.coach.AdminCoachesActivity
import com.example.myapplication.coach.UserCoachesActivity
import com.example.myapplication.datamanager.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        val profile=findViewById<Button>(R.id.btProfileHomeP)
        val meals=findViewById<Button>(R.id.btMealsHomeP)
        val calendar=findViewById<Button>(R.id.btCalendarHomeP)
        val workout=findViewById<Button>(R.id.btWorkoutHomeP)
        val goals=findViewById<Button>(R.id.btGoalsHomeP)
        val trainer=findViewById<Button>(R.id.btTrainerHomeP)
        val friends=findViewById<Button>(R.id.btFriendsHomeP)
        val community=findViewById<Button>(R.id.btCommunityHomeP)
        val calculator=findViewById<Button>(R.id.btCalculatorHomeP)

        val userId = getIntent().getIntExtra("USER_ID", -1)

        profile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            intent.putExtra("USER_ID", getIntent().getIntExtra("USER_ID", -1))
            startActivity(intent)
        }
        /*meals*/
        calendar.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            intent.putExtra("USER_ID", getIntent().getIntExtra("USER_ID", -1))
            startActivity(intent)
        }
        workout.setOnClickListener{
            val intent= Intent(this, MainWorkout::class.java)
            intent.putExtra("USER_ID", getIntent().getIntExtra("USER_ID", -1))

            startActivity(intent)
        }
        /*trainer*/
        /*friends*/
        friends.setOnClickListener {
            val intent = Intent(this, FriendsActivity::class.java)
            intent.putExtra("USER_ID", getIntent().getIntExtra("USER_ID", -1))
            startActivity(intent)
        }
        community.setOnClickListener {
            val intent = Intent(this, PostActivity::class.java)
            intent.putExtra("USER_ID", getIntent().getIntExtra("USER_ID", -1))
            startActivity(intent)
        }
        calculator.setOnClickListener{
            val intent= Intent(this, CalculatorPage::class.java)
            startActivity(intent)
        }
        meals.setOnClickListener{
            val intent= Intent(this, MealPlanPage::class.java)
            intent.putExtra("USER_ID", getIntent().getIntExtra("USER_ID", -1))
            startActivity(intent)
        }
        goals.setOnClickListener{
            val intent= Intent(this, AchievementActivity::class.java)
            intent.putExtra("USER_ID", getIntent().getIntExtra("USER_ID", -1))
            startActivity(intent)
        }
        trainer.setOnClickListener {
            // Check if user is an admin
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
    }
}