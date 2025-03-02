package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.calculator.CalculatorPage

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


        profile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            intent.putExtra("USER_ID", getIntent().getIntExtra("USER_ID", -1))
            startActivity(intent)
        }
        /*meals*/
        calendar.setOnClickListener {
            val intent=Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }
        workout.setOnClickListener{
            val intent= Intent(this, Workout::class.java)
            startActivity(intent)
        }
        /*trainer*/
        /*friends*/
        /*community*/
        calculator.setOnClickListener{
            val intent= Intent(this, CalculatorPage::class.java)
            startActivity(intent)
        }
    }
}