package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {
    private const val LANGUAGE_PREF = "language_preference"
    private const val SELECTED_LANGUAGE = "selected_language"

    // Save selected language to preferences
    fun setLocale(context: Context, language: String) {
        val preferences = getPreferences(context)
        val editor = preferences.edit()
        editor.putString(SELECTED_LANGUAGE, language)
        editor.apply()

        // Apply the language
        updateResources(context, language)
    }

    // Get the saved language from preferences
    fun getLanguage(context: Context): String {
        val preferences = getPreferences(context)
        return preferences.getString(SELECTED_LANGUAGE, "bg") ?: "bg" // Default is Bulgarian
    }

    // Update resources with selected language
// Update resources with selected language
    fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
        } else {
            configuration.locale = locale
        }

        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        return context
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(LANGUAGE_PREF, Context.MODE_PRIVATE)
    }
}