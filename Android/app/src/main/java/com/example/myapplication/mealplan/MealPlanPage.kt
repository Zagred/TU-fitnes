package com.example.myapplication.mealplan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.HomePage
import com.example.myapplication.R
import com.example.myapplication.calculator.BMICalculator
import com.example.myapplication.calculator.CalorieCalculator

class MealPlanPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_plan_page)
        val mealPlanBtn = findViewById<TextView>(R.id.btnMealPlan)
        val checkCaloriesBtn = findViewById<TextView>(R.id.btnCheckCalories)
        mealPlanBtn.setOnClickListener {
            val intent = Intent(this, MealPlan::class.java)
            intent.putExtra("USER_ID", getIntent().getIntExtra("USER_ID", -1))
            startActivity(intent)
        }
        checkCaloriesBtn.setOnClickListener {
            val intent = Intent(this, CheckCalories::class.java)
            startActivity(intent)
        }
        val home=findViewById<Button>(R.id.btHome)
        home.setOnClickListener{
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
            finish()
        }
    }
}