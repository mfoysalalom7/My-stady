package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HabitCompletionEntity
import com.example.data.local.HabitDatabase
import com.example.data.local.HabitEntity
import com.example.data.model.GlobalStats
import com.example.data.model.HabitWithStats
import com.example.data.repository.HabitRepository
import com.example.util.AppDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitRepository

    // Date selected on the Today screen (user can browse past/future days in week strip)
    private val _selectedDate = MutableStateFlow(AppDate.today())
    val selectedDate: StateFlow<AppDate> = _selectedDate.asStateFlow()

    // Filter for Today view: null = all
    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    private val _timeFilter = MutableStateFlow<String?>(null)
    val timeFilter: StateFlow<String?> = _timeFilter.asStateFlow()

    // Month browsing state for the Streak Calendar
    private val _calendarMonth = MutableStateFlow(
        AppDate(AppDate.today().year, AppDate.today().month, 1)
    )
    val calendarMonth: StateFlow<AppDate> = _calendarMonth.asStateFlow()

    // Date tapped inside the Calendar view
    private val _calendarSelectedDate = MutableStateFlow(AppDate.today())
    val calendarSelectedDate: StateFlow<AppDate> = _calendarSelectedDate.asStateFlow()

    // Habit filter for Calendar view (null = aggregate all habits)
    private val _calendarHabitFilter = MutableStateFlow<Long?>(null)
    val calendarHabitFilter: StateFlow<Long?> = _calendarHabitFilter.asStateFlow()

    init {
        val db = HabitDatabase.getInstance(application)
        repository = HabitRepository(db.habitDao())

        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    val allHabits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCompletions: StateFlow<List<HabitCompletionEntity>> = repository.allCompletions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val habitsWithStats: StateFlow<List<HabitWithStats>> = _selectedDate
        .flatMapLatest { date ->
            repository.getHabitsWithStats(date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredHabitsWithStats: StateFlow<List<HabitWithStats>> = combine(
        habitsWithStats,
        _categoryFilter,
        _timeFilter
    ) { list, category, time ->
        list.filter { item ->
            val matchCategory = category == null || item.habit.category.equals(category, ignoreCase = true)
            val matchTime = time == null || item.habit.timeOfDay.equals(time, ignoreCase = true) || item.habit.timeOfDay == "ANYTIME"
            matchCategory && matchTime
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalStats: StateFlow<GlobalStats> = repository.getGlobalStats()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            GlobalStats(0, 0, 0, 0, 0, 0f)
        )

    fun selectDate(date: AppDate) {
        _selectedDate.value = date
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = if (_categoryFilter.value == category) null else category
    }

    fun setTimeFilter(time: String?) {
        _timeFilter.value = if (_timeFilter.value == time) null else time
    }

    fun toggleHabitCompletion(habitId: Long, date: AppDate) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, date.epochDay)
        }
    }

    fun selectCalendarDate(date: AppDate) {
        _calendarSelectedDate.value = date
    }

    fun setCalendarHabitFilter(habitId: Long?) {
        _calendarHabitFilter.value = habitId
    }

    fun previousCalendarMonth() {
        val current = _calendarMonth.value
        val (newYear, newMonth) = if (current.month == 1) {
            Pair(current.year - 1, 12)
        } else {
            Pair(current.year, current.month - 1)
        }
        _calendarMonth.value = AppDate(newYear, newMonth, 1)
    }

    fun nextCalendarMonth() {
        val current = _calendarMonth.value
        val (newYear, newMonth) = if (current.month == 12) {
            Pair(current.year + 1, 1)
        } else {
            Pair(current.year, current.month + 1)
        }
        _calendarMonth.value = AppDate(newYear, newMonth, 1)
    }

    fun goToCurrentMonth() {
        val today = AppDate.today()
        _calendarMonth.value = AppDate(today.year, today.month, 1)
        _calendarSelectedDate.value = today
    }

    fun saveHabit(
        id: Long = 0,
        title: String,
        description: String,
        colorHex: String,
        iconKey: String,
        category: String,
        frequencyType: String,
        targetDaysPerWeek: Int,
        timeOfDay: String
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                val todayEpoch = AppDate.today().epochDay
                repository.insertHabit(
                    HabitEntity(
                        title = title.trim(),
                        description = description.trim(),
                        colorHex = colorHex,
                        iconKey = iconKey,
                        category = category,
                        frequencyType = frequencyType,
                        targetDaysPerWeek = targetDaysPerWeek,
                        timeOfDay = timeOfDay,
                        createdAtEpochDay = todayEpoch
                    )
                )
            } else {
                val existing = habitsWithStats.value.firstOrNull { it.habit.id == id }?.habit
                val createdAt = existing?.createdAtEpochDay ?: AppDate.today().epochDay
                val order = existing?.displayOrder ?: 0
                repository.updateHabit(
                    HabitEntity(
                        id = id,
                        title = title.trim(),
                        description = description.trim(),
                        colorHex = colorHex,
                        iconKey = iconKey,
                        category = category,
                        frequencyType = frequencyType,
                        targetDaysPerWeek = targetDaysPerWeek,
                        timeOfDay = timeOfDay,
                        createdAtEpochDay = createdAt,
                        displayOrder = order
                    )
                )
            }
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }
}
