package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitCompletionEntity
import com.example.util.AppDate

@Composable
fun WeeklyStrip(
    selectedDate: AppDate,
    onDateSelected: (AppDate) -> Unit,
    allCompletions: List<HabitCompletionEntity>,
    totalHabitsCount: Int,
    modifier: Modifier = Modifier
) {
    val today = remember { AppDate.today() }

    // Display current week containing selectedDate: Monday to Sunday
    val weekDays = remember(selectedDate) {
        val dayOfWeek = selectedDate.dayOfWeek // 1 (Mon) to 7 (Sun)
        val monday = selectedDate.minusDays(dayOfWeek - 1)
        (0..6).map { monday.plusDays(it) }
    }

    val completionsByDay = remember(allCompletions) {
        allCompletions.groupBy { it.dateEpochDay }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${weekDays.first().formatShort()} – ${weekDays.last().formatShort()}, ${selectedDate.year}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onDateSelected(selectedDate.minusDays(7)) },
                    modifier = Modifier.size(32.dp).testTag("prev_week_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = "Previous Week",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!selectedDate.isSameDay(today)) {
                    TextButton(
                        onClick = { onDateSelected(today) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp).testTag("jump_today_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Today,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                IconButton(
                    onClick = { onDateSelected(selectedDate.plusDays(7)) },
                    modifier = Modifier.size(32.dp).testTag("next_week_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Next Week",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach { date ->
                val isSelected = date.isSameDay(selectedDate)
                val isToday = date.isSameDay(today)
                val completedCount = completionsByDay[date.epochDay]?.size ?: 0
                val allCompleted = totalHabitsCount > 0 && completedCount >= totalHabitsCount
                val partiallyCompleted = completedCount > 0

                val pillBackground = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else -> Color.Transparent
                }

                val textColor = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(pillBackground)
                        .clickable { onDateSelected(date) }
                        .padding(vertical = 8.dp, horizontal = 2.dp)
                        .testTag("week_day_${date.day}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.dayOfWeekShort,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = textColor.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${date.day}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Medium
                        ),
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Completion progress dot or ring
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected && allCompleted -> Color.White
                                    isSelected && partiallyCompleted -> Color.White.copy(alpha = 0.6f)
                                    allCompleted -> MaterialTheme.colorScheme.primary
                                    partiallyCompleted -> MaterialTheme.colorScheme.tertiary
                                    else -> Color.Transparent
                                }
                            )
                    )
                }
            }
        }
    }
}
