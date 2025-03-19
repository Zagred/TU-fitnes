package com.example.myapplication.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.activity.Activity
import com.example.myapplication.datamanager.custom.CustomExercise

class ExerciseAdapter(
    private val onEditClick: (CustomExercise, Activity?) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    private var exerciseList = emptyList<Pair<CustomExercise, Activity?>>()

    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseName: TextView = itemView.findViewById(R.id.tvExerciseName)
        val exerciseDetails: TextView = itemView.findViewById(R.id.tvExerciseDetails)
        val activityDescription: TextView = itemView.findViewById(R.id.tvActivityDescription)
        val editButton: TextView = itemView.findViewById(R.id.tvEditExercise)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val (exercise, activity) = exerciseList[position]

        activity?.let {
            holder.exerciseName.text = it.name
            holder.exerciseDetails.text = "Sets: ${exercise.sets}, Reps: ${exercise.reps}, Weight: ${exercise.weightsInKg}kg"
            holder.activityDescription.text = "Description: ${it.description ?: "No description"}"

            holder.editButton.setOnClickListener {
                onEditClick(exercise, activity)
            }
        } ?: run {
            holder.activityDescription.text = ""
        }
    }

    override fun getItemCount(): Int = exerciseList.size

    fun setData(exercisesWithActivities: List<Pair<CustomExercise, Activity?>>) {
        this.exerciseList = exercisesWithActivities
        notifyDataSetChanged()
    }
}

