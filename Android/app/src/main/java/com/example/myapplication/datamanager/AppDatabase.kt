package com.example.myapplication.datamanager

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.dailydata.DailyData
import com.example.myapplication.dailydata.DailyDataDAO
import com.example.myapplication.datamanager.activity.Activity
import com.example.myapplication.datamanager.activity.ActivityDAO
import com.example.myapplication.datamanager.custom.CustomExerciseDAO
import com.example.myapplication.datamanager.custom.CustomWorkout
import com.example.myapplication.datamanager.custom.CustomExercise
import com.example.myapplication.datamanager.custom.CustomWorkoutCustomExercise
import com.example.myapplication.datamanager.custom.CustomWorkoutCustomExerciseDAO
import com.example.myapplication.datamanager.custom.CustomWorkoutDAO
import com.example.myapplication.datamanager.user.NutritionInfo
import com.example.myapplication.datamanager.user.NutritionInfoDAO
import com.example.myapplication.datamanager.user.User
import com.example.myapplication.datamanager.user.UserDAO
import com.example.myapplication.datamanager.user.UserInfo
import com.example.myapplication.datamanager.user.UserInfoDAO
import com.example.myapplication.datamanager.user.Friends
import com.example.myapplication.datamanager.user.FriendsDAO
import com.example.myapplication.datamanager.user.Post
import com.example.myapplication.datamanager.user.PostDAO



@Database(
    entities = [
        User::class, UserInfo::class, NutritionInfo::class, Activity::class, CustomExercise::class,
        CustomWorkout::class, CustomWorkoutCustomExercise::class, DailyData::class, Post::class, Friends::class
    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDAO(): UserDAO
    abstract fun activityDAO(): ActivityDAO
    abstract fun userInfoDAO(): UserInfoDAO
    abstract fun nutritionInfoDAO():NutritionInfoDAO
    abstract fun customWorkoutDAO():CustomWorkoutDAO
    abstract fun customExerciseDAO(): CustomExerciseDAO
    abstract fun customWorkoutCustomExerciseDAO(): CustomWorkoutCustomExerciseDAO
    abstract fun postDAO(): PostDAO
    abstract fun friendsDAO(): FriendsDAO
    abstract fun dailyDataDAO(): DailyDataDAO

    companion object {
        private const val DATABASE_NAME = "calorie.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null



        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        DATABASE_NAME
                    ).allowMainThreadQueries()
                        .createFromAsset("database/fitnessapp.db")
                        .build()
                }
                return instance
            }
        }
    }
}
/*synchronized(this) {
                context.deleteDatabase(DATABASE_NAME)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()

                INSTANCE = instance
                return instance
            }




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
            }*/