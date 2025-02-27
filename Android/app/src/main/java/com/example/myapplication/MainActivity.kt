package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val userName=findViewById<EditText>(R.id.edUsername)
        val password=findViewById<EditText>(R.id.edPassword)
        val login=findViewById<Button>(R.id.btLogin)
        val register=findViewById<Button>(R.id.btRegister)

        //smenyane mejdu stranicite
        login.setOnClickListener{
            val intent=Intent(this,HomePage::class.java)
            startActivity(intent)
        }
    }
}