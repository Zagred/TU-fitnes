package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.utils.LanguageUtils

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        LanguageUtils.loadSavedLanguage(newBase)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageUtils.loadSavedLanguage(this)
    }
}