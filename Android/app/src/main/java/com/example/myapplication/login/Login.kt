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
    private var loginAttempts = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val login = findViewById<Button>(R.id.btLogin)
        val register = findViewById<Button>(R.id.btRegister)

        val db = AppDatabase.getInstance(applicationContext)
        userDAO = db.userDAO()

        login.setOnClickListener {
            lifecycleScope.launch {
                login()
            }
        }
        register.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }

    private suspend fun login() {
        val email = findViewById<EditText>(R.id.tEmailRegister).text.toString()
        val password = findViewById<EditText>(R.id.tPassLogin).text.toString()

        if (email.isBlank() || password.isBlank()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@Login,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        withContext(Dispatchers.IO) {
            try {
                // Try to find user by email first
                var user = userDAO.findByEmail(email)

                // If not found by email, try username
                if (user == null) {
                    user = userDAO.findByUsername(email)
                }

                if (user == null) {
                    incrementLoginAttempt()
                    return@withContext
                }

                if (user.password == password) {
                    loginAttempts = 0
                    val userId = user.uid

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@Login,
                            "User logged in successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this@Login, HomePage::class.java)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    incrementLoginAttempt()
                }
            } catch (e: Exception) {
                Log.e("LoginError", "Failed to login user", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Login,
                        "Failed to login user: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private suspend fun incrementLoginAttempt() {
        loginAttempts++

        withContext(Dispatchers.Main) {
            if (loginAttempts >= 3) {
                Toast.makeText(
                    this@Login,
                    "Too many failed login attempts",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this@Login,
                    "Invalid email or password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}