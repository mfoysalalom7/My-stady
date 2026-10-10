package com.example.data.repository

import com.example.data.local.HabitCompletionEntity
import com.example.data.local.HabitDao
import com.example.data.local.HabitEntity
import com.example.data.model.DayCheckStatus
import com.example.data.model.GlobalStats
import com.example.data.model.HabitWithStats
import com.example.util.AppDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class HabitRepository(private val habitDao: HabitDao) {

    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllActiveHabits()
    val allCompletions: Flow<List<HabitCompletionEntity>> = habitDao.getAllCompletions()

    suspend fun checkAndSeedInitialData() {
        val count = habitDao.getHabitCount()
        if (count == 0) {
            val today = AppDate.today()
            val todayEpoch = today.epochDay

            val habit1Id = habitDao.insertHabit(
                HabitEntity(
                    title = "Drink 2L Water",
                    description = "Stay hydrated and drink 8 glasses daily",
                    colorHex = "#06B6D4",
                    iconKey = "water",
                    category = "Health",
                    frequencyType = "DAILY",
                    targetDaysPerWeek = 7,
                    timeOfDay = "ANYTIME",
                    createdAtEpochDay = todayEpoch - 14,
                    displayOrder = 0
                )
            )

            val habit2Id = habitDao.insertHabit(
                HabitEntity(
                    title = "Morning Meditation",
                    description = "10 minutes of calm breathwork and mindfulness",
                    colorHex = "#8B5CF6",
                    iconKey = "meditation",
                    category = "Mindfulness",
                    frequencyType = "DAILY",
                    targetDaysPerWeek = 7,
                    timeOfDay = "MORNING",
                    createdAtEpochDay = todayEpoch - 10,
                    displayOrder = 1
                )
            )

            val habit3Id = habitDao.insertHabit(
                HabitEntity(
                    title = "Read 20 Pages",
                    description = "Non-fiction, tech books, or daily wisdom",
                    colorHex = "#F59E0B",
                    iconKey = "book",
                    category = "Learning",
                    frequencyType = "DAILY",
                    targetDaysPerWeek = 7,
                    timeOfDay = "EVENING",
                    createdAtEpochDay = todayEpoch - 12,
                    displayOrder = 2
                )
            )

            val habit4Id = habitDao.insertHabit(
                HabitEntity(
                    title = "30m Workout",
                    description = "Cardio, calisthenics, or gym session",
                    colorHex = "#10B981",
                    iconKey = "fitness",
                    category = "Fitness",
                    frequencyType = "DAILY",
                    targetDaysPerWeek = 5,
                    timeOfDay = "MORNING",
                    createdAtEpochDay = todayEpoch - 8,
                    displayOrder = 3
                )
            )

            // Seed completions for habit 1 (Water: 6-day streak)
            for (offset in 0..5) {
                habitDao.insertCompletion(
                    HabitCompletionEntity(
                        habitId = habit1Id,
                        dateEpochDay = todayEpoch - offset
                    )
                )
            }

            // Seed completions for habit 2 (Meditation: 4-day streak)
            for (offset in 0..3) {
                habitDao.insertCompletion(
                    HabitCompletionEntity(
                        habitId = habit2Id,
                        dateEpochDay = todayEpoch - offset
                    )
                )
            }

            // Seed completions for habit 3 (Read: completed yesterday & 2 days ago, pending today)
            for (offset in 1..4) {
                habitDao.insertCompletion(
                    HabitCompletionEntity(
                        habitId = habit3Id,
                        dateEpochDay = todayEpoch - offset
                    )
                )
            }

            // Seed completions for habit 4 (Workout: completed today & 2 days ago)
            habitDao.insertCompletion(
                HabitCompletionEntity(
                    habitId = habit4Id,
                    dateEpochDay = todayEpoch
                )
            )
            habitDao.insertCompletion(
                HabitCompletionEntity(
                    habitId = habit4Id,
                    dateEpochDay = todayEpoch - 2
                )
            )
        }
    }

    suspend fun insertHabit(habit: HabitEntity): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habitId: Long) {
        habitDao.deleteHabitAndCompletions(habitId)
    }

    suspend fun toggleHabitCompletion(habitId: Long, dateEpochDay: Long): Boolean {
        val existing = habitDao.getCompletion(habitId, dateEpochDay)
        return if (existing != null) {
            habitDao.deleteCompletion(habitId, dateEpochDay)
            false
        } else {
            habitDao.insertCompletion(
                HabitCompletionEntity(
                    habitId = habitId,
                    dateEpochDay = dateEpochDay
                )
            )
            true
        }
    }

    fun getHabitsWithStats(selectedDate: AppDate): Flow<List<HabitWithStats>> {
        val today = AppDate.today()
        val todayEpoch = today.epochDay
        val selectedEpoch = selectedDate.epochDay

        return combine(allHabits, allCompletions) { habits, completions ->
            val completionsByHabit = completions.groupBy { it.habitId }

            habits.map { habit ->
                val habitCompletions = completionsByHabit[habit.id] ?: emptyList()
                val completedEpochDays = habitCompletions.map { it.dateEpochDay }.toSet()

                val currentStreak = calculateCurrentStreak(completedEpochDays, todayEpoch)
                val bestStreak = calculateBestStreak(completedEpochDays, currentStreak)
                val isCompleted = completedEpochDays.contains(selectedEpoch)

                // Last 7 days status ending at selectedDate (or today)
                val referenceDate = if (selectedDate.epochDay > todayEpoch) selectedDate else today
                val last7Days = (6 downTo 0).map { offset ->
                    val day = referenceDate.minusDays(offset)
                    DayCheckStatus(
                        date = day,
                        isCompleted = completedEpochDays.contains(day.epochDay),
                        isToday = day.isSameDay(today)
                    )
                }

                // 30 day completion rate
                val daysToCheck = 30
                var completedInWindow = 0
                val habitAgeDays = (todayEpoch - habit.createdAtEpochDay + 1).coerceAtLeast(1)
                val windowSize = minOf(daysToCheck.toLong(), habitAgeDays).toInt()

                for (offset in 0 until windowSize) {
                    if (completedEpochDays.contains(todayEpoch - offset)) {
                        completedInWindow++
                    }
                }
                val rate = if (windowSize > 0) completedInWindow.toFloat() / windowSize.toFloat() else 0f

                HabitWithStats(
                    habit = habit,
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                    totalCompletions = habitCompletions.size,
                    completionRate30Days = rate.coerceIn(0f, 1f),
                    isCompletedForDate = isCompleted,
                    last7DaysStatus = last7Days,
                    completedEpochDays = completedEpochDays
                )
            }
        }
    }

    fun getGlobalStats(): Flow<GlobalStats> {
        val todayEpoch = AppDate.today().epochDay

        return combine(allHabits, allCompletions) { habits, completions ->
            val totalHabits = habits.size
            val totalCompletionsAllTime = completions.size
            val todayCompletions = completions.count { it.dateEpochDay == todayEpoch }

            val completionsByHabit = completions.groupBy { it.habitId }
            var topStreak = 0
            var bestHabit: String? = null
            var bestStreakRecord = 0

            habits.forEach { habit ->
                val set = completionsByHabit[habit.id]?.map { it.dateEpochDay }?.toSet() ?: emptySet()
                val streak = calculateCurrentStreak(set, todayEpoch)
                val best = calculateBestStreak(set, streak)
                if (streak > topStreak) {
                    topStreak = streak
                }
                if (best > bestStreakRecord) {
                    bestStreakRecord = best
                    bestHabit = habit.title
                }
            }

            val overallRate = if (totalHabits > 0) {
                todayCompletions.toFloat() / totalHabits.toFloat()
            } else 0f

            GlobalStats(
                totalHabits = totalHabits,
                totalCompletionsAllTime = totalCompletionsAllTime,
                todayCompletedCount = todayCompletions,
                todayTotalCount = totalHabits,
                longestActiveStreak = topStreak,
                overallSuccessRate = overallRate.coerceIn(0f, 1f),
                bestHabitTitle = bestHabit
            )
        }
    }

    private fun calculateCurrentStreak(completedEpochDays: Set<Long>, todayEpoch: Long): Int {
        if (completedEpochDays.isEmpty()) return 0

        // If completed today, streak starts at 1 and counts backwards
        if (completedEpochDays.contains(todayEpoch)) {
            var streak = 1
            var checkEpoch = todayEpoch - 1
            while (completedEpochDays.contains(checkEpoch)) {
                streak++
                checkEpoch--
            }
            return streak
        }

        // If completed yesterday, current streak continues from yesterday!
        if (completedEpochDays.contains(todayEpoch - 1)) {
            var streak = 1
            var checkEpoch = todayEpoch - 2
            while (completedEpochDays.contains(checkEpoch)) {
                streak++
                checkEpoch--
            }
            return streak
        }

        return 0
    }

    private fun calculateBestStreak(completedEpochDays: Set<Long>, currentStreak: Int): Int {
        if (completedEpochDays.isEmpty()) return 0
        val sortedDays = completedEpochDays.sorted()

        var maxStreak = 1
        var runningStreak = 1

        for (i in 1 until sortedDays.size) {
            if (sortedDays[i] == sortedDays[i - 1] + 1) {
                runningStreak++
                if (runningStreak > maxStreak) {
                    maxStreak = runningStreak
                }
            } else if (sortedDays[i] > sortedDays[i - 1] + 1) {
                runningStreak = 1
            }
        }

        return maxOf(maxStreak, currentStreak)
    }
}
