package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_completions",
    indices = [
        Index(value = ["habitId", "dateEpochDay"], unique = true),
        Index(value = ["dateEpochDay"])
    ]
)
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val dateEpochDay: Long,
    val completedAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)
