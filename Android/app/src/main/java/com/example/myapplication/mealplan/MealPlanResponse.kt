package com.example.myapplication.mealplan

import com.google.gson.annotations.SerializedName

// ==== API Request Models ====
data class MealPlanRequest(
    val size: Int,
    val plan: PlanDetails
)

data class PlanDetails(
    val fit: Map<String, Map<String, Int>>,
    val sections: Map<String, SectionDetails>
)

data class SectionDetails(
    val accept: Map<String, List<Map<String, List<String>>>>,
    val fit: Map<String, Map<String, Int>>
)

// ==== API Response Models ====
data class MealPlanResponse(
    @SerializedName("status") val status: String,
    @SerializedName("selection") val selection: List<DaySelection>
)

data class DaySelection(
    @SerializedName("sections") val sections: Map<String, SectionResponse>
)

data class SectionResponse(
    @SerializedName("assigned") val assigned: String? = null,
    @SerializedName("_links") val links: LinksResponse? = null,
    @SerializedName("sections") val sections: Map<String, SectionResponse>? = null
)

data class LinksResponse(
    @SerializedName("self") val self: LinkDetails
)

data class LinkDetails(
    @SerializedName("title") val title: String,
    @SerializedName("href") val href: String
)

// ==== Recipe Detail Models ====
data class RecipeResponse(
    @SerializedName("recipe") val recipe: RecipeDetails
)

data class RecipeDetails(
    @SerializedName("label") val label: String,
    @SerializedName("url") val url: String,
    @SerializedName("calories") val calories: Double,
    @SerializedName("yield") val yield: Int,
    @SerializedName("totalTime") val totalTime: Int,
    @SerializedName("ingredientLines") val ingredientLines: List<String>,
    @SerializedName("dietLabels") val dietLabels: List<String>,
    @SerializedName("healthLabels") val healthLabels: List<String>,
    @SerializedName("cuisineType") val cuisineType: List<String> = emptyList(),
    @SerializedName("mealType") val mealType: List<String> = emptyList(),
    @SerializedName("dishType") val dishType: List<String> = emptyList(),
    @SerializedName("images") val images: ImagesResponse? = null
)

data class ImagesResponse(
    @SerializedName("REGULAR") val regular: ImageDetails? = null,
    @SerializedName("SMALL") val small: ImageDetails? = null
)

data class ImageDetails(
    @SerializedName("url") val url: String
)

// ==== Meal Model (Used for UI display) ====
data class Meal(
    val label: String,
    val url: String,
    val calories: Double = 0.0,
    val servings: Int = 0,
    val totalTime: Int = 0,
    val ingredientLines: List<String> = emptyList(),
    val dietLabels: List<String> = emptyList(),
    val healthLabels: List<String> = emptyList(),
    val cuisineType: List<String> = emptyList(),
    val mealType: List<String> = emptyList(),
    val dishType: List<String> = emptyList(),
    val imageUrl: String? = null,
    val mealCategory: String = "" // Breakfast, Lunch, Dinner, etc.
)

// ==== Extension Functions ====
fun MealPlanResponse.extractMeals(): List<Meal> {
    val meals = mutableListOf<Meal>()

    this.selection.forEach { day ->
        extractMealsFromSection(day.sections, meals)
    }

    return meals
}

private fun extractMealsFromSection(sections: Map<String, SectionResponse>, meals: MutableList<Meal>, category: String = "") {
    sections.forEach { (sectionName, section) ->
        // If this is a recipe (has links)
        section.links?.self?.let { link ->
            meals.add(Meal(
                label = link.title,
                url = link.href,
                mealCategory = if (category.isNotEmpty()) category else sectionName
            ))
        }

        // If this section has subsections
        section.sections?.let { subsections ->
            extractMealsFromSection(subsections, meals,
                if (category.isNotEmpty()) category else sectionName)
        }
    }
}