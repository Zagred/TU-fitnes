package com.example.myapplication.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.custom.CustomWorkout

class WorkoutAdapter(
    private val onDeleteClick: (CustomWorkout) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    private var workoutsList = emptyList<CustomWorkout>()

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workoutName: TextView = itemView.findViewById(R.id.tvWorkoutName)
        val workoutTimer: TextView = itemView.findViewById(R.id.tvWorkoutTimer)
//        val deleteButton: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workoutsList[position]
        holder.workoutName.text = workout.name
        holder.workoutTimer.text = "Rest: ${workout.restInSeconds} sec"
/*
        holder.deleteButton.setOnClickListener {
            onDeleteClick(workout)
        }*/
    }

    override fun getItemCount(): Int = workoutsList.size

    fun setData(workouts: List<CustomWorkout>) {
        this.workoutsList = workouts
        notifyDataSetChanged()
    }
}
