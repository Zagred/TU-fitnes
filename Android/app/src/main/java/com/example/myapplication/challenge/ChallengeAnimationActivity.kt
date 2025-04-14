package com.example.myapplication.challenge

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.CycleInterpolator
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

        // Find the dumbbell image
        val imgDumbbell = findViewById<ImageView>(R.id.imgdumbell)

        // Find text views to display messages (if they exist in your layout)
        val tvCongrats = findViewById<TextView?>(R.id.tvCongrats)
        val tvPoints = findViewById<TextView?>(R.id.tvPoints)

        // Update text views if they exist
        tvCongrats?.text = "Challenge Completed!"
        tvPoints?.text = "You earned $earnedPoints dumbbells!"

        // Start the dumbbell shaking animation
        startShakingAnimation(imgDumbbell)

        // Auto close after animation completes
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 3000) // 3 seconds
    }

    private fun startShakingAnimation(view: ImageView) {
        // Create a shaking animation
        val shakeAnimation = ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        shakeAnimation.duration = 1000

        // Create a scale up and down animation
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f)
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.2f, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.2f, 1f)

        scaleUpX.duration = 500
        scaleUpY.duration = 500
        scaleDownX.duration = 500
        scaleDownY.duration = 500

        // Create animation set for scaling
        val scaleUp = AnimatorSet()
        scaleUp.playTogether(scaleUpX, scaleUpY)

        val scaleDown = AnimatorSet()
        scaleDown.playTogether(scaleDownX, scaleDownY)

        // Play all animations in sequence
        val fullAnimation = AnimatorSet()
        fullAnimation.playSequentially(shakeAnimation, scaleUp, scaleDown)

        // Start the animation
        fullAnimation.start()
    }
}