package com.example.myapplication.mealplan

import android.util.Log
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MealPlanRepository {
    suspend fun fetchMealPlan(appId: String, appKey: String, calories: Int): MealPlanResponse? {
        return withContext(Dispatchers.IO) {
            try {
                // Create a simple meal plan request
                val request = MealPlanRequest(
                    size = 1, // Just get one day
                    plan = PlanDetails(
                        fit = mapOf(
                            "ENERC_KCAL" to mapOf(
                                "min" to calories - 400,
                                "max" to calories + 400
                            )
                        ),
                        sections = mapOf(
                            "Breakfast" to SectionDetails(
                                accept = mapOf(
                                    "all" to listOf(
                                        mapOf("meal" to listOf("breakfast"))
                                    )
                                ),
                                fit = mapOf(
                                    "ENERC_KCAL" to mapOf(
                                        "min" to (calories * 0.2).toInt(),
                                        "max" to (calories * 0.3).toInt()
                                    )
                                )
                            ),
                            "Lunch" to SectionDetails(
                                accept = mapOf(
                                    "all" to listOf(
                                        mapOf("meal" to listOf("lunch/dinner"))
                                    )
                                ),
                                fit = mapOf(
                                    "ENERC_KCAL" to mapOf(
                                        "min" to (calories * 0.3).toInt(),
                                        "max" to (calories * 0.4).toInt()
                                    )
                                )
                            ),
                            "Dinner" to SectionDetails(
                                accept = mapOf(
                                    "all" to listOf(
                                        mapOf("meal" to listOf("lunch/dinner"))
                                    )
                                ),
                                fit = mapOf(
                                    "ENERC_KCAL" to mapOf(
                                        "min" to (calories * 0.3).toInt(),
                                        "max" to (calories * 0.4).toInt()
                                    )
                                )
                            )
                        )
                    )
                )

                // Create Basic auth header - this is how the example does it
                val credentials = "$appId:$appKey"
                val basic = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

                // Call the API with the correct headers
                val response = RetrofitClient.instance.getMealPlan(
                    appId = appId,
                    appKey = appKey,
                    type = "public",
                    userId = "kristianstanev03",
                    authorization = basic,
                    request = request
                )

                Log.d("MealPlanRepository", "Successfully fetched meal plan")
                response
            } catch (e: Exception) {
                Log.e("MealPlanRepository", "Error fetching meal plan: ${e.message}", e)
                null
            }
        }
    }

    // New method to fetch detailed meal plan
    suspend fun fetchDetailedMealPlan(appId: String, appKey: String, calories: Int): List<Meal> {
        return withContext(Dispatchers.IO) {
            try {
                val mealPlan = fetchMealPlan(appId, appKey, calories) ?: return@withContext emptyList()
                val basicMeals = mealPlan.extractMeals()

                Log.d("MealPlanRepository", "Basic meals extracted: ${basicMeals.size}")

                // For each basic meal, fetch detailed information
                val detailedMeals = mutableListOf<Meal>()

                for (meal in basicMeals) {
                    try {
                        Log.d("MealPlanRepository", "Fetching details for: ${meal.label}")
                        val detailedMeal = fetchRecipeDetails(meal.url, appId, appKey)
                        if (detailedMeal != null) {
                            // Preserve the meal category from the basic meal
                            detailedMeals.add(detailedMeal.copy(mealCategory = meal.mealCategory))
                        } else {
                            // Fallback to basic meal if detailed info can't be fetched
                            detailedMeals.add(meal)
                        }
                    } catch (e: Exception) {
                        Log.e("MealPlanRepository", "Error fetching recipe details: ${e.message}", e)
                        detailedMeals.add(meal) // Use basic meal as fallback
                    }
                }

                Log.d("MealPlanRepository", "Fetched ${detailedMeals.size} detailed meals")
                detailedMeals
            } catch (e: Exception) {
                Log.e("MealPlanRepository", "Error fetching detailed meal plan: ${e.message}", e)
                emptyList()
            }
        }
    }

    // Helper function to fetch detailed recipe information
    private suspend fun fetchRecipeDetails(url: String, appId: String, appKey: String): Meal? {
        return try {
            // Extract recipe ID from the URL
            val recipeId = extractRecipeIdFromUrl(url)
            if (recipeId.isNullOrEmpty()) {
                Log.e("MealPlanRepository", "Could not extract recipe ID from URL: $url")
                return null
            }

            Log.d("MealPlanRepository", "Extracting recipe ID: $recipeId from URL: $url")

            // Call the API to get detailed recipe information
            val response = RetrofitClient.instance.getRecipeDetails(
                recipeId = recipeId,
                type = "public",
                appId = appId,
                appKey = appKey,
                userId = "kristianstanev03"
            )

            // Map the response to our Meal model
            response.recipe.let { recipe ->
                Meal(
                    label = recipe.label,
                    url = recipe.url,
                    calories = recipe.calories,
                    servings = recipe.yield,
                    totalTime = recipe.totalTime,
                    ingredientLines = recipe.ingredientLines,
                    dietLabels = recipe.dietLabels,
                    healthLabels = recipe.healthLabels,
                    cuisineType = recipe.cuisineType,
                    mealType = recipe.mealType,
                    dishType = recipe.dishType,
                    imageUrl = recipe.images?.regular?.url ?: recipe.images?.small?.url
                )
            }
        } catch (e: Exception) {
            Log.e("MealPlanRepository", "Error fetching recipe details: ${e.message}", e)
            null
        }
    }

    // Helper function to extract recipe ID from URL
    private fun extractRecipeIdFromUrl(url: String): String? {
        val regex = "recipes/v2/([^?]*)".toRegex()
        val matchResult = regex.find(url)
        return matchResult?.groupValues?.getOrNull(1)
    }
}