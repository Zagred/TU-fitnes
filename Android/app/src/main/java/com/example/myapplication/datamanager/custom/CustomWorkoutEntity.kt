package com.example.myapplication.datamanager.custom

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.myapplication.datamanager.user.User

@Entity(tableName = "CustomWorkout",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("user_id"),
            onDelete = ForeignKey.CASCADE
        )
    ])
data class CustomWorkout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "name") val name: Int,
    @ColumnInfo(name = "rest_in_seconds") val restInSeconds: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CustomWorkout

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (name != other.name) return false
        if (restInSeconds != other.restInSeconds) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + userId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + restInSeconds.hashCode()

        return result
    }
}
