package com.example.myapplication.achievement

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.achievement.Achievement

class AchievementAdapter(
    private val achievements: MutableList<Achievement>,
    private val onProgressUpdate: (Achievement, Int) -> Unit,
    private val onDeleteClick: (Achievement) -> Unit
) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    class AchievementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val goalNameTextView: TextView = view.findViewById(R.id.goalNameTextView)
        val progressTextView: TextView = view.findViewById(R.id.progressTextView)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val progressInput: EditText = view.findViewById(R.id.progressInput)
        val updateProgressButton: Button = view.findViewById(R.id.updateProgressButton)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val achievement = achievements[position]

        holder.goalNameTextView.text = achievement.goalName

        val progressPercentage = if (achievement.targetValue > 0) {
            (achievement.currentValue.toFloat() / achievement.targetValue * 100).toInt()
        } else {
            0
        }

        holder.progressTextView.text = "${achievement.currentValue}/${achievement.targetValue}"
        holder.progressBar.progress = progressPercentage

        holder.progressInput.setText(achievement.currentValue.toString())

        holder.updateProgressButton.setOnClickListener {
            val inputProgress = holder.progressInput.text.toString().toIntOrNull() ?: 0
            onProgressUpdate(achievement, inputProgress)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(achievement)
        }
    }

    override fun getItemCount() = achievements.size

    fun updateAchievements(newAchievements: List<Achievement>) {
        achievements.clear()
        achievements.addAll(newAchievements)
        notifyDataSetChanged()
    }
}