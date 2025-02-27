package com.example.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class CalendarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar2)

        val dateView: TextView = findViewById(R.id.date_view)
        val calendarRecyclerView: RecyclerView = findViewById(R.id.calendar_recycler_view)

        val daysList = generateCalendarDays()

        val adapter = CalendarAdapter(daysList) { selectedDate ->
            dateView.text = "Selected: $selectedDate"
        }

        calendarRecyclerView.layoutManager = GridLayoutManager(this, 7) // 7 columns for weeks
        calendarRecyclerView.adapter = adapter
    }

    private fun generateCalendarDays(): List<String> {
        val days = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (day in 1..maxDays) {
            days.add(day.toString())
        }

        return days
    }
}
