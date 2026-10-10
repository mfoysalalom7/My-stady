package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitCompletionEntity
import com.example.data.model.GlobalStats
import com.example.data.model.HabitWithStats
import com.example.ui.components.HabitCard
import com.example.ui.components.WeeklyStrip
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.FlameYellow
import com.example.ui.theme.SuccessGreen
import com.example.util.AppDate

@Composable
fun TodayScreen(
    selectedDate: AppDate,
    onDateSelected: (AppDate) -> Unit,
    habitsWithStats: List<HabitWithStats>,
    filteredHabits: List<HabitWithStats>,
    allCompletions: List<HabitCompletionEntity>,
    globalStats: GlobalStats,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    selectedTimeFilter: String?,
    onTimeFilterSelected: (String?) -> Unit,
    onToggleCheck: (habitId: Long, date: AppDate) -> Unit,
    onHabitClick: (HabitWithStats) -> Unit,
    onEditHabit: (HabitWithStats) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onAddNewHabit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { AppDate.today() }
    val isViewingToday = selectedDate.isSameDay(today)

    // Completion numbers for selected date
    val totalCount = habitsWithStats.size
    val completedCount = habitsWithStats.count { it.isCompletedForDate }
    val progressRatio = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val isAllDone = totalCount > 0 && completedCount == totalCount

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNewHabit,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("New Habit", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_habit_fab")
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("today_screen_list"),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Title & Subtitle Header
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isViewingToday) "Today's Checklist" else selectedDate.formatDisplay(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 26.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (isViewingToday) selectedDate.formatDisplay() else "Log past or future habit targets",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Top flame streak pill
                        if (globalStats.longestActiveStreak > 0) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = FlameOrange.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, FlameOrange.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = FlameOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${globalStats.longestActiveStreak}d streak",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = FlameOrange
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Scannable Weekly Day Strip
            item {
                WeeklyStrip(
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected,
                    allCompletions = allCompletions,
                    totalHabitsCount = totalCount
                )
            }

            // Motivation & Progress Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAllDone) {
                            SuccessGreen.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isAllDone) SuccessGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when {
                                        totalCount == 0 -> "No Habits Yet"
                                        isAllDone -> "🎉 Perfection! All Done Today!"
                                        progressRatio >= 0.5f -> "🔥 Awesome momentum! Keep it going!"
                                        completedCount > 0 -> "💪 Great start, keep ticking off habits!"
                                        else -> "☀️ Rise and shine! Time to conquer today."
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = if (isAllDone) SuccessGreen else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (totalCount > 0) {
                                        "$completedCount of $totalCount habits completed (${(progressRatio * 100).toInt()}%)"
                                    } else {
                                        "Tap + to create your first habit"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Progress percentage badge
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isAllDone) SuccessGreen else MaterialTheme.colorScheme.primaryContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${(progressRatio * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isAllDone) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        if (totalCount > 0) {
                            Spacer(modifier = Modifier.height(14.dp))
                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isAllDone) SuccessGreen else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick Filter Chips (Time of Day & Category)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Time of Day filters
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedTimeFilter == null,
                                onClick = { onTimeFilterSelected(null) },
                                label = { Text("All Times") },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        listOf("MORNING" to "Morning", "AFTERNOON" to "Afternoon", "EVENING" to "Evening").forEach { (key, label) ->
                            item {
                                FilterChip(
                                    selected = selectedTimeFilter == key,
                                    onClick = { onTimeFilterSelected(key) },
                                    label = { Text(label) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Habit Cards Section
            if (filteredHabits.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EventAvailable,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No habits match this filter",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try clearing filters or add a new daily habit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onAddNewHabit,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Habit")
                            }
                        }
                    }
                }
            } else {
                items(
                    items = filteredHabits,
                    key = { it.habit.id }
                ) { habitWithStats ->
                    HabitCard(
                        habitWithStats = habitWithStats,
                        onToggleCheck = {
                            onToggleCheck(habitWithStats.habit.id, selectedDate)
                        },
                        onCardClick = { onHabitClick(habitWithStats) },
                        onEditClick = { onEditHabit(habitWithStats) },
                        onDeleteClick = { onDeleteHabit(habitWithStats.habit.id) }
                    )
                }
            }
        }
    }
}
