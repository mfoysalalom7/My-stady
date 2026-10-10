package com.example.util

import java.util.Calendar

/**
 * Lightweight, rock-solid date representation compatible with all Android API levels.
 */
data class AppDate(
    val year: Int,
    val month: Int, // 1 to 12
    val day: Int    // 1 to 31
) : Comparable<AppDate> {

    val epochDay: Long by lazy {
        val cal = toCalendar()
        cal.timeInMillis / 86400000L
    }

    /**
     * Day of week: 1 (Mon) to 7 (Sun)
     */
    val dayOfWeek: Int
        get() {
            val cal = toCalendar()
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            return if (dow == Calendar.SUNDAY) 7 else dow - 1
        }

    val dayOfWeekShort: String
        get() = when (dayOfWeek) {
            1 -> "Mon"
            2 -> "Tue"
            3 -> "Wed"
            4 -> "Thu"
            5 -> "Fri"
            6 -> "Sat"
            else -> "Sun"
        }

    val dayOfWeekLetter: String
        get() = when (dayOfWeek) {
            1 -> "M"
            2 -> "T"
            3 -> "W"
            4 -> "T"
            5 -> "F"
            6 -> "S"
            else -> "S"
        }

    val monthNameShort: String
        get() = when (month) {
            1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"; 5 -> "May"; 6 -> "Jun"
            7 -> "Jul"; 8 -> "Aug"; 9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; else -> "Dec"
        }

    val monthNameLong: String
        get() = when (month) {
            1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"; 5 -> "May"; 6 -> "June"
            7 -> "July"; 8 -> "August"; 9 -> "September"; 10 -> "October"; 11 -> "November"; else -> "December"
        }

    fun toCalendar(): Calendar {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    fun plusDays(days: Int): AppDate {
        val cal = toCalendar()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return fromCalendar(cal)
    }

    fun minusDays(days: Int): AppDate = plusDays(-days)

    fun daysInMonth(): Int {
        val cal = toCalendar()
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun isSameDay(other: AppDate): Boolean {
        return year == other.year && month == other.month && day == other.day
    }

    fun formatDisplay(): String {
        return "$monthNameShort $day, $year"
    }

    fun formatShort(): String {
        return "$monthNameShort $day"
    }

    override fun compareTo(other: AppDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    companion object {
        fun today(): AppDate = fromCalendar(Calendar.getInstance())

        fun fromCalendar(cal: Calendar): AppDate {
            return AppDate(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun fromEpochDay(epochDay: Long): AppDate {
            val cal = Calendar.getInstance()
            cal.timeInMillis = epochDay * 86400000L + 43200000L
            return fromCalendar(cal)
        }
    }
}
