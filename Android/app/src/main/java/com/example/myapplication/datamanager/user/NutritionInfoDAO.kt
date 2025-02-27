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

    @Query("SELECT * FROM NutritionInfo WHERE user_id LIKE :userId LIMIT 1")
    fun findByUserId(userId: Int): NutritionInfo

    @Query("SELECT * FROM NutritionInfo WHERE user_id IN " +
            "(SELECT uid, username FROM User WHERE username = :username) LIMIT 1")
    fun findByUsername(username: String): NutritionInfo

    @Query("UPDATE NutritionInfo SET calories_per_day = :calories WHERE user_id IN " +
            "(SELECT uid, username FROM User WHERE username = :username)")
    fun updateUserCaloriesByUsername(username: String, calories: Int)

    @Query("UPDATE NutritionInfo SET carbs_per_day = :carbs WHERE user_id IN " +
            "(SELECT uid, username FROM User WHERE username = :username)")
    fun updateUserCarbsByUsername(username: String, carbs: Int)

    @Query("UPDATE NutritionInfo SET protein_per_day = :protein WHERE user_id IN " +
            "(SELECT uid, username FROM User WHERE username = :username)")
    fun updateUserProteinByUsername(username: String, protein: Int)

    @Query("UPDATE NutritionInfo SET fat_per_day = :fat WHERE user_id IN " +
            "(SELECT uid, username FROM User WHERE username = :username)")
    fun updateUserFatByUsername(username: String, fat: Int)

    @Query("DELETE FROM NutritionInfo WHERE user_id IN " +
            "(SELECT uid, username FROM User WHERE username = :username)")
    fun deleteNutritionInfoByUsername(username: String)

    @Update
    fun update(nutritionInfo: NutritionInfo)

    @Insert
    fun insert(nutritionInfo: NutritionInfo)

    @Delete
    fun delete(nutritionInfo: NutritionInfo)
}
