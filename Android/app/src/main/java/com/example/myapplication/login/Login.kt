package com.example.myapplication.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.HomePage
import com.example.myapplication.R
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.user.UserDAO
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Login : AppCompatActivity() {
    private lateinit var userDAO: UserDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val login = findViewById<Button>(R.id.btLogin)
        val register = findViewById<Button>(R.id.btRegister)

        val db = AppDatabase.getInstance(applicationContext)
        userDAO = db.userDAO()

        login.setOnClickListener {
            lifecycleScope.launch {
                Log.i("Mytag","before login")
                login()

            }
        }
        register.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }

    private suspend fun login(){
        val username = findViewById<EditText>(R.id.tNameLogin).text.toString()
        val password = findViewById<EditText>(R.id.tPassLogin).text.toString()

        if (username.isBlank() || password.isBlank()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Login,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

         withContext(Dispatchers.IO) {
            try {
                val user = userDAO.findByUsername(username = username) ?: return@withContext false
                if (user.password == password) {
                    val userId = user.uid

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@Login,
                            "User logged successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Create intent and pass the user ID
                        val intent = Intent(this@Login, HomePage::class.java)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                    }

                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@Login,
                            "Invalid username or password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            } catch (e: Exception) {
                Log.e("LoginError", "Failed to login user", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Login,
                        "Failed to login user",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }
}