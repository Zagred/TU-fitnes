package com.example.myapplication.datamanager.user

import android.content.Context
import com.example.myapplication.datamanager.LoggedUser

object UserChallengeManager {

    // Check if a challenge is completed for a specific user
    fun isChallengeCompleted(context: Context, username: String, challengeId: Int): Boolean {
        val sharedPref = context.getSharedPreferences("user_${username}_challenges", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("challenge_$challengeId", false)
    }

    // Check if a challenge is completed for the currently logged in user
    fun isCurrentUserChallengeCompleted(context: Context, challengeId: Int): Boolean {
        val username = LoggedUser.getUsername() ?: return false
        return isChallengeCompleted(context, username, challengeId)
    }

    // Mark a challenge as completed for a specific user
    fun completeChallenge(context: Context, username: String, challengeId: Int) {
        val sharedPref = context.getSharedPreferences("user_${username}_challenges", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("challenge_$challengeId", true)
            apply()
        }
    }

    // Mark a challenge as completed for the current user
    fun completeCurrentUserChallenge(context: Context, challengeId: Int) {
        val username = LoggedUser.getUsername() ?: return
        completeChallenge(context, username, challengeId)
    }

    // Get all completed challenges for a specific user
    fun getCompletedChallenges(context: Context, username: String): List<Int> {
        val sharedPref = context.getSharedPreferences("user_${username}_challenges", Context.MODE_PRIVATE)
        val completedChallenges = mutableListOf<Int>()

        // This assumes you have a known set of challenge IDs,
        // or you could store the total number of challenges somewhere
        // For demonstration, let's check challenges with IDs 1-10
        for (id in 1..10) {
            if (sharedPref.getBoolean("challenge_$id", false)) {
                completedChallenges.add(id)
            }
        }

        return completedChallenges
    }

    // Get all completed challenges for the current user
    fun getCurrentUserCompletedChallenges(context: Context): List<Int> {
        val username = LoggedUser.getUsername() ?: return emptyList()
        return getCompletedChallenges(context, username)
    }

    // Reset a challenge for a specific user (mark as incomplete)
    fun resetChallenge(context: Context, username: String, challengeId: Int) {
        val sharedPref = context.getSharedPreferences("user_${username}_challenges", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("challenge_$challengeId", false)
            apply()
        }
    }

    // Reset a challenge for the current user
    fun resetCurrentUserChallenge(context: Context, challengeId: Int) {
        val username = LoggedUser.getUsername() ?: return
        resetChallenge(context, username, challengeId)
    }
}