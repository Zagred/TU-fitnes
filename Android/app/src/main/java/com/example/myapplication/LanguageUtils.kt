package com.example.myapplication.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LanguageUtils {

    fun setAppLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        context.resources.updateConfiguration(
            config,
            context.resources.displayMetrics
        )

        // Save language preference
        val sharedPref = context.getSharedPreferences("LanguageSettings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("app_language", languageCode)
            apply()
        }
    }

    fun loadSavedLanguage(context: Context) {
        val sharedPref = context.getSharedPreferences("LanguageSettings", Context.MODE_PRIVATE)
        val language = sharedPref.getString("app_language", "en") // Default to English
        language?.let {
            setAppLanguage(context, it)
        }
    }
}