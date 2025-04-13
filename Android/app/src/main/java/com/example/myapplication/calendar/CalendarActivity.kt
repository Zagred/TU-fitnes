package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.calendar.CalendarEvent
import com.example.myapplication.datamanager.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class CalendarActivity : AppCompatActivity() {
    private lateinit var adapter: CalendarAdapter
    private var userId: Int = -1
    private val savedDates = mutableSetOf<String>()
    private var currentMonth = 0
    private var currentYear = 0
    private lateinit var calendar: Calendar
    private lateinit var dateView: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var selectedDateInfo: TextView
    private lateinit var allDatesView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar2)

        calendar = Calendar.getInstance()
        currentMonth = calendar.get(Calendar.MONTH)
        currentYear = calendar.get(Calendar.YEAR)
        allDatesView = findViewById(R.id.all_dates_view)



        dateView = findViewById(R.id.date_view)
        calendarRecyclerView = findViewById(R.id.calendar_recycler_view)
        selectedDateInfo = findViewById(R.id.selected_date_info)

        val prevMonthButton = findViewById<Button>(R.id.prev_month)
        val nextMonthButton = findViewById<Button>(R.id.next_month)

        userId = intent.getIntExtra("USER_ID", -1)

        prevMonthButton.setOnClickListener {
            calendar.set(currentYear, currentMonth, 1)
            calendar.add(Calendar.MONTH, -1)
            currentMonth = calendar.get(Calendar.MONTH)
            currentYear = calendar.get(Calendar.YEAR)
            updateCalendarView()
        }

        nextMonthButton.setOnClickListener {
            calendar.set(currentYear, currentMonth, 1)
            calendar.add(Calendar.MONTH, 1)
            currentMonth = calendar.get(Calendar.MONTH)
            currentYear = calendar.get(Calendar.YEAR)
            updateCalendarView()
        }

        updateCalendarView()

        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                checkTodayWorkoutDay()
            }
        }

    }
    private fun checkTodayWorkoutDay() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(applicationContext)

                // today's date
                val todayCalendar = Calendar.getInstance()
                val todayYear = todayCalendar.get(Calendar.YEAR)
                val todayMonth = todayCalendar.get(Calendar.MONTH) + 1
                val todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH)

                Log.d("WorkoutCheck", "Checking workout for: $todayDay/$todayMonth/$todayYear")
                Log.d("WorkoutCheck", "User ID: $userId")

                val allUserEvents = db.calendarEventDao().getAllUserEvents(userId)
                Log.d("WorkoutCheck", "All user events:")
                allUserEvents.forEach { event ->
                    Log.d("WorkoutCheck", "Event: ${event.date}/${event.month}/${event.year}")
                }

                val todayWorkoutDay = db.calendarEventDao().checkEventExists(
                    userId,
                    todayDay.toString(),
                    todayMonth,
                    todayYear
                )

                Log.d("WorkoutCheck", "Is workout day: $todayWorkoutDay")

                if (todayWorkoutDay) {
                    withContext(Dispatchers.Main) {
                        showWorkoutNotification()
                    }

                    db.calendarEventDao().deleteEvent(
                        userId,
                        todayDay.toString(),
                        todayMonth,
                        todayYear
                    )

                    Log.d("WorkoutCheck", "Workout day removed")
                }
            } catch (e: Exception) {
                Log.e("WorkoutCheck", "Error checking workout day", e)
            }
        }
    }
    private fun showWorkoutNotification() {
        try {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "workout_channel",
                    "Workout Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(this, "workout_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Workout Reminder")
                .setContentText("It's time to go workout!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager.notify(2, builder.build())

            Log.d("WorkoutCheck", "Notification shown")
        } catch (e: Exception) {
            Log.e("WorkoutCheck", "Error showing notification", e)

        }
    }

    private fun updateCalendarView() {
        val monthName = getMonthName(currentMonth + 1)
        dateView.text = "$monthName $currentYear"

        val daysList = generateCalendarDays()

        adapter = CalendarAdapter(daysList, savedDates) { selectedDate, isSelected ->
            val action = if (isSelected) "Selected" else "Unselected"
            selectedDateInfo.text = "$action: $selectedDate"
            if (isSelected) {
                saveDate(selectedDate)
            } else {
                deleteDate(selectedDate)
            }
            updateAllDatesDisplay()
        }

        adapter.setCurrentMonthAndYear(currentMonth + 1, currentYear)

        calendarRecyclerView.layoutManager = GridLayoutManager(this, 7)
        calendarRecyclerView.adapter = adapter

        loadSavedDates()
    }
    private fun updateAllDatesDisplay() {
        if (savedDates.isEmpty()) {
            allDatesView.text = "No dates selected"
            return
        }

        val sortedDates = savedDates.sortedWith(compareBy({ it.split("-")[0].toInt() },
            { it.split("-")[1].toInt() },
            { it.split("-")[2].toInt() }))

        val formattedDates = sortedDates.joinToString("\n") { dateKey ->
            val parts = dateKey.split("-")
            val year = parts[0]
            val month = getMonthName(parts[1].toInt())
            val day = parts[2]
            "$day $month $year"
        }

        allDatesView.text = formattedDates
    }

    private fun formatDateKey(date: String): String {
        val parts = date.split("-")
        if (parts.size == 3) {
            return date
        }
        val day = parts[0].toInt()
        return "$currentYear-${currentMonth+1}-$day"
    }

    private fun getMonthName(month: Int): String {
        return when(month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> "Unknown"
        }
    }

    private fun generateCalendarDays(): List<String> {
        val calDays = mutableListOf<CalendarDay>()

        val tempCalendar = Calendar.getInstance()
        tempCalendar.set(currentYear, currentMonth, 1)

        val firstDayOfMonth = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1

        val daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        tempCalendar.add(Calendar.MONTH, -1)
        val daysInPrevMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        tempCalendar.add(Calendar.MONTH, 1)

        for (i in 0 until firstDayOfMonth) {
            val prevDay = daysInPrevMonth - firstDayOfMonth + i + 1
            calDays.add(CalendarDay(prevDay.toString(), false, "prev"))
        }

        for (i in 1..daysInMonth) {
            calDays.add(CalendarDay(i.toString(), true, "current"))
        }

        val remainingDays = 42 - calDays.size
        for (i in 1..remainingDays) {
            calDays.add(CalendarDay(i.toString(), false, "next"))
        }

        return calDays.map {
            when (it.monthType) {
                "current" -> it.day
                "prev" -> "prev-${it.day}"
                "next" -> "next-${it.day}"
                else -> it.day
            }
        }
    }

    private data class CalendarDay(val day: String, val isCurrentMonth: Boolean, val monthType: String)

    private fun saveDate(date: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val parts = date.split("-")
                if (parts.size != 3) return@launch

                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2]

                val event = CalendarEvent(
                    userId = userId,
                    date = day,
                    month = month,
                    year = year
                )

                db.calendarEventDao().insert(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun deleteDate(date: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val parts = date.split("-")
                if (parts.size != 3) return@launch

                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2]

                db.calendarEventDao().deleteEvent(userId, day, month, year)

                withContext(Dispatchers.Main) {
                    showRemovalNotification(day, month, year)

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun showRemovalNotification(day: String, month: Int, year: Int) {
        val monthName = getMonthName(month)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "calendar_channel",
                "Calendar Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, "calendar_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Date Removed")
            .setContentText("You've removed $day $monthName $year from your calendar")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }
    private fun loadSavedDates() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val month = currentMonth + 1
                val year = currentYear

                val events = db.calendarEventDao().getEventsForMonth(userId, month, year)
                val dates = events.map { "$year-$month-${it.date}" }.toSet()

                withContext(Dispatchers.Main) {
                    savedDates.clear()
                    savedDates.addAll(dates)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}