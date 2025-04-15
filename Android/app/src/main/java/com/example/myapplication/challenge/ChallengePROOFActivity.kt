package com.example.myapplication.challenge

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityChallengeProofactivityBinding
import com.example.myapplication.datamanager.AppDatabase
import com.example.myapplication.datamanager.user.UserScoreManager
import com.example.myapplication.datamanager.user.UserChallengeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChallengePROOFActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChallengeProofactivityBinding
    private lateinit var challengeDao: ChallengeDAO
    private var challengeId: Int = -1
    private var challenge: Challenge? = null

    private var isBeforePictureTaken = false
    private var isAfterPictureTaken = false
    private var isReadyToComplete = false

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) launchCamera(currentCameraAction)
        else showToast("Camera permission is required to take pictures")
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                when (currentCameraAction) {
                    CameraAction.TAKE_BEFORE -> {
                        binding.imgBefore.setImageBitmap(imageBitmap)
                        isBeforePictureTaken = true
                        showToast("Before picture taken")
                        checkAllRequirementsMet()
                    }
                    CameraAction.TAKE_AFTER -> {
                        binding.imgAfter.setImageBitmap(imageBitmap)
                        isAfterPictureTaken = true
                        showToast("After picture taken")
                        checkAllRequirementsMet()
                    }
                    else -> {}
                }
            }
        }
    }

    private enum class CameraAction { NONE, TAKE_BEFORE, TAKE_AFTER }
    private var currentCameraAction = CameraAction.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallengeProofactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        challengeDao = AppDatabase.getInstance(this).challengeDAO()
        challengeId = intent.getIntExtra("challenge_id", -1)

        if (challengeId == -1) {
            showToast("Database error!")
            finish()
            return
        }

        loadChallengeDetails()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnTakeBefore.setOnClickListener {
            currentCameraAction = CameraAction.TAKE_BEFORE
            checkCameraPermissionAndLaunch()
        }

        binding.btnTakeAfter.setOnClickListener {
            currentCameraAction = CameraAction.TAKE_AFTER
            checkCameraPermissionAndLaunch()
        }

        binding.btnSubmitExperience.setOnClickListener {
            if (!isExperienceValid()) {
                showToast("Please enter your experience")
            } else {
                showToast("Experience submitted!")
                checkAllRequirementsMet()
            }
        }

        binding.btnComplete.setOnClickListener {
            if (!isBeforePictureTaken) {
                showToast("Please take the BEFORE photo")
            } else if (!isAfterPictureTaken) {
                showToast("Please take the AFTER photo")
            } else if (!isExperienceValid()) {
                showToast("Please enter your experience")
            } else {
                // If all requirements are met, proceed with challenge completion
                completeChallenge()
            }
        }
    }

    // New method to check if all requirements are met
    private fun checkAllRequirementsMet() {
        if (isBeforePictureTaken && isAfterPictureTaken && isExperienceValid()) {
            // All requirements met, update button appearance and state
            binding.btnComplete.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black))
            isReadyToComplete = true
        }
    }

    private fun isExperienceValid(): Boolean {
        return !binding.etfeedback.text?.toString()?.trim().isNullOrEmpty()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> launchCamera(currentCameraAction)
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                showToast("Camera permission is needed to take pictures for challenge proof")
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera(action: CameraAction) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            takePictureLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            showToast("No camera app found")
        }
    }

    private fun loadChallengeDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            challenge = challengeDao.findById(challengeId)
            withContext(Dispatchers.Main) {
                challenge?.let {
                    binding.tvChallengeTitle.text = it.title
                    binding.tvChallengeDescription.text = it.description
                    binding.tvChallengeDifficulty.text = "Difficulty: ${it.difficulty}"
                    binding.tvChallengePoints.text = "Dumbbells: ${it.points}"
                }
            }
        }
    }

    private fun completeChallenge() {
        CoroutineScope(Dispatchers.IO).launch {
            challenge?.let {
                // Save feedback to the challenge object (if you still need this)
                it.feedback = binding.etfeedback.text.toString()
                challengeDao.update(it)

                // IMPORTANT CHANGE: Mark the challenge as completed for this specific user
                // Instead of updating the database directly, use UserChallengeManager
                UserChallengeManager.completeCurrentUserChallenge(this@ChallengePROOFActivity, challengeId)

                // Add points to the current user's score

            }

            withContext(Dispatchers.Main) {
                showToast("Congratulations! You just earned ${challenge?.points} dumbbells!")

                // Pass the earned points to the animation activity
                val intent = Intent(this@ChallengePROOFActivity, ChallengeAnimationActivity::class.java).apply {
                    putExtra("EARNED_POINTS", challenge?.points ?: 0)
                }
                startActivity(intent)
                finish()
            }
        }
    }

    private fun updateDumbbellCounter(pointsToAdd: Int) {
        // Use the UserScoreManager to add dumbbells to the current user
        UserScoreManager.addDumbbellsToCurrentUser(this, pointsToAdd)
    }
}