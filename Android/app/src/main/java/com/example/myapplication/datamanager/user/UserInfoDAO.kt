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

    @Query("UPDATE UserInfo SET avatar = :avatar WHERE user_id = :userId")
    fun updateUserAvatar(userId: Int, avatar: String)
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