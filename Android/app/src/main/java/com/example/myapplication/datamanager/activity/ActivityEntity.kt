package com.example.myapplication.datamanager.activity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Activity")
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "metabolic_equivalent") val metabolicEquivalent: Int,
    @ColumnInfo(name = "description") val description: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Activity

        if (id != other.id) return false
        if (name != other.name) return false
        if (metabolicEquivalent != other.metabolicEquivalent) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + metabolicEquivalent.hashCode()
        result = 31 * result + description.hashCode()

        return result
    }
}
