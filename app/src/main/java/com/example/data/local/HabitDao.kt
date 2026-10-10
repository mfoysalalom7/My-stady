package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY displayOrder ASC, id ASC")
    fun getAllActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Long)

    @Query("DELETE FROM habit_completions WHERE habitId = :id")
    suspend fun deleteCompletionsForHabit(id: Long)

    @Transaction
    suspend fun deleteHabitAndCompletions(id: Long) {
        deleteCompletionsForHabit(id)
        deleteHabit(id)
    }

    // Completions
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY dateEpochDay DESC")
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY dateEpochDay DESC")
    suspend fun getCompletionsListForHabit(habitId: Long): List<HabitCompletionEntity>

    @Query("SELECT * FROM habit_completions ORDER BY dateEpochDay DESC")
    fun getAllCompletions(): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE dateEpochDay = :dateEpochDay")
    fun getCompletionsForDate(dateEpochDay: Long): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay")
    fun getCompletionsBetweenDates(startEpochDay: Long, endEpochDay: Long): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getCompletion(habitId: Long, dateEpochDay: Long): HabitCompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletionEntity): Long

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay")
    suspend fun deleteCompletion(habitId: Long, dateEpochDay: Long)

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getHabitCount(): Int
}
