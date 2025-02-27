package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        val calculaterl=findViewById<Button>(R.id.button9)
        val calendar=findViewById<Button>(R.id.button3)

        calculaterl.setOnClickListener{
            val intent= Intent(this,Calculator::class.java)
            startActivity(intent)
        }
        calendar.setOnClickListener {
            val intent=Intent(this,Calendar::class.java)
            startActivity(intent)
        }
    }
}