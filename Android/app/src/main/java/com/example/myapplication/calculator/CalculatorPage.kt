package com.example.myapplication.calculator

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

class CalculatorPage : AppCompatActivity() {
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_calculator)
        val bmiCalculator=findViewById<TextView>(R.id.btnCalculatorBmi)
        val calorieCalculator = findViewById<TextView>(R.id.btnCalculatorCalorie)
        userId = intent.getIntExtra("USER_ID", -1)

        bmiCalculator.setOnClickListener{
            val intent= Intent(this, BMICalculator::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
        calorieCalculator.setOnClickListener{
            val intent= Intent(this, CalorieCalculator::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
        val home=findViewById<Button>(R.id.btHome)
        home.setOnClickListener{
            val intent = Intent(this, HomePage::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
            finish()
        }
    }
}