package com.example.myapplication.mealplan

data class NutritionItem(
    val name: String,
    val calories: String,
    val servingSize: String,
    val fatTotal: Double,
    val fatSaturated: Double,
    val protein: String,
    val sodium: Int,
    val potassium: Int,
    val cholesterol: Int,
    val carbs: Double,
    val fiber: Double,
    val sugar: Double
)
