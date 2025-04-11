package com.example.myapplication.login

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.custom.CustomWorkout
import com.example.myapplication.datamanager.custom.CustomWorkoutDAO
import com.example.myapplication.datamanager.user.NutritionInfo
import com.example.myapplication.datamanager.user.NutritionInfoDAO
import com.example.myapplication.datamanager.user.User
import com.example.myapplication.datamanager.user.UserDAO
import com.example.myapplication.datamanager.user.UserInfo
import com.example.myapplication.datamanager.user.UserInfoDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern


class Register : AppCompatActivity() {
    private lateinit var userDAO: UserDAO
    private lateinit var userInfoDAO: UserInfoDAO
    private lateinit var nutritionInfo: NutritionInfoDAO
    private lateinit var workout: CustomWorkoutDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val submitg = findViewById<Button>(R.id.btSubmit)
        val db = AppDatabase.getInstance(applicationContext)
        userDAO = db.userDAO()
        userInfoDAO = db.userInfoDAO()
        nutritionInfo = db.nutritionInfoDAO()
        workout = db.customWorkoutDAO()

        submitg.setOnClickListener {
            insert()
        }
        val loginText = findViewById<Button>(R.id.loginRegister)

        loginText.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }


    private fun isPasswordStrong(password: String): Boolean {

        val passwordPattern = Pattern.compile(
            "^(?=.*[0-9])" +       // at least 1 number
                    "(?=.*[a-z])" +         // at least 1 lower case letter
                    "(?=.*[A-Z])" +         // at least 1 upper case letter
                    "(?=.*[!@#$%^&*()_+])" + // at least 1 special character
                    "(?=\\S+$).{8,}$"       // at least 8 characters, no whitespace
        )
        return passwordPattern.matcher(password).matches()
    }

    private fun isEmailValid(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9_-]+@[a-z-]+\\.[a-z]+$"
        )
        return emailPattern.matcher(email).matches()
    }

    private fun insert() {
        val email = findViewById<EditText>(R.id.tNameRegister).text.toString()
        val password = findViewById<EditText>(R.id.tPassRegister).text.toString()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isPasswordStrong(password)) {
            Toast.makeText(
                this,
                "Password must be at least 8 characters long and contain:\n" +
                        "- Uppercase letter\n" +
                        "- Lowercase letter\n" +
                        "- Number\n" +
                        "- Special character (!@#$%^&*()_+)",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (!isEmailValid(email)) {
            Toast.makeText(
                this,
                "Please enter a valid email address!\n",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Check if username already exists
                val existingUser = userDAO.findByUsername(email)
                if (existingUser != null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Register, "Email is already registered", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                userDAO.insert(User(0, username = "user", password = password, email = email))

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Register, "User registered successfully!", Toast.LENGTH_SHORT).show()
                }

                val id = userDAO.findByUsername(email)

                if (id != null) {
                    workout.insert(CustomWorkout(0, id.uid, "first workout", 0))
                    userInfoDAO.insert(UserInfo(0, birthdate = "", gender = "", height = 0, weight = 0.0, id.uid))
                    nutritionInfo.insert(NutritionInfo(0, caloriesPerDay = 0, carbsPerDay = 1, proteinPerDay = 0, fatPerDay = 0, id.uid))
                }

                withContext(Dispatchers.Main) {
                    val intent = Intent(this@Register, Login::class.java)
                    startActivity(intent)
                    finish()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Register, "Failed to register user", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}