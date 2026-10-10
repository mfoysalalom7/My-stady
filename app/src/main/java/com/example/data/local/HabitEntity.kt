package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val colorHex: String = "#10B981",
    val iconKey: String = "flame",
    val category: String = "Health",
    val frequencyType: String = "DAILY", // DAILY, WEEKDAYS, WEEKENDS, CUSTOM
    val targetDaysPerWeek: Int = 7,
    val timeOfDay: String = "ANYTIME",   // ANYTIME, MORNING, AFTERNOON, EVENING
    val createdAtEpochDay: Long,
    val isArchived: Boolean = false,
    val displayOrder: Int = 0
)
