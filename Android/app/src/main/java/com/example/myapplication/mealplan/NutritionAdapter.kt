// NutritionAdapter.kt
package com.example.myapplication.mealplan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemNutritionBinding

class NutritionAdapter(private val items: List<NutritionItem>) :
    RecyclerView.Adapter<NutritionAdapter.NutritionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutritionViewHolder {
        val binding = ItemNutritionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NutritionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NutritionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class NutritionViewHolder(private val binding: ItemNutritionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NutritionItem) {
            binding.foodNameText.text = item.name.capitalize()
            binding.caloriesText.text = "Calories: ${item.calories}"
            binding.servingSizeText.text = "Serving: ${item.servingSize}"
            binding.fatsText.text = "Fat: ${item.fatTotal}g (Sat: ${item.fatSaturated}g)"
            binding.proteinText.text = "Protein: ${item.protein}"
            binding.carbsText.text = "Carbs: ${item.carbs}g (Sugar: ${item.sugar}g, Fiber: ${item.fiber}g)"

            binding.mineralsText.text = "Sodium: ${item.sodium}mg | Potassium: ${item.potassium}mg"
            binding.cholesterolText.text = "Cholesterol: ${item.cholesterol}mg"
        }
    }
}