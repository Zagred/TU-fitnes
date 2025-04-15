package com.example.myapplication.datamanager.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NutritionInfoDAO {
    @Query("SELECT * FROM NutritionInfo")
    fun getAll(): List<NutritionInfo>
    @Update
    fun update(nutritionInfo: NutritionInfo)

    @Insert
    fun insert(nutritionInfo: NutritionInfo)

    @Delete
    fun delete(nutritionInfo: NutritionInfo)
}
