package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.calculator.BMICalculator
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.user.UserDAO
import com.example.myapplication.login.Login
import com.example.myapplication.login.Register
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {private lateinit var userDAO: UserDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val login = findViewById<Button>(R.id.btLogin)
        val register = findViewById<Button>(R.id.btRegister)

        val db = AppDatabase.getInstance(applicationContext)
        userDAO = db.userDAO()

        login.setOnClickListener {
            lifecycleScope.launch {
                val success = login()
                if (success) {
                    val intent = Intent(this@MainActivity, HomePage::class.java)
                    startActivity(intent)
                }
            }
        }
        register.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }

    private suspend fun login(): Boolean {
        val username = findViewById<EditText>(R.id.tNameLogin).text.toString()
        val password = findViewById<EditText>(R.id.tPassLogin).text.toString()
        if (username.isBlank() || password.isBlank()) {
            withContext(Dispatchers.IO) {
                Toast.makeText(this@MainActivity, "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
            }
            return false
        }
        return withContext(Dispatchers.IO) {
            try {
                val name = userDAO.findByUsername(username = username)?:return@withContext false
                if (name.password == password) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "User logged successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                }else{
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Invalid username or password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to login user",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                false
            }
        }
    }
}