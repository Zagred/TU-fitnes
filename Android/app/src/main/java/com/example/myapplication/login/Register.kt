package com.example.myapplication.login

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.Workout
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

class Register : AppCompatActivity() {
    private lateinit var userDAO:UserDAO
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
        userInfoDAO=db.userInfoDAO()
        nutritionInfo=db.nutritionInfoDAO()
        workout=db.customWorckoutDAO()
        submitg.setOnClickListener {
            insert()
        }
    }
    private fun insert() {
        val username = findViewById<EditText>(R.id.tNameRegister).text.toString()
        val password = findViewById<EditText>(R.id.tPassRegister).text.toString()

        if (username.isBlank() || password.isBlank()) {
            runOnUiThread {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                userDAO.insert(User(0, username = username, password = password))

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Register, "User registered successfully!", Toast.LENGTH_SHORT).show()
                }
                val id=userDAO.findByUsername(username=username)
                userInfoDAO.insert(UserInfo(0, birthdate = "", gender = "", height = 0, weight =0.0 ,id.uid))
                nutritionInfo.insert(NutritionInfo(0, caloriesPerDay = 0, carbsPerDay = 1, proteinPerDay = 0, fatPerDay = 0,id.uid))
                workout.insert(CustomWorkout(0,id.uid,0,0))

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Register, "Failed to register user", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}