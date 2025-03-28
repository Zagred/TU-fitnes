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

    /*
        @Query("SELECT * FROM UserInfo WHERE user_id LIKE :userId LIMIT 1")
        fun findByUserId(userId: Int): UserInfo

        @Query("SELECT * FROM UserInfo WHERE user_id IN " +
                "(SELECT uid, username FROM User WHERE username = :username) LIMIT 1")
        fun findByUsername(username: String): UserInfo

        @Query("UPDATE UserInfo SET height = :height WHERE user_id IN " +
                "(SELECT uid, username FROM User WHERE username = :username)")
        fun updateUserHeightByUsername(username: String, height: Int)

        @Query("UPDATE UserInfo SET height = :weight WHERE user_id IN " +
                "(SELECT uid, username FROM User WHERE username = :username)")
        fun updateUserWeightByUsername(username: String, weight: Float)

        @Query("DELETE FROM UserInfo WHERE user_id IN " +
                "(SELECT uid, username FROM User WHERE username = :username)")
        fun deleteUserInfoByUsername(username: String)
    */
    @Query("SELECT * FROM UserInfo WHERE user_id = :userId LIMIT 1")
    fun getUserInfoByUserId(userId: Int): UserInfo?

    @Query("SELECT * FROM UserInfo")
    fun getAllUserInfo(): List<UserInfo>

    @Update
    fun update(userInfo: UserInfo)

    @Insert
    fun insert(userInfo: UserInfo)

    @Delete
    fun delete(userInfo: UserInfo)

    @Query("DELETE FROM UserInfo WHERE user_id = :userId")
    fun deleteUserInfoByUserId(userId: Int)

}