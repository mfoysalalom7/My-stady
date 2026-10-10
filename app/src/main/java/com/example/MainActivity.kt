package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.HabitEntity
import com.example.data.model.HabitWithStats
import com.example.ui.components.AddEditHabitSheet
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.HabitDetailSheet
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.HabitPulseTheme
import com.example.ui.viewmodel.HabitViewModel
import com.example.util.AppDate

enum class HabitPulseTab(val title: String) {
    TODAY("Today"),
    CALENDAR("Calendar"),
    STATS("Stats")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitPulseTheme {
                HabitPulseApp()
            }
        }
    }
}

@Composable
fun HabitPulseApp(viewModel: HabitViewModel = viewModel()) {
    var currentTab by remember { mutableStateOf(HabitPulseTab.TODAY) }

    // Modal bottom sheet states
    var showAddEditSheet by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<HabitEntity?>(null) }
    var selectedHabitDetailId by remember { mutableStateOf<Long?>(null) }

    // Reactive states from ViewModel
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val habitsWithStats by viewModel.habitsWithStats.collectAsStateWithLifecycle()
    val filteredHabits by viewModel.filteredHabitsWithStats.collectAsStateWithLifecycle()
    val globalStats by viewModel.globalStats.collectAsStateWithLifecycle()
    val allHabits by viewModel.allHabits.collectAsStateWithLifecycle()
    val allCompletions by viewModel.allCompletions.collectAsStateWithLifecycle()

    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val timeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()

    val calendarMonth by viewModel.calendarMonth.collectAsStateWithLifecycle()
    val calendarSelectedDate by viewModel.calendarSelectedDate.collectAsStateWithLifecycle()
    val calendarHabitFilter by viewModel.calendarHabitFilter.collectAsStateWithLifecycle()

    // Find currently selected habit for detail sheet if open
    val habitForDetail = remember(habitsWithStats, selectedHabitDetailId) {
        habitsWithStats.firstOrNull { it.habit.id == selectedHabitDetailId }
    }

    // Handle back button navigation
    BackHandler(enabled = currentTab != HabitPulseTab.TODAY || showAddEditSheet || selectedHabitDetailId != null) {
        when {
            showAddEditSheet -> showAddEditSheet = false
            selectedHabitDetailId != null -> selectedHabitDetailId = null
            currentTab != HabitPulseTab.TODAY -> currentTab = HabitPulseTab.TODAY
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == HabitPulseTab.TODAY,
                    onClick = { currentTab = HabitPulseTab.TODAY },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == HabitPulseTab.TODAY) Icons.Rounded.CheckCircle else Icons.Rounded.CheckCircleOutline,
                            contentDescription = "Today"
                        )
                    },
                    label = { Text("Today", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_today")
                )

                NavigationBarItem(
                    selected = currentTab == HabitPulseTab.CALENDAR,
                    onClick = { currentTab = HabitPulseTab.CALENDAR },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = "Calendar"
                        )
                    },
                    label = { Text("Calendar", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_calendar")
                )

                NavigationBarItem(
                    selected = currentTab == HabitPulseTab.STATS,
                    onClick = { currentTab = HabitPulseTab.STATS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == HabitPulseTab.STATS) Icons.Rounded.Leaderboard else Icons.Rounded.BarChart,
                            contentDescription = "Stats"
                        )
                    },
                    label = { Text("Stats", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_stats")
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            HabitPulseTab.TODAY -> {
                TodayScreen(
                    selectedDate = selectedDate,
                    onDateSelected = { viewModel.selectDate(it) },
                    habitsWithStats = habitsWithStats,
                    filteredHabits = filteredHabits,
                    allCompletions = allCompletions,
                    globalStats = globalStats,
                    selectedCategory = categoryFilter,
                    onCategorySelected = { viewModel.setCategoryFilter(it) },
                    selectedTimeFilter = timeFilter,
                    onTimeFilterSelected = { viewModel.setTimeFilter(it) },
                    onToggleCheck = { id, date -> viewModel.toggleHabitCompletion(id, date) },
                    onHabitClick = { selectedHabitDetailId = it.habit.id },
                    onEditHabit = {
                        habitToEdit = it.habit
                        showAddEditSheet = true
                    },
                    onDeleteHabit = { viewModel.deleteHabit(it) },
                    onAddNewHabit = {
                        habitToEdit = null
                        showAddEditSheet = true
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            HabitPulseTab.CALENDAR -> {
                CalendarScreen(
                    calendarMonth = calendarMonth,
                    selectedCalendarDate = calendarSelectedDate,
                    onDateSelected = { viewModel.selectCalendarDate(it) },
                    onPreviousMonth = { viewModel.previousCalendarMonth() },
                    onNextMonth = { viewModel.nextCalendarMonth() },
                    onCurrentMonth = { viewModel.goToCurrentMonth() },
                    habits = allHabits,
                    allCompletions = allCompletions,
                    filteredHabitId = calendarHabitFilter,
                    onSelectHabitFilter = { viewModel.setCalendarHabitFilter(it) },
                    onToggleCheck = { id, date -> viewModel.toggleHabitCompletion(id, date) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            HabitPulseTab.STATS -> {
                StatsScreen(
                    globalStats = globalStats,
                    habitsWithStats = habitsWithStats,
                    onHabitClick = { selectedHabitDetailId = it.habit.id },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    // Add / Edit Habit Modal Bottom Sheet
    if (showAddEditSheet) {
        AddEditHabitSheet(
            habitToEdit = habitToEdit,
            onDismiss = {
                showAddEditSheet = false
                habitToEdit = null
            },
            onSaveHabit = { id, title, description, colorHex, iconKey, category, frequencyType, targetDays, timeOfDay ->
                viewModel.saveHabit(
                    id = id,
                    title = title,
                    description = description,
                    colorHex = colorHex,
                    iconKey = iconKey,
                    category = category,
                    frequencyType = frequencyType,
                    targetDaysPerWeek = targetDays,
                    timeOfDay = timeOfDay
                )
            }
        )
    }

    // Habit Detail & Stats Modal Bottom Sheet
    if (habitForDetail != null) {
        HabitDetailSheet(
            habitWithStats = habitForDetail,
            onDismiss = { selectedHabitDetailId = null },
            onToggleToday = {
                viewModel.toggleHabitCompletion(habitForDetail.habit.id, AppDate.today())
            },
            onEditHabit = {
                val habit = habitForDetail.habit
                selectedHabitDetailId = null
                habitToEdit = habit
                showAddEditSheet = true
            },
            onDeleteHabit = {
                viewModel.deleteHabit(habitForDetail.habit.id)
                selectedHabitDetailId = null
            }
        )
    }
}
