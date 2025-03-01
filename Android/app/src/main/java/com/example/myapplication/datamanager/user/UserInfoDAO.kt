
// Fixed UserInfoDAO.kt
package com.example.myapplication.datamanager.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserInfoDAO {
    @Query("SELECT * FROM UserInfo")
    fun getAll(): List<UserInfo>

    @Query("SELECT * FROM UserInfo WHERE user_id = :userId LIMIT 1")
    fun findByUserId(userId: Int): UserInfo

    @Query("SELECT * FROM UserInfo WHERE user_id IN " +
            "(SELECT uid FROM User WHERE username = :username) LIMIT 1")
    fun findByUsername(username: String): UserInfo

    @Query("UPDATE UserInfo SET height = :height WHERE user_id IN " +
            "(SELECT uid FROM User WHERE username = :username)")
    fun updateUserHeightByUsername(username: String, height: Int)

    @Query("UPDATE UserInfo SET weight = :weight WHERE user_id IN " +
            "(SELECT uid FROM User WHERE username = :username)")
    fun updateUserWeightByUsername(username: String, weight: Float)

    @Query("DELETE FROM UserInfo WHERE user_id IN " +
            "(SELECT uid FROM User WHERE username = :username)")
    fun deleteUserInfoByUsername(username: String)

    @Update
    fun update(userInfo: UserInfo)

    @Insert
    fun insert(userInfo: UserInfo)

    @Delete
    fun delete(userInfo: UserInfo)
}