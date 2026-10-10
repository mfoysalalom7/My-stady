package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class HabitColorOption(
    val hex: String,
    val name: String,
    val color: Color
)

data class HabitIconOption(
    val key: String,
    val name: String,
    val icon: ImageVector
)

object HabitThemeData {
    val ColorOptions = listOf(
        HabitColorOption("#10B981", "Emerald", Color(0xFF10B981)),
        HabitColorOption("#06B6D4", "Cyan", Color(0xFF06B6D4)),
        HabitColorOption("#3B82F6", "Blue", Color(0xFF3B82F6)),
        HabitColorOption("#6366F1", "Indigo", Color(0xFF6366F1)),
        HabitColorOption("#8B5CF6", "Purple", Color(0xFF8B5CF6)),
        HabitColorOption("#EC4899", "Pink", Color(0xFFEC4899)),
        HabitColorOption("#F43F5E", "Rose", Color(0xFFF43F5E)),
        HabitColorOption("#F97316", "Orange", Color(0xFFF97316)),
        HabitColorOption("#F59E0B", "Amber", Color(0xFFF59E0B)),
        HabitColorOption("#84CC16", "Lime", Color(0xFF84CC16))
    )

    val IconOptions = listOf(
        HabitIconOption("flame", "Streak", Icons.Rounded.LocalFireDepartment),
        HabitIconOption("water", "Hydration", Icons.Rounded.WaterDrop),
        HabitIconOption("fitness", "Exercise", Icons.Rounded.FitnessCenter),
        HabitIconOption("meditation", "Mindfulness", Icons.Rounded.SelfImprovement),
        HabitIconOption("book", "Reading", Icons.Rounded.MenuBook),
        HabitIconOption("code", "Coding", Icons.Rounded.Code),
        HabitIconOption("run", "Running", Icons.Rounded.DirectionsRun),
        HabitIconOption("sun", "Morning", Icons.Rounded.WbSunny),
        HabitIconOption("moon", "Sleep", Icons.Rounded.Bedtime),
        HabitIconOption("heart", "Health", Icons.Rounded.Favorite),
        HabitIconOption("star", "Goal", Icons.Rounded.Star),
        HabitIconOption("smile", "Mood", Icons.Rounded.SentimentSatisfied),
        HabitIconOption("work", "Focus", Icons.Rounded.LaptopMac),
        HabitIconOption("food", "Nutrition", Icons.Rounded.Restaurant),
        HabitIconOption("brush", "Creativity", Icons.Rounded.Palette),
        HabitIconOption("savings", "Finance", Icons.Rounded.Savings)
    )

    val Categories = listOf(
        "Health",
        "Mindfulness",
        "Fitness",
        "Learning",
        "Productivity",
        "Personal"
    )

    val TimeOfDayOptions = listOf(
        "ANYTIME" to "Anytime",
        "MORNING" to "Morning",
        "AFTERNOON" to "Afternoon",
        "EVENING" to "Evening"
    )

    fun getIcon(key: String): ImageVector {
        return IconOptions.firstOrNull { it.key == key }?.icon ?: Icons.Rounded.LocalFireDepartment
    }

    fun parseColor(hex: String): Color {
        return try {
            val clean = hex.removePrefix("#")
            val colorLong = clean.toLong(16)
            if (clean.length == 6) {
                Color(0xFF000000 or colorLong)
            } else {
                Color(colorLong)
            }
        } catch (_: Exception) {
            Color(0xFF10B981)
        }
    }
}
