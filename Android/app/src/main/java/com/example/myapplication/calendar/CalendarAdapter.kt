package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarAdapter(
    private val days: List<String>,
    private val onDateClick: (String) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val selectedDates = mutableSetOf<String>()

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.day_text)

        fun bind(date: String) {
            dayText.text = date

            if (selectedDates.contains(date)) {
                dayText.setTextColor(Color.RED)
            } else {
                dayText.setTextColor(Color.BLACK)
            }

            dayText.setOnClickListener {
                if (selectedDates.contains(date)) {
                    selectedDates.remove(date)
                } else {
                    selectedDates.add(date)
                }
                notifyDataSetChanged()
                onDateClick(date)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int = days.size
}
