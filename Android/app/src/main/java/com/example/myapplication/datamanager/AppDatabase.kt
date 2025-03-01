package com.example.myapplication.datamanager

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.dailydata.DailyData
import com.example.myapplication.datamanager.activity.Activity
import com.example.myapplication.datamanager.custom.CustomExercise
import com.example.myapplication.datamanager.custom.CustomWorkout
import com.example.myapplication.datamanager.custom.CustomWorkoutCustomExercise
import com.example.myapplication.datamanager.user.NutritionInfo
import com.example.myapplication.datamanager.user.User
import com.example.myapplication.datamanager.user.UserDAO
import com.example.myapplication.datamanager.user.UserInfo
import com.example.myapplication.datamanager.user.UserInfoDAO


@Database(
    entities = [
        User::class, UserInfo::class, NutritionInfo::class, Activity::class, CustomExercise::class,
        CustomWorkout::class, CustomWorkoutCustomExercise::class, DailyData::class
               ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDAO(): UserDAO
    abstract fun userInfoDao(): UserInfoDAO

    companion object {
        private const val DATABASE_NAME = "calorie.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        DATABASE_NAME
                    ).allowMainThreadQueries().build()
                }
                return instance
            }
        }
    }
}
