package com.example.myapplication.datamanager.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "NutritionInfo",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("user_id"),
            onDelete = ForeignKey.CASCADE
        )
    ])
data class NutritionInfo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "calories_per_day") val caloriesPerDay: Int,
    @ColumnInfo(name = "carbs_per_day") val carbsPerDay: Int,
    @ColumnInfo(name = "protein_per_day") val proteinPerDay: Int,
    @ColumnInfo(name = "fat_per_day") val fatPerDay: Int,
    @ColumnInfo(name = "user_id") val userId: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NutritionInfo

        if (id != other.id) return false
        if (caloriesPerDay != other.caloriesPerDay) return false
        if (carbsPerDay != other.carbsPerDay) return false
        if (proteinPerDay != other.proteinPerDay) return false
        if (fatPerDay != other.fatPerDay) return false
        if (userId != other.userId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + caloriesPerDay.hashCode()
        result = 31 * result + carbsPerDay.hashCode()
        result = 31 * result + proteinPerDay.hashCode()
        result = 31 * result + fatPerDay.hashCode()
        result = 31 * result + userId.hashCode()
        return result
    }
}
