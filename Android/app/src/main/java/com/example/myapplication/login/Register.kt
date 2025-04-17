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
import com.example.myapplication.BaseActivity
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


class Register : BaseActivity() {
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
        val email = findViewById<EditText>(R.id.tEmailRegister).text.toString()
        val username = findViewById<EditText>(R.id.tUsernameRegister).text.toString()
        val password = findViewById<EditText>(R.id.tPassRegister).text.toString()
        val confirmPassword = findViewById<EditText>(R.id.tPassConfirmRegister).text.toString()

        // Validate all fields
        if (email.isBlank() || username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(this, getString(R.string.toast_empty_fields), Toast.LENGTH_SHORT).show()
            return
        }

        // Check if passwords match
        if (password != confirmPassword) {
            Toast.makeText(this, getString(R.string.toast_passwords_mismatch), Toast.LENGTH_SHORT).show()
            return
        }

        if (!isPasswordStrong(password)) {
            Toast.makeText(
                this,
                getString(R.string.toast_password_requirements),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (!isEmailValid(email)) {
            Toast.makeText(
                this,
                getString(R.string.toast_invalid_email),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Check if username or email already exists
                val existingUserByUsername = userDAO.findByUsername(username)
                if (existingUserByUsername != null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Register, getString(R.string.toast_username_taken), Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val existingUserByEmail = userDAO.findByEmail(email)
                if (existingUserByEmail != null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Register, getString(R.string.toast_email_registered), Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // Insert the user with their actual username (not hardcoded)
                userDAO.insert(User(0, username = username, password = password, email = email))

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Register, getString(R.string.toast_register_success), Toast.LENGTH_SHORT).show()
                }

                val id = userDAO.findByUsername(username)

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
                    Toast.makeText(this@Register, getString(R.string.toast_register_fail, e.message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}