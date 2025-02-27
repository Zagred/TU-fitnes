package com.example.myapplication.datamanager.custom

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.myapplication.datamanager.activity.Activity

@Entity(tableName = "CustomExercise",
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("activity_id"),
            onDelete = ForeignKey.CASCADE
        )
    ])
data class CustomExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "activity_id") val activityId: Int,
    @ColumnInfo(name = "weights_in_kg") val weightsInKg: Int,
    @ColumnInfo(name = "reps") val reps: Int,
    @ColumnInfo(name = "sets") val sets: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CustomExercise

        if (id != other.id) return false
        if (activityId != other.activityId) return false
        if (weightsInKg != other.weightsInKg) return false
        if (reps != other.reps) return false
        if (sets != other.sets) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + activityId.hashCode()
        result = 31 * result + weightsInKg.hashCode()
        result = 31 * result + reps.hashCode()
        result = 31 * result + sets.hashCode()

        return result
    }
}
