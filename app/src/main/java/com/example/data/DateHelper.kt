package com.example.data

import java.text.DecimalFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

object DateHelper {
    private val monthNamesShort = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    private val monthNamesFull = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val dayNamesFull = arrayOf(
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    )

    private val dayNamesShort = arrayOf(
        "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"
    )

    private val currencyFormatter = DecimalFormat("#,##0.##")
    private val hoursFormatter = DecimalFormat("0.##")

    fun getTodayIso(): String {
        val cal = Calendar.getInstance()
        return calendarToIso(cal)
    }

    fun getCurrentYearMonth(): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        return Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun calendarToIso(cal: Calendar): String {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
    }

    fun isoToCalendar(dateIso: String): Calendar {
        val cal = Calendar.getInstance()
        val parts = dateIso.split("-")
        if (parts.size == 3) {
            val y = parts[0].toIntOrNull() ?: 2026
            val m = (parts[1].toIntOrNull() ?: 1) - 1
            val d = parts[2].toIntOrNull() ?: 1
            cal.set(y, m, d, 12, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
        }
        return cal
    }

    fun getYearMonthFromIso(dateIso: String): Pair<Int, Int> {
        val parts = dateIso.split("-")
        if (parts.size == 3) {
            val y = parts[0].toIntOrNull() ?: 2026
            val m = parts[1].toIntOrNull() ?: 1
            return Pair(y, m)
        }
        return getCurrentYearMonth()
    }

    /**
     * Returns 1 for Monday .. 6 for Saturday, 7 for Sunday.
     */
    fun getDayOfWeekIndex(dateIso: String): Int {
        val cal = isoToCalendar(dateIso)
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    fun getDayNameFull(dayIndex: Int): String = dayNamesFull[(dayIndex - 1).coerceIn(0, 6)]
    fun getDayNameShort(dayIndex: Int): String = dayNamesShort[(dayIndex - 1).coerceIn(0, 6)]

    /**
     * Given any dateIso, returns the Monday ISO date of that same Monday-Sunday week.
     */
    fun getMondayOfWeek(dateIso: String): String {
        val cal = isoToCalendar(dateIso)
        val dayIndex = getDayOfWeekIndex(dateIso)
        cal.add(Calendar.DAY_OF_MONTH, -(dayIndex - 1))
        return calendarToIso(cal)
    }

    fun addDays(dateIso: String, days: Int): String {
        val cal = isoToCalendar(dateIso)
        cal.add(Calendar.DAY_OF_MONTH, days)
        return calendarToIso(cal)
    }

    fun formatDayMonth(dateIso: String): String {
        val cal = isoToCalendar(dateIso)
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val m = monthNamesShort[cal.get(Calendar.MONTH).coerceIn(0, 11)]
        return String.format(Locale.US, "%02d %s", d, m)
    }

    fun formatFullDateWithDay(dateIso: String): String {
        val cal = isoToCalendar(dateIso)
        val dayShort = getDayNameShort(getDayOfWeekIndex(dateIso)).lowercase()
            .replaceFirstChar { it.uppercase() }
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val m = monthNamesShort[cal.get(Calendar.MONTH).coerceIn(0, 11)]
        val y = cal.get(Calendar.YEAR)
        return String.format(Locale.US, "%s, %02d %s %d", dayShort, d, m, y)
    }

    fun formatWeekRangeLabel(mondayIso: String, saturdayIso: String): String {
        val monCal = isoToCalendar(mondayIso)
        val satCal = isoToCalendar(saturdayIso)
        val monD = monCal.get(Calendar.DAY_OF_MONTH)
        val monM = monthNamesShort[monCal.get(Calendar.MONTH)]
        val satD = satCal.get(Calendar.DAY_OF_MONTH)
        val satM = monthNamesShort[satCal.get(Calendar.MONTH)]
        val satY = satCal.get(Calendar.YEAR)
        return String.format(Locale.US, "Mon, %02d %s – Sat, %02d %s %d", monD, monM, satD, satM, satY)
    }

    fun getMonthTitle(year: Int, month: Int): String {
        val mName = monthNamesFull[(month - 1).coerceIn(0, 11)]
        return "$mName $year"
    }

    fun getDaysInMonth(year: Int, month: Int): List<String> {
        val cal = Calendar.getInstance()
        cal.set(year, (month - 1).coerceIn(0, 11), 1, 12, 0, 0)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val result = mutableListOf<String>()
        for (d in 1..maxDay) {
            result.add(String.format(Locale.US, "%04d-%02d-%02d", year, month, d))
        }
        return result
    }

    fun previousMonth(year: Int, month: Int): Pair<Int, Int> {
        return if (month <= 1) Pair(year - 1, 12) else Pair(year, month - 1)
    }

    fun nextMonth(year: Int, month: Int): Pair<Int, Int> {
        return if (month >= 12) Pair(year + 1, 1) else Pair(year, month + 1)
    }

    fun formatMinutesToTime(minutesFromMidnight: Int): String {
        val normalized = ((minutesFromMidnight % 1440) + 1440) % 1440
        val hour24 = normalized / 60
        val minute = normalized % 60
        val amPm = if (hour24 < 12) "AM" else "PM"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return String.format(Locale.US, "%02d:%02d %s", hour12, minute, amPm)
    }

    fun formatRs(amount: Double): String {
        return "₹${currencyFormatter.format(amount)}"
    }

    fun formatHours(hours: Double): String {
        return "${hoursFormatter.format(hours)}h"
    }

    fun formatHoursAndMinutes(hours: Double): String {
        val totalMinutes = (hours * 60.0).roundToInt()
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return if (m == 0) "${h}h" else "${h}h ${m}m"
    }

    fun formatLunchDuration(lunchMinutes: Int): String {
        if (lunchMinutes <= 0) return "0m"
        val h = lunchMinutes / 60
        val m = lunchMinutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h (${lunchMinutes}m)"
            else -> "${m}m"
        }
    }
}
