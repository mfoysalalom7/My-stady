package com.example.data.model

import com.example.data.local.HabitEntity
import com.example.util.AppDate

data class HabitWithStats(
    val habit: HabitEntity,
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompletions: Int,
    val completionRate30Days: Float, // 0.0f to 1.0f
    val isCompletedForDate: Boolean,
    val last7DaysStatus: List<DayCheckStatus>,
    val completedEpochDays: Set<Long>
)

data class DayCheckStatus(
    val date: AppDate,
    val isCompleted: Boolean,
    val isToday: Boolean
)

data class GlobalStats(
    val totalHabits: Int,
    val totalCompletionsAllTime: Int,
    val todayCompletedCount: Int,
    val todayTotalCount: Int,
    val longestActiveStreak: Int,
    val overallSuccessRate: Float, // 0.0f to 1.0f
    val bestHabitTitle: String? = null
)
