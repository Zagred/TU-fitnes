package com.example.myapplication.datamanager.user

import android.content.Context
import com.example.myapplication.datamanager.LoggedUser

object UserScoreManager {

    // Get dumbbell count for a specific user
    fun getDumbbellCount(context: Context, username: String): Int {
        val sharedPref = context.getSharedPreferences("user_${username}_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("dumbbell_count", 0)
    }

    // Get dumbbell count for the currently logged in user
    fun getCurrentUserDumbbellCount(context: Context): Int {
        val username = LoggedUser.getUsername() ?: return 0
        return getDumbbellCount(context, username)
    }

    // Add dumbbells to a user's count
    fun addDumbbells(context: Context, username: String, amount: Int) {
        val currentCount = getDumbbellCount(context, username)
        val newCount = currentCount + amount

        val sharedPref = context.getSharedPreferences("user_${username}_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("dumbbell_count", newCount)
            apply()
        }

        // Also update the app-wide preference for backward compatibility
        if (username == LoggedUser.getUsername()) {
            val appPref = context.getSharedPreferences("fitness_app_prefs", Context.MODE_PRIVATE)
            with(appPref.edit()) {
                putInt("dumbbell_count", newCount)
                apply()
            }
        }
    }

    // Add dumbbells to the current user's count
    fun addDumbbellsToCurrentUser(context: Context, amount: Int) {
        val username = LoggedUser.getUsername() ?: return
        addDumbbells(context, username, amount)
    }
}