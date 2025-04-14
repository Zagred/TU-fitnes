package com.example.myapplication.challenge

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R

class ChallengeAnimationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_challenge_animation)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get the earned points from the intent
        val earnedPoints = intent.getIntExtra("EARNED_POINTS", 0)

        // Find views
        val imgDumbbell = findViewById<ImageView>(R.id.imgdumbell)
        val tvCongrats = findViewById<TextView>(R.id.tvCongrats)
        val tvPoints = findViewById<TextView>(R.id.tvPoints)
        val tvPointsLabel = findViewById<TextView>(R.id.tvPointsLabel)
        val btnContinue = findViewById<Button>(R.id.btnContinue)

        // Set initial text
        tvCongrats.text = "Challenge Completed!"
        tvPoints.text = "0"
        tvPointsLabel.text = "YOU JUST EARNED"

        // Update dumbbell count in SharedPreferences
        updateDumbbellCounter(earnedPoints)

        // Set click listener for Continue button
        btnContinue.setOnClickListener {
            finish()
        }

        // Start animations sequence
        startAnimationSequence(imgDumbbell, tvPoints, earnedPoints)
    }

    private fun startAnimationSequence(imgDumbbell: ImageView, tvPoints: TextView, earnedPoints: Int) {
        // First make dumbbell appear with a scale animation
        val initialScaleX = ObjectAnimator.ofFloat(imgDumbbell, "scaleX", 0f, 1f)
        val initialScaleY = ObjectAnimator.ofFloat(imgDumbbell, "scaleY", 0f, 1f)

        // Create initial scale set
        val initialScale = AnimatorSet()
        initialScale.playTogether(initialScaleX, initialScaleY)
        initialScale.duration = 500
        initialScale.interpolator = OvershootInterpolator()

        // Create the shaking animation
        val shakeAnimation = ObjectAnimator.ofFloat(imgDumbbell, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        shakeAnimation.duration = 1000

        // Create pulse animation
        val scaleUpX = ObjectAnimator.ofFloat(imgDumbbell, "scaleX", 1f, 1.2f)
        val scaleUpY = ObjectAnimator.ofFloat(imgDumbbell, "scaleY", 1f, 1.2f)
        val scaleDownX = ObjectAnimator.ofFloat(imgDumbbell, "scaleX", 1.2f, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(imgDumbbell, "scaleY", 1.2f, 1f)

        scaleUpX.duration = 400
        scaleUpY.duration = 400
        scaleDownX.duration = 400
        scaleDownY.duration = 400

        val scaleUp = AnimatorSet()
        scaleUp.playTogether(scaleUpX, scaleUpY)

        val scaleDown = AnimatorSet()
        scaleDown.playTogether(scaleDownX, scaleDownY)

        // First do the initial scale
        initialScale.start()

        // After initial scale, do shake and point counting
        Handler(Looper.getMainLooper()).postDelayed({
            // Shake animation
            shakeAnimation.start()

            // After shake starts, begin counting points
            Handler(Looper.getMainLooper()).postDelayed({
                animatePointsCounter(tvPoints, 0, earnedPoints)
            }, 300)

            // After shake completes, do pulse animation
            Handler(Looper.getMainLooper()).postDelayed({
                val pulseSet = AnimatorSet()
                pulseSet.playSequentially(scaleUp, scaleDown)
                pulseSet.start()
            }, 1000)
        }, 500)
    }

    private fun animatePointsCounter(textView: TextView, startValue: Int, endValue: Int) {
        val animator = ValueAnimator.ofInt(startValue, endValue)
        animator.duration = 1200
        animator.interpolator = AccelerateDecelerateInterpolator()

        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            textView.text = animatedValue.toString()

            // Optional: scale the text slightly with each update for extra emphasis
            val scaleAnimation = AnimatorSet()
            val scaleX = ObjectAnimator.ofFloat(textView, "scaleX", 1f, 1.2f, 1f)
            val scaleY = ObjectAnimator.ofFloat(textView, "scaleY", 1f, 1.2f, 1f)
            scaleAnimation.playTogether(scaleX, scaleY)
            scaleAnimation.duration = 150
            scaleAnimation.start()
        }

        animator.start()
    }

    private fun updateDumbbellCounter(pointsToAdd: Int) {
        // Get the current dumbbell count from SharedPreferences
        val sharedPref = getSharedPreferences("fitness_app_prefs", Context.MODE_PRIVATE)
        val currentDumbbells = sharedPref.getInt("dumbbell_count", 0)

        // Add the new points to the current count
        val newDumbbellCount = currentDumbbells + pointsToAdd

        // Save the updated count back to SharedPreferences
        with(sharedPref.edit()) {
            putInt("dumbbell_count", newDumbbellCount)
            apply()
        }
    }
}