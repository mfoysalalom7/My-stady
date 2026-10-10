package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.util.HabitThemeData

data class HabitPreset(
    val title: String,
    val description: String,
    val category: String,
    val iconKey: String,
    val colorHex: String,
    val timeOfDay: String
)

val QuickPresets = listOf(
    HabitPreset("Drink 2L Water", "8 glasses daily", "Health", "water", "#06B6D4", "ANYTIME"),
    HabitPreset("Morning Run", "Cardio 5km or jog", "Fitness", "run", "#10B981", "MORNING"),
    HabitPreset("Meditate 10m", "Deep mindfulness & breath", "Mindfulness", "meditation", "#8B5CF6", "MORNING"),
    HabitPreset("Read 20 Pages", "Personal growth or books", "Learning", "book", "#F59E0B", "EVENING"),
    HabitPreset("Code 1 Hour", "Side project & learning", "Productivity", "code", "#3B82F6", "AFTERNOON"),
    HabitPreset("Sleep Early", "In bed by 11:00 PM", "Health", "moon", "#6366F1", "EVENING"),
    HabitPreset("Daily Walk", "30 mins fresh air", "Health", "heart", "#EC4899", "ANYTIME")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitSheet(
    habitToEdit: HabitEntity?,
    onDismiss: () -> Unit,
    onSaveHabit: (
        id: Long,
        title: String,
        description: String,
        colorHex: String,
        iconKey: String,
        category: String,
        frequencyType: String,
        targetDaysPerWeek: Int,
        timeOfDay: String
    ) -> Unit
) {
    var title by remember(habitToEdit) { mutableStateOf(habitToEdit?.title ?: "") }
    var description by remember(habitToEdit) { mutableStateOf(habitToEdit?.description ?: "") }
    var selectedColorHex by remember(habitToEdit) { mutableStateOf(habitToEdit?.colorHex ?: "#10B981") }
    var selectedIconKey by remember(habitToEdit) { mutableStateOf(habitToEdit?.iconKey ?: "flame") }
    var selectedCategory by remember(habitToEdit) { mutableStateOf(habitToEdit?.category ?: "Health") }
    var selectedTimeOfDay by remember(habitToEdit) { mutableStateOf(habitToEdit?.timeOfDay ?: "ANYTIME") }
    var selectedFrequency by remember(habitToEdit) { mutableStateOf(habitToEdit?.frequencyType ?: "DAILY") }
    var targetDays by remember(habitToEdit) { mutableIntStateOf(habitToEdit?.targetDaysPerWeek ?: 7) }

    var titleError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (habitToEdit == null) "New Habit" else "Edit Habit",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close")
                }
            }

            // Quick Preset Suggestions (only when adding new habit)
            if (habitToEdit == null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Quick Inspiration",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(QuickPresets) { preset ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.clickable {
                                title = preset.title
                                description = preset.description
                                selectedCategory = preset.category
                                selectedIconKey = preset.iconKey
                                selectedColorHex = preset.colorHex
                                selectedTimeOfDay = preset.timeOfDay
                                titleError = false
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = HabitThemeData.getIcon(preset.iconKey),
                                    contentDescription = null,
                                    tint = HabitThemeData.parseColor(preset.colorHex),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) titleError = false
                },
                label = { Text("Habit Name *") },
                placeholder = { Text("e.g. Morning Meditation, Drink Water") },
                isError = titleError,
                supportingText = if (titleError) {
                    { Text("Habit name is required") }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habit_title_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Motivation or Goal (Optional)") },
                placeholder = { Text("e.g. 10 minutes daily for clarity") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habit_desc_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Color Selection
            Text(
                text = "Theme Color",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(HabitThemeData.ColorOptions) { option ->
                    val isSelected = option.hex.equals(selectedColorHex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(option.color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColorHex = option.hex }
                            .testTag("color_option_${option.hex}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Icon Selection
            Text(
                text = "Habit Icon",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(HabitThemeData.IconOptions) { iconOption ->
                    val isSelected = iconOption.key == selectedIconKey
                    val themeColor = HabitThemeData.parseColor(selectedColorHex)

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) themeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) themeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedIconKey = iconOption.key }
                            .testTag("icon_option_${iconOption.key}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconOption.icon,
                            contentDescription = iconOption.name,
                            tint = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Category Selection
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(HabitThemeData.Categories) { cat ->
                    val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Time of Day
            Text(
                text = "Time of Day",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HabitThemeData.TimeOfDayOptions.forEach { (key, label) ->
                    val isSelected = key == selectedTimeOfDay
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTimeOfDay = key },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save Action Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onSaveHabit(
                            habitToEdit?.id ?: 0L,
                            title,
                            description,
                            selectedColorHex,
                            selectedIconKey,
                            selectedCategory,
                            selectedFrequency,
                            targetDays,
                            selectedTimeOfDay
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_habit_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HabitThemeData.parseColor(selectedColorHex)
                )
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (habitToEdit == null) "Create Habit" else "Save Changes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
