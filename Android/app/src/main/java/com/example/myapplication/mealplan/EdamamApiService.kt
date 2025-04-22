package com.example.myapplication.mealplan

import retrofit2.http.*

interface EdamamApiService {
    @POST("api/meal-planner/v1/{app_id}/select")
    suspend fun getMealPlan(
        @Path("app_id") appId: String,
        @Query("app_key") appKey: String,
        @Query("type") type: String,
        @Header("Edamam-Account-User") userId: String = "new user",
        @Header("Authorization") authorization: String,
        @Body request: MealPlanRequest
    ): MealPlanResponse

    @GET("api/recipes/v2/{recipeId}")
    suspend fun getRecipeDetails(
        @Path("recipeId") recipeId: String,
        @Query("type") type: String,
        @Query("app_id") appId: String,
        @Query("app_key") appKey: String,
        @Header("Edamam-Account-User") userId: String = "new user"
    ): RecipeResponse
}