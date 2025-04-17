package com.example.myapplication.login

import android.content.Intent
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
import com.example.myapplication.utils.LocaleHelper


class Login : BaseActivity() {
    private lateinit var userDAO: UserDAO
    private var loginAttempts = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val login = findViewById<Button>(R.id.btLogin)
        val register = findViewById<Button>(R.id.btRegister)
        val btnBulgarian = findViewById<Button>(R.id.btBulgarian)
        val btnEnglish = findViewById<Button>(R.id.btEnglish)
        btnBulgarian.setOnClickListener {
            changeLanguage("bg")
        }

        btnEnglish.setOnClickListener {
            changeLanguage("en")
        }
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
        } catch (e: Exception) {
            Log.e("Login", "Error initializing database", e)
            Toast.makeText(
                this,
                getString(R.string.toast_db_init_error, e.message),
                Toast.LENGTH_LONG
            ).show()
        }
    }
    private fun changeLanguage(languageCode: String) {
        LocaleHelper.setLocale(this, languageCode)
        recreate()
    }
    private suspend fun login() {
        try {
            val email = findViewById<EditText>(R.id.tEmailLogin).text.toString()
            val password = findViewById<EditText>(R.id.tPassLogin).text.toString()

            if (email.isBlank() || password.isBlank()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Login,
                        getString(R.string.toast_login_empty_fields),
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
                                getString(R.string.toast_login_success),
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
                            getString(R.string.toast_login_fail, e.message),
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
                    getString(R.string.toast_login_error, e.message),
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
                    getString(R.string.toast_login_attempts),
                    Toast.LENGTH_LONG
                ).show()
                // You could implement a temporary lockout here
                // or other security measures
            } else {
                Toast.makeText(
                    this@Login,
                    getString(R.string.toast_invalid_credentials),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}