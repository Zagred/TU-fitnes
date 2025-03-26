package com.example.myapplication.coach

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.datamanager.coach.Coach

class CoachAdapter(
    private val coaches: MutableList<Coach>,
    private val isAdminMode: Boolean = false,
    private val onDeleteClick: ((Coach) -> Unit)? = null
) : RecyclerView.Adapter<CoachAdapter.CoachViewHolder>() {

    class CoachViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.coachNameTextView)
        val specializationTextView: TextView = view.findViewById(R.id.coachSpecializationTextView)
        val contactInfoTextView: TextView = view.findViewById(R.id.coachContactInfoTextView)
        val deleteButton: Button? = view.findViewById(R.id.deleteCoachButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoachViewHolder {
        val layoutId = if (isAdminMode) R.layout.coach_item_admin else R.layout.coach_item_user
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return CoachViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoachViewHolder, position: Int) {
        val coach = coaches[position]
        holder.nameTextView.text = coach.name
        holder.specializationTextView.text = coach.specialization
        holder.contactInfoTextView.text = coach.contactInfo

        if (isAdminMode) {
            holder.deleteButton?.setOnClickListener {
                onDeleteClick?.invoke(coach)
            }
        }
    }

    override fun getItemCount() = coaches.size

    fun updateCoaches(newCoaches: List<Coach>) {
        coaches.clear()
        coaches.addAll(newCoaches)
        notifyDataSetChanged()
    }
}