package com.example.myapplication.datamanager.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FriendsDAO {
    @Insert
    suspend fun insert(friend: Friends): Long

    @Delete
    suspend fun delete(friend: Friends)

    @Query("SELECT * FROM Friends WHERE loggedUser = :userId")
    suspend fun getFriendsForUser(userId: Int): List<Friends>

    @Query("SELECT COUNT(*) FROM Friends WHERE loggedUser = :userId AND frinedOfUser = :friendId")
    suspend fun friendshipExists(userId: Int, friendId: Int): Int

    //ako iskame da dobavim counter za kolko priayteli ima
    @Query("SELECT COUNT(*) FROM Friends WHERE loggedUser = :loggedUserId")
    suspend fun getFriendCount(loggedUserId: Int): Int


    @Query("""
        SELECT User.username 
        FROM Friends 
        INNER JOIN User ON Friends.frinedOfUser = User.uid 
        WHERE Friends.loggedUser = :userId
    """)
    suspend fun getFriendsNames(userId: Int): List<String>

    @Query("""
        SELECT COUNT(*) FROM Friends 
        WHERE loggedUser = :userId AND frinedOfUser = :friendId
    """)
    suspend fun isAlreadyFriend(userId: Int, friendId: Int): Int


}