package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.user.UserDAO
import com.example.myapplication.datamanager.user.UserInfo
import com.example.myapplication.datamanager.user.UserInfoDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Profile : AppCompatActivity() {
    private lateinit var infoDAO: UserInfoDAO
    private lateinit var userDAO: UserDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val editButton = findViewById<Button>(R.id.btEditProfile)

        val db = AppDatabase.getInstance(applicationContext)
        infoDAO = db.userInfoDAO()
        userDAO = db.userDAO()

        lifecycleScope.launch {
            userDisplay()
        }

        editButton.setOnClickListener {
            lifecycleScope.launch {
                updateUser()
            }
        }
    }

    private suspend fun userDisplay() {
        val userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show()
            return
        }

        val user = withContext(Dispatchers.IO) { userDAO.findById(userId) }
        val userInfo = withContext(Dispatchers.IO) { infoDAO.getUserInfoByUserId(userId) }

        withContext(Dispatchers.Main) {
            findViewById<EditText>(R.id.tNameProfile).setText(user?.username ?: "")
            userInfo?.let {
                findViewById<EditText>(R.id.tHeightProfile).setText(it.height.toString())
                findViewById<EditText>(R.id.tWeightProfile).setText(it.weight.toString())
                findViewById<EditText>(R.id.tGenderProfile).setText(it.gender)
                findViewById<EditText>(R.id.tBirthProfile).setText(it.birthdate)
            }
        }
    }

    private suspend fun updateUser() {
        val userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val userInfo = withContext(Dispatchers.IO) { infoDAO.getUserInfoByUserId(userId) }

        if (userInfo == null) {
            Toast.makeText(this, "User info not found", Toast.LENGTH_SHORT).show()
            return
        }

        val name = findViewById<EditText>(R.id.tNameProfile).text.toString()
        val phone = findViewById<EditText>(R.id.tPhoneProfile).text.toString()
        val height = findViewById<EditText>(R.id.tHeightProfile).text.toString().toIntOrNull() ?: 0
        val weight = findViewById<EditText>(R.id.tWeightProfile).text.toString().toDoubleOrNull() ?: 0.0
        val birthdate = findViewById<EditText>(R.id.tBirthProfile).text.toString()
        val gender = findViewById<EditText>(R.id.tGenderProfile).text.toString()

        withContext(Dispatchers.IO) {

            userDAO.updateUserUsername(userDAO.findById(userId).username, name)

            infoDAO.update(
                UserInfo(
                    id = userInfo.id,
                    birthdate = birthdate,
                    gender = gender,
                    height = height,
                    weight = weight,
                    userId = userInfo.userId
                )
            )
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(this@Profile, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        }
    }
}