package com.example.myapplication.mealplan

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityCheckCaloriesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URLEncoder

class CheckCalories : AppCompatActivity() {
    private lateinit var binding: ActivityCheckCaloriesBinding
    private lateinit var adapter: NutritionAdapter
    private val nutritionItems = mutableListOf<NutritionItem>()

    private val apiKey = "HOBpNVIKKDVsL8GfAnSf+g==ogR1fKvNYW7B2xqU"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckCaloriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchFunctionality()
    }

    private fun setupRecyclerView() {
        adapter = NutritionAdapter(nutritionItems)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearchFunctionality() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.searchButton.setOnClickListener {
            performSearch()
        }
    }

    private fun performSearch() {
        val query = binding.searchEditText.text.toString().trim()

        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a food", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.emptyStateText.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                val url = "https://api.api-ninjas.com/v1/nutrition?query=$encodedQuery"

                val response = HttpClient.get(url, apiKey)
                val nutritionData = parseNutritionData(response)

                withContext(Dispatchers.Main) {
                    nutritionItems.clear()

                    if (nutritionData.isEmpty()) {
                        binding.emptyStateText.visibility = View.VISIBLE
                        binding.emptyStateText.text = "No results found for '$query'"
                    } else {
                        nutritionItems.addAll(nutritionData)
                    }

                    adapter.notifyDataSetChanged()
                    binding.progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CheckCalories, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun parseNutritionData(jsonString: String): List<NutritionItem> {
        val result = mutableListOf<NutritionItem>()
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val name = obj.getString("name")
            val calories = if (obj.getString("calories") == "Only available for premium subscribers.")
                "Premium only" else obj.getString("calories")
            val servingSize = if (obj.getString("serving_size_g") == "Only available for premium subscribers.")
                "Premium only" else "${obj.getString("serving_size_g")}g"
            val fatTotal = obj.getDouble("fat_total_g")
            val fatSaturated = obj.getDouble("fat_saturated_g")
            val protein = if (obj.getString("protein_g") == "Only available for premium subscribers.")
                "Premium only" else "${obj.getString("protein_g")}g"
            val sodium = obj.getInt("sodium_mg")
            val potassium = obj.getInt("potassium_mg")
            val cholesterol = obj.getInt("cholesterol_mg")
            val carbs = obj.getDouble("carbohydrates_total_g")
            val fiber = obj.getDouble("fiber_g")
            val sugar = obj.getDouble("sugar_g")

            result.add(
                NutritionItem(
                    name, calories, servingSize, fatTotal, fatSaturated, protein,
                    sodium, potassium, cholesterol, carbs, fiber, sugar
                )
            )
        }

        return result
    }
}