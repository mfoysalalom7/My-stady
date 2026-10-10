package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.HabitCompletionEntity
import com.example.data.local.HabitDatabase
import com.example.data.local.HabitEntity
import com.example.data.repository.HabitRepository
import com.example.util.AppDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: HabitDatabase
    private lateinit var repository: HabitRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HabitDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = HabitRepository(db.habitDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("HabitPulse", appName)
    }

    @Test
    fun `appDate arithmetic and formatting`() {
        val date = AppDate(2026, 10, 10)
        assertEquals(2026, date.year)
        assertEquals(10, date.month)
        assertEquals(10, date.day)
        assertEquals("Oct 10, 2026", date.formatDisplay())
        assertEquals(31, date.daysInMonth())

        val nextDay = date.plusDays(1)
        assertEquals(11, nextDay.day)

        val prevDay = date.minusDays(1)
        assertEquals(9, prevDay.day)
    }

    @Test
    fun `seeding and streak tracking works correctly`() = runBlocking {
        repository.checkAndSeedInitialData()

        val habits = repository.allHabits.first()
        assertTrue("Seed data should populate habits", habits.isNotEmpty())
        assertEquals(4, habits.size)

        val today = AppDate.today()
        val habitsWithStats = repository.getHabitsWithStats(today).first()
        assertEquals(4, habitsWithStats.size)

        // Find water habit which was seeded with a 6-day streak
        val waterHabit = habitsWithStats.find { it.habit.title == "Drink 2L Water" }
        assertNotNull(waterHabit)
        assertTrue("Water habit should have active streak", waterHabit!!.currentStreak >= 5)
        assertTrue("Water habit should be completed today", waterHabit.isCompletedForDate)

        // Toggle completion on today
        val stillCompleted = repository.toggleHabitCompletion(waterHabit.habit.id, today.epochDay)
        assertFalse("Toggling completed habit should uncheck it", stillCompleted)

        val updatedStats = repository.getHabitsWithStats(today).first()
        val updatedWater = updatedStats.find { it.habit.id == waterHabit.habit.id }!!
        assertFalse("Water habit should now be unchecked for today", updatedWater.isCompletedForDate)
    }

    @Test
    fun `inserting and deleting habit works`() = runBlocking {
        val today = AppDate.today()
        val newHabitId = repository.insertHabit(
            HabitEntity(
                title = "Yoga Routine",
                description = "Daily 15m stretch",
                colorHex = "#EC4899",
                iconKey = "heart",
                category = "Health",
                frequencyType = "DAILY",
                targetDaysPerWeek = 7,
                timeOfDay = "MORNING",
                createdAtEpochDay = today.epochDay
            )
        )

        assertTrue(newHabitId > 0)
        var allHabits = repository.allHabits.first()
        assertTrue(allHabits.any { it.id == newHabitId && it.title == "Yoga Routine" })

        repository.deleteHabit(newHabitId)
        allHabits = repository.allHabits.first()
        assertFalse(allHabits.any { it.id == newHabitId })
    }
}
