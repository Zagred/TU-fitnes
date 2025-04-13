package com.example.myapplication.challenge

import androidx.room.*

@Dao
interface ChallengeDAO {

    @Query("SELECT * FROM Challenge")
    fun getAll(): List<Challenge>

    @Query("SELECT * FROM Challenge WHERE id = :challengeId LIMIT 1")
    fun findById(challengeId: Int): Challenge?

    @Query("SELECT * FROM Challenge WHERE LOWER(difficulty) = LOWER(:difficulty) AND is_completed = 0 ORDER BY RANDOM() LIMIT :limit")
    fun getRandomByDifficulty(difficulty: String, limit: Int): List<Challenge>

    @Query("UPDATE Challenge SET is_completed = 1 WHERE id = :challengeId")
    fun markChallengeAsCompleted(challengeId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(challenge: Challenge): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(challenges: List<Challenge>)

    @Update
    fun update(challenge: Challenge)

    @Delete
    fun delete(challenge: Challenge)
}
