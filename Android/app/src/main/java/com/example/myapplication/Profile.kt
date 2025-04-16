package com.example.myapplication

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.user.UserDAO
import com.example.myapplication.datamanager.user.UserInfo
import com.example.myapplication.datamanager.user.UserInfoDAO
import com.example.myapplication.login.Login
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Profile : AppCompatActivity() {
    private lateinit var infoDAO: UserInfoDAO
    private lateinit var userDAO: UserDAO
    private var userId: Int = -1
    private lateinit var birthDateTextView: TextView
    private lateinit var datePickerLayout: LinearLayout
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private lateinit var profileImageView: CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        val editButton = findViewById<Button>(R.id.btEditProfile)
        val deleteButton = findViewById<Button>(R.id.btDeleteProfile)
        profileImageView = findViewById(R.id.imageView)

        // Initialize date picker components
        birthDateTextView = findViewById(R.id.tBirthProfile)
        datePickerLayout = findViewById(R.id.datePickerLayout)
        setupDatePicker()

        // Setup avatar selection
        setupAvatarSelection()

        val db = AppDatabase.getInstance(applicationContext)
        infoDAO = db.userInfoDAO()
        userDAO = db.userDAO()

        userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            userDisplay()
        }

        editButton.setOnClickListener {
            showUpdateConfirmationDialog()
        }

        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun setupAvatarSelection() {
        profileImageView.setOnClickListener {
            showAvatarSelectionDialog()
        }
    }

    private fun showAvatarSelectionDialog() {
        // List of available avatar animals (these should match your drawable resource names)
        val avatarOptions = listOf("panda", "duck", "bear", "pig", "frog", "elephant", "monkey", "bober", "koala", "tiger")

        // Create a grid layout for the avatars
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose your avatar")

        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.avatar_selection_grid, null)
        val gridView = dialogView.findViewById<GridView>(R.id.avatarGridView)

        val adapter = AvatarAdapter(this, avatarOptions)
        gridView.adapter = adapter

        builder.setView(dialogView)
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()

        gridView.setOnItemClickListener { _, _, position, _ ->
            val selectedAvatar = avatarOptions[position]
            updateUserAvatar(selectedAvatar)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateUserAvatar(avatarName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Update the avatar in the database
            infoDAO.updateUserAvatar(userId, avatarName)

            // Update the UI on the main thread
            withContext(Dispatchers.Main) {
                // Update the image view with the new avatar
                val resourceId = resources.getIdentifier(avatarName, "drawable", packageName)
                profileImageView.setImageResource(resourceId)
                Toast.makeText(this@Profile, "Avatar updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDatePicker() {
        // Set click listener on both the TextView and parent layout
        datePickerLayout.setOnClickListener {
            showDatePickerDialog()
        }
        birthDateTextView.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateBirthDateLabel()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set a reasonable date range (e.g., no future dates)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        // Set a minimum date (120 years ago as reasonable minimum)
        val minCalendar = Calendar.getInstance()
        minCalendar.add(Calendar.YEAR, -120)
        datePickerDialog.datePicker.minDate = minCalendar.timeInMillis

        datePickerDialog.show()
    }

    private fun updateBirthDateLabel() {
        birthDateTextView.text = dateFormat.format(calendar.time)
    }

    private fun showUpdateConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Update Profile")
            .setMessage("Are you sure you want to update your profile information?")
            .setPositiveButton("Update") { _, _ ->
                lifecycleScope.launch {
                    updateUser()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private suspend fun userDisplay() {
        val userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show()
            return
        }

        val user = withContext(Dispatchers.IO) { userDAO.findById(userId) }
        val userInfo = withContext(Dispatchers.IO) { infoDAO.getUserInfoByUserId(userId) }

        // Set up gender spinner
        val genderSpinner = findViewById<Spinner>(R.id.spinnerGender)
        val genderOptions = resources.getStringArray(R.array.gender_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter

        withContext(Dispatchers.Main) {
            findViewById<EditText>(R.id.tNameProfile).setText(user?.username ?: "")

            // Display email if available
            if (user?.email != null) {
                findViewById<EditText>(R.id.tEmailProfile).setText(user.email)
            }

            userInfo?.let {
                findViewById<EditText>(R.id.tHeightProfile).setText(it.height.toString())
                findViewById<EditText>(R.id.tWeightProfile).setText(it.weight.toString())

                // Set gender spinner selection
                val genderPosition = when (it.gender.toLowerCase()) {
                    "male" -> 0
                    "female" -> 1
                    else -> 0 // Default to male if gender is not recognized
                }
                genderSpinner.setSelection(genderPosition)

                // Set birth date and update calendar
                try {
                    birthDateTextView.text = it.birthdate
                    // Parse the stored date to set the calendar
                    val date = dateFormat.parse(it.birthdate)
                    if (date != null) {
                        calendar.time = date
                    }
                } catch (e: Exception) {
                    birthDateTextView.text = ""
                }

                // Load avatar
                try {
                    val resourceId = resources.getIdentifier(it.avatar, "drawable", packageName)
                    if (resourceId != 0) {
                        profileImageView.setImageResource(resourceId)
                    } else {
                        // Fallback to default avatar if resource not found
                        profileImageView.setImageResource(R.drawable.panda)
                    }
                } catch (e: Exception) {
                    profileImageView.setImageResource(R.drawable.panda)
                }
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
        val birthdate = birthDateTextView.text.toString()

        // Get gender from spinner
        val genderSpinner = findViewById<Spinner>(R.id.spinnerGender)
        val gender = genderSpinner.selectedItem.toString()

        withContext(Dispatchers.IO) {
            userDAO.updateUserUsername(userDAO.findById(userId).username, name)

            infoDAO.update(
                UserInfo(
                    id = userInfo.id,
                    birthdate = birthdate,
                    gender = gender,
                    height = height,
                    weight = weight,
                    userId = userInfo.userId,
                    avatar = userInfo.avatar // Preserve the current avatar
                )
            )
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(this@Profile, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Profile")
            .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    deleteUserProfile()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private suspend fun deleteUserProfile() {
        withContext(Dispatchers.IO) {
            infoDAO.deleteUserInfoByUserId(userId)

            userDAO.delete(userDAO.findById(userId))
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(this@Profile, "Profile deleted successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@Profile, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}

class AvatarAdapter(
    private val context: Context,
    private val avatarList: List<String>
) : BaseAdapter() {

    override fun getCount(): Int = avatarList.size

    override fun getItem(position: Int): Any = avatarList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView: ImageView

        if (convertView == null) {
            imageView = ImageView(context)
            imageView.layoutParams = ViewGroup.LayoutParams(150, 150)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView.setPadding(8, 8, 8, 8)
        } else {
            imageView = convertView as ImageView
        }

        val resourceId = context.resources.getIdentifier(
            avatarList[position], "drawable", context.packageName
        )
        imageView.setImageResource(resourceId)

        return imageView
    }
}