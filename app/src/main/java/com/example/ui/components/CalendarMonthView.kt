package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitCompletionEntity
import com.example.data.local.HabitEntity
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.SuccessGreen
import com.example.util.AppDate
import com.example.util.HabitThemeData

@Composable
fun CalendarMonthView(
    monthDate: AppDate, // 1st of the month
    selectedDate: AppDate,
    onDateSelected: (AppDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrentMonth: () -> Unit,
    completions: List<HabitCompletionEntity>,
    habits: List<HabitEntity>,
    filteredHabitId: Long?,
    modifier: Modifier = Modifier
) {
    val today = remember { AppDate.today() }
    val daysInMonth = remember(monthDate) { monthDate.daysInMonth() }
    val firstDayOfWeek = remember(monthDate) { monthDate.dayOfWeek } // 1 (Mon) to 7 (Sun)

    // Map of epochDay -> list of completions for that day
    val completionsByDay = remember(completions, filteredHabitId) {
        val filtered = if (filteredHabitId != null) {
            completions.filter { it.habitId == filteredHabitId }
        } else {
            completions
        }
        filtered.groupBy { it.dateEpochDay }
    }

    val totalHabitsCount = habits.size
    val filteredHabit = habits.firstOrNull { it.id == filteredHabitId }
    val activeColor = remember(filteredHabit) {
        if (filteredHabit != null) {
            HabitThemeData.parseColor(filteredHabit.colorHex)
        } else {
            SuccessGreen
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Month Header & Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${monthDate.monthNameLong} ${monthDate.year}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (filteredHabit != null) filteredHabit.title else "All Habits Streak Heatmap",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPreviousMonth,
                        modifier = Modifier.size(36.dp).testTag("cal_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = "Previous Month"
                        )
                    }

                    if (monthDate.month != today.month || monthDate.year != today.year) {
                        TextButton(
                            onClick = onCurrentMonth,
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(32.dp).testTag("cal_today_btn")
                        ) {
                            Text(
                                "Today",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.size(36.dp).testTag("cal_next_month")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Next Month"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day of Week Header Row (Mon to Sun)
            Row(modifier = Modifier.fillMaxWidth()) {
                val weekDayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                weekDayLabels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Month Grid Calculations
            val totalLeadingBlanks = firstDayOfWeek - 1 // 1 (Mon) has 0 blanks
            val totalCells = totalLeadingBlanks + daysInMonth
            val totalRows = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (rowIndex in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (colIndex in 0..6) {
                            val cellIndex = rowIndex * 7 + colIndex
                            val dayNumber = cellIndex - totalLeadingBlanks + 1

                            if (dayNumber in 1..daysInMonth) {
                                val cellDate = AppDate(monthDate.year, monthDate.month, dayNumber)
                                val isSelected = cellDate.isSameDay(selectedDate)
                                val isToday = cellDate.isSameDay(today)

                                val dayCompletions = completionsByDay[cellDate.epochDay] ?: emptyList()
                                val completionCount = dayCompletions.size

                                val isCompletedForFiltered = if (filteredHabitId != null) {
                                    completionCount > 0
                                } else false

                                val completionRatio = if (totalHabitsCount > 0) {
                                    (completionCount.toFloat() / totalHabitsCount.toFloat()).coerceIn(0f, 1f)
                                } else 0f

                                // Heatmap styling
                                val cellBgColor = when {
                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                    filteredHabitId != null && isCompletedForFiltered -> activeColor.copy(alpha = 0.2f)
                                    filteredHabitId == null && completionRatio >= 1.0f -> SuccessGreen.copy(alpha = 0.25f)
                                    filteredHabitId == null && completionRatio >= 0.5f -> SuccessGreen.copy(alpha = 0.15f)
                                    filteredHabitId == null && completionCount > 0 -> SuccessGreen.copy(alpha = 0.08f)
                                    isToday -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    else -> Color.Transparent
                                }

                                val cellBorderColor = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    filteredHabitId != null && isCompletedForFiltered -> activeColor.copy(alpha = 0.6f)
                                    filteredHabitId == null && completionRatio >= 1.0f -> SuccessGreen.copy(alpha = 0.7f)
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(cellBgColor)
                                        .border(
                                            width = if (isSelected) 2.dp else if (cellBorderColor != Color.Transparent) 1.dp else 0.dp,
                                            color = cellBorderColor,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onDateSelected(cellDate) }
                                        .testTag("cal_cell_${dayNumber}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$dayNumber",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 14.sp
                                            ),
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                                isToday -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )

                                        // Mini indicator dot or flame
                                        if (filteredHabitId != null && isCompletedForFiltered) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(activeColor)
                                            )
                                        } else if (filteredHabitId == null && completionCount > 0) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.padding(top = 1.dp)
                                            ) {
                                                if (completionRatio >= 1.0f) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.LocalFireDepartment,
                                                        contentDescription = null,
                                                        tint = FlameOrange,
                                                        modifier = Modifier.size(8.dp)
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(SuccessGreen)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Empty spacer cell
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.25f))
                            .border(1.dp, SuccessGreen, CircleShape)
                    )
                    Text(
                        text = "100% Done",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.1f))
                    )
                    Text(
                        text = "Partial",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Text(
                        text = "Selected",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
