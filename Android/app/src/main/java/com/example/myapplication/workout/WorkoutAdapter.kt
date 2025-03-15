package com.example.myapplication.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.custom.CustomWorkout

class WorkoutAdapter(
    private val onDeleteClick: (CustomWorkout) -> Unit,
    private val onEditClick: (CustomWorkout) -> Unit,
    private val onOpenClick: (CustomWorkout) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    private var workoutsList = emptyList<CustomWorkout>()

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workoutName: TextView = itemView.findViewById(R.id.tvWorkoutName)
        val workoutTimer: TextView = itemView.findViewById(R.id.tvWorkoutTimer)
        val optionsButton: TextView = itemView.findViewById(R.id.tvOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workoutsList[position]
        holder.workoutName.text = workout.name
        holder.workoutTimer.text = "Rest: ${workout.restInSeconds} sec"

        holder.optionsButton.setOnClickListener { view ->
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.menu_workout, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit -> {
                        onEditClick(workout)
                        true
                    }
                    R.id.action_delete -> {
                        onDeleteClick(workout)
                        true
                    }
                    R.id.action_open -> {
                        onOpenClick(workout)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int = workoutsList.size

    fun setData(workouts: List<CustomWorkout>) {
        this.workoutsList = workouts
        notifyDataSetChanged()
    }
}
