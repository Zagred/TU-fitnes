package com.example.myapplication.mealplan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.datamanager.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.HomePage

class MealPlan : AppCompatActivity() {
    private val appId = "3775050e"
    private val appKey = "e59758448c11c3a92bdf5bbce0d56778"
    private val mealPlanRepository = MealPlanRepository()
    private lateinit var database: AppDatabase
    private lateinit var mealsTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MealsAdapter
    private lateinit var calorieEditText: EditText
    private lateinit var searchButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_meal_plan)

        // Initialize views
        mealsTextView = findViewById(R.id.mealsTextView)
        recyclerView = findViewById(R.id.recyclerView)
        calorieEditText = findViewById(R.id.calorieEditText)
        searchButton = findViewById(R.id.searchButton)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MealsAdapter()
        recyclerView.adapter = adapter

        // Initially hide the RecyclerView until search is performed
        recyclerView.visibility = View.GONE
        mealsTextView.visibility = View.VISIBLE

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = AppDatabase.getInstance(application)

        // Set up the search button click listener
        searchButton.setOnClickListener {
            val calorieText = calorieEditText.text.toString()

            if (calorieText.isBlank()) {
                Toast.makeText(this, "Please enter calories", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val calories = calorieText.toIntOrNull()
            if (calories == null || calories <= 0) {
                Toast.makeText(this, "Please enter a valid calorie amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading state
            mealsTextView.text = "Loading meal plans..."
            mealsTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE

            // Fetch meal plans with the entered calories
            fetchMealPlan(calories)
        }
        val home=findViewById<Button>(R.id.btHome)
        home.setOnClickListener{
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchMealPlan(calories: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d("MealPlan", "Fetching detailed meal plan with calories: $calories")
                val meals = mealPlanRepository.fetchDetailedMealPlan(appId, appKey, calories)

                if (meals.isNotEmpty()) {
                    // Update UI with meals
                    adapter.submitList(meals)
                    recyclerView.visibility = View.VISIBLE
                    mealsTextView.visibility = View.GONE
                } else {
                    // Show empty state
                    mealsTextView.text = "No meals found for $calories calories"
                    mealsTextView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    Log.d("MealPlan", "No meals found in the response")
                }
            } catch (e: Exception) {
                Log.e("MealPlan", "Error displaying meal plan", e)
                mealsTextView.text = "Error: ${e.message}"
                mealsTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                Toast.makeText(this@MealPlan, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// RecyclerView Adapter remains the same
class MealsAdapter : RecyclerView.Adapter<MealsAdapter.MealViewHolder>() {
    private var meals: List<Meal> = emptyList()

    fun submitList(newMeals: List<Meal>) {
        meals = newMeals
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(meals[position])
    }

    override fun getItemCount(): Int = meals.size

    class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mealImageView: ImageView = itemView.findViewById(R.id.mealImageView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val caloriesTextView: TextView = itemView.findViewById(R.id.caloriesTextView)
        private val ingredientsTextView: TextView = itemView.findViewById(R.id.ingredientsTextView)
        private val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)

        fun bind(meal: Meal) {
            // Load image using Glide
            meal.imageUrl?.let { url ->
                Glide.with(itemView.context)
                    .load(url)
                    .into(mealImageView)
            }

            titleTextView.text = "${meal.label} (${meal.mealCategory})"
            caloriesTextView.text = "Calories: ${(meal.calories / meal.servings).toInt()}"
            ingredientsTextView.text = meal.ingredientLines.joinToString("\n- ", "- ")
            categoryTextView.text = "Category: ${meal.mealCategory}"
        }
    }
}