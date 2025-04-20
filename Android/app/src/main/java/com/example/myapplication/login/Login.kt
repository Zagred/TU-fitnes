package com.example.myapplication.login

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.BaseActivity
import com.example.myapplication.HomePage
import com.example.myapplication.R
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.user.UserDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myapplication.datamanager.LoggedUser
import java.util.Locale

class Login : BaseActivity() {
    private lateinit var userDAO: UserDAO
    private var loginAttempts = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Load saved language setting before setting content view
        loadLocale()
        setContentView(R.layout.activity_main)

        val login = findViewById<Button>(R.id.btLogin)
        val register = findViewById<Button>(R.id.btRegister)
        val bulgarianBtn = findViewById<Button>(R.id.btBulgarian)
        val englishBtn = findViewById<Button>(R.id.btEnglish)

        try {
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

            // Add click listeners for language buttons
            bulgarianBtn.setOnClickListener {
                changeLanguage("bg")
            }

            englishBtn.setOnClickListener {
                changeLanguage("en")
            }

        } catch (e: Exception) {
            Log.e("Login", "Error initializing database", e)
            Toast.makeText(
                this,
                "Error initializing app: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Method to change the app language
    private fun changeLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.locale = locale

        baseContext.resources.updateConfiguration(
            config,
            baseContext.resources.displayMetrics
        )

        // Save language preference
        val sharedPref = getSharedPreferences("LanguageSettings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("app_language", languageCode)
            apply()
        }

        // Restart the activity to apply language changes
        recreate()
    }

    // Method to load saved language preference
    private fun loadLocale() {
        val sharedPref = getSharedPreferences("LanguageSettings", Context.MODE_PRIVATE)
        val language = sharedPref.getString("app_language", "en") // Default to English
        language?.let {
            val locale = Locale(it)
            Locale.setDefault(locale)

            val config = Configuration()
            config.locale = locale

            baseContext.resources.updateConfiguration(
                config,
                baseContext.resources.displayMetrics
            )
        }
    }

    private suspend fun login() {
        try {
            val email = findViewById<EditText>(R.id.tEmailLogin).text.toString()
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

                            LoggedUser.setUsername(user.username)

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
        } catch (e: Exception) {
            Log.e("LoginError", "Unexpected error during login", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@Login,
                    "Unexpected error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
                // You could implement a temporary lockout here
                // or other security measures
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