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
        val calculaterl=findViewById<Button>(R.id.button9)
        val calendar=findViewById<Button>(R.id.button3)

        calculaterl.setOnClickListener{
            val intent= Intent(this, CalculatorPage::class.java)
            startActivity(intent)
        }
        calendar.setOnClickListener {
            val intent=Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }
    }
}