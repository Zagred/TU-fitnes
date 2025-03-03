package com.example.myapplication.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R

class CalorieCalculator : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calroie_calculator)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etAge = findViewById<EditText>(R.id.etAge)
        val radioGroup = findViewById<RadioGroup>(R.id.rgGender)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)
        val tvIndex = findViewById<TextView>(R.id.tvIndex)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val tvInfo = findViewById<TextView>(R.id.tvInfo)

        btnCalculate.setOnClickListener {
            val weight = etWeight.text.toString().toDoubleOrNull()
            val height = etHeight.text.toString().toDoubleOrNull()
            val age = etAge.text.toString().toIntOrNull()
            val selectedGenderId = radioGroup.checkedRadioButtonId

            if (weight != null && height != null && age != null && selectedGenderId != -1) {
                val isMale = findViewById<RadioButton>(selectedGenderId).text.toString() == "Male"
                val bmr = calculateBMR(weight, height, age, isMale)

                tvIndex.text = "%.2f kcal/day".format(bmr)

            } else {
                tvIndex.text = "Invalid input!"
                tvResult.text = "Error"
                tvInfo.text = "Please enter valid values."
            }
        }
    }

    private fun calculateBMR(weight: Double, height: Double, age: Int, isMale: Boolean): Double {
        return if (isMale) {
            10 * weight + 6.25 * height - 5 * age + 5
        } else {
            10 * weight + 6.25 * height - 5 * age - 161
        }
    }
}