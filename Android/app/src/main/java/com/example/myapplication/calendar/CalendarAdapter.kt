package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class CalendarAdapter(
    private val days: List<String>,
    private val savedDates: MutableSet<String>,
    private val onDateClick: (String, Boolean) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.day_text)

        fun bind(dateText: String) {
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH) + 1
            val currentYear = calendar.get(Calendar.YEAR)

            var isCurrentMonth = true
            var displayText = dateText
            var month = currentMonth
            var year = currentYear

            when {
                dateText.startsWith("prev-") -> {
                    displayText = dateText.substring(5)
                    isCurrentMonth = false

                    // Calculate previous month
                    if (currentMonth == 1) {
                        month = 12
                        year = currentYear - 1
                    } else {
                        month = currentMonth - 1
                    }
                }
                dateText.startsWith("next-") -> {
                    displayText = dateText.substring(5)
                    isCurrentMonth = false

                    // Calculate next month
                    if (currentMonth == 12) {
                        month = 1
                        year = currentYear + 1
                    } else {
                        month = currentMonth + 1
                    }
                }
            }

            dayText.text = displayText

            val dateKey = "$year-$month-$displayText"

            if (!isCurrentMonth) {
                dayText.setTextColor(Color.GRAY)
                dayText.alpha = 0.5f
            } else {
                dayText.alpha = 1.0f
                if (savedDates.contains(dateKey)) {
                    dayText.setTextColor(Color.RED)
                } else {
                    dayText.setTextColor(Color.BLACK)
                }
            }

            dayText.setOnClickListener {
                if (!isCurrentMonth) return@setOnClickListener

                val isNowSelected = !savedDates.contains(dateKey)
                if (isNowSelected) {
                    savedDates.add(dateKey)
                } else {
                    savedDates.remove(dateKey)
                }
                notifyDataSetChanged()
                onDateClick(dateKey, isNowSelected)
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