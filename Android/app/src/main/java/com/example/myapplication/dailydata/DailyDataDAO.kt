package com.example.myapplication.dailydata

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.datamanager.user.UserInfo
import java.sql.Date

@Dao
interface DailyDataDAO {

    @Query("SELECT * FROM DailyData WHERE user_id LIKE :userId")
    fun getAllByUserId(userId: Int): List<DailyData>

    @Query("SELECT * FROM DailyData WHERE user_id IN " +
            "(SELECT uid, username FROM User WHERE username = :username) LIMIT 1")
    fun getAllByUsername(username: String): List<DailyData>

    @Query("SELECT * FROM DailyData WHERE date LIKE :date AND user_id IN " +
            "(SELECT uid, username FROM User WHERE username = :username) LIMIT 1")
    fun getByUsernameAndDate(username: String, date: Date): DailyData

    @Query("UPDATE DailyData SET protein_consumed = :protein AND cals_consumed = :cals " +
            "AND fat_consumed = :fat AND carbs_consumed = :carbs WHERE user_id IN " +
            "(SELECT uid, username FROM User WHERE username = :username)")
    fun updateCalsCarbsFatProteinByUsername(username: String, cals: Int,
                                            carbs: Int, fat: Int, protein: Int)

    @Query("UPDATE DailyData SET cals_consumed = :cals WHERE user_id IN " +
            "(SELECT uid, username FROM User WHERE username = :username)")
    fun updateCalsByUsername(username: String, cals: Int)

    @Query("UPDATE DailyData SET carbs_consumed = :carbs WHERE user_id IN " +
            "(SELECT uid, username FROM User WHERE username = :username)")
    fun updateCarbsByUsername(username: String, carbs: Int)

    @Query("UPDATE DailyData SET fat_consumed = :fat WHERE user_id IN " +
            "(SELECT uid, username FROM User WHERE username = :username)")
    fun updateFatByUsername(username: String, fat: Int)

    @Query("UPDATE DailyData SET protein_consumed = :protein WHERE user_id IN " +
            "(SELECT uid, username FROM User WHERE username = :username)")
    fun updateProteinByUsername(username: String, protein: Int)

    @Query("UPDATE DailyData SET cals_burnt = :calsBurnt WHERE user_id IN " +
            "(SELECT uid, username FROM User WHERE username = :username)")
    fun updateCalsBurntByUsername(username: String, calsBurnt: Int)

    @Update
    fun update(dailyData: DailyData)

    @Insert
    fun insert(dailyData: DailyData)

}
