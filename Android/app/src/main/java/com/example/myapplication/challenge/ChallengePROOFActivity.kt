package com.example.myapplication.challenge

import android.Manifest
import android.content.ActivityNotFoundException
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
                    }
                    CameraAction.TAKE_AFTER -> {
                        binding.imgAfter.setImageBitmap(imageBitmap)
                        isAfterPictureTaken = true
                        showToast("After picture taken")
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
                if (isBeforePictureTaken && isAfterPictureTaken) {
                    binding.btnComplete.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black))
                }
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
                binding.btnComplete.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black))

                completeChallenge()
                startActivity(Intent(this, ChallengeAnimationActivity::class.java))
                finish()
            }
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
                it.feedback = binding.etfeedback.text.toString()
                challengeDao.update(it)
                challengeDao.markChallengeAsCompleted(challengeId)
            }

            withContext(Dispatchers.Main) {
                showToast("Congratulations! You just earned ${challenge?.points} dumbbells!")
                startActivity(Intent(this@ChallengePROOFActivity, ChallengeAnimationActivity::class.java))
                finish()
            }
        }
    }
}
