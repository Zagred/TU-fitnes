package com.example.myapplication.calculator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R

class CalculatorPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_calculator)
        val bmiCalculator=findViewById<Button>(R.id.btnCalculatorBmi)

        bmiCalculator.setOnClickListener{
            val intent= Intent(this, BMICalculator::class.java)
            startActivity(intent)
        }
    }
}