package com.example.myapplication.datamanager.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(
    tableName = "UserInfo",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("user_id"),
            onDelete = ForeignKey.CASCADE
        )
    ])
data class UserInfo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "birthdate") val birthdate: Date,
    @ColumnInfo(name = "gender") val gender: String,
    @ColumnInfo(name = "height") val height: Int,
    @ColumnInfo(name = "weight") val weight: Float,
    @ColumnInfo(name = "user_id") val userId: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserInfo

        if (id != other.id) return false
        if (birthdate != other.birthdate) return false
        if (gender != other.gender) return false
        if (height != other.height) return false
        if (weight != other.weight) return false
        if (userId != other.userId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + birthdate.hashCode()
        result = 31 * result + gender.hashCode()
        result = 31 * result + height.hashCode()
        result = 31 * result + weight.hashCode()
        result = 31 * result + userId.hashCode()
        return result
    }
}
