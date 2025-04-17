package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.utils.LocaleHelper

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val language = LocaleHelper.getLanguage(newBase)
        val context = LocaleHelper.updateResources(newBase, language)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val language = LocaleHelper.getLanguage(this)
        LocaleHelper.updateResources(this, language)
    }
}