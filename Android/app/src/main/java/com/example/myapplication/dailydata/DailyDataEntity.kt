package com.example.myapplication.dailydata

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.myapplication.datamanager.user.User
import java.sql.Date

@Entity(
    tableName = "DailyData",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("user_id"),
            onDelete = ForeignKey.CASCADE
        )
    ])
data class DailyData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "date") val date: Date,
    @ColumnInfo(name = "cals_consumed") val calsConsumed: Int,
    @ColumnInfo(name = "cals_burnt") val calsBurnt: Int,
    @ColumnInfo(name = "fat_consumed") val fatConsumed: Int,
    @ColumnInfo(name = "carbs_consumed") val carbsConsumed: Int,
    @ColumnInfo(name = "protein_consumed") val proteinConsumed: Int,
    @ColumnInfo(name = "user_id") val userId: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DailyData

        if (id != other.id) return false
        if (date != other.date) return false
        if (calsConsumed != other.calsConsumed) return false
        if (calsBurnt != other.calsBurnt) return false
        if (fatConsumed != other.fatConsumed) return false
        if (carbsConsumed != other.carbsConsumed) return false
        if (proteinConsumed != other.proteinConsumed) return false
        if (userId != other.userId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + date.hashCode()
        result = 31 * result + calsConsumed.hashCode()
        result = 31 * result + calsBurnt.hashCode()
        result = 31 * result + fatConsumed.hashCode()
        result = 31 * result + proteinConsumed.hashCode()
        result = 31 * result + carbsConsumed.hashCode()
        result = 31 * result + userId.hashCode()
        return result
    }
}
