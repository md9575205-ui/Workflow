package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_shifts")
data class WorkShiftEntity(
    @PrimaryKey val dateIso: String, // Format: YYYY-MM-DD
    val startMinutes: Int = 540,     // 09:00 AM (9 * 60)
    val endMinutes: Int = 1080,      // 06:00 PM (18 * 60)
    val lunchMinutes: Int = 60,      // 60 minutes (1 hour lunch)
    val extraOvertimeHours: Double = 0.0, // Additional manual OT hours if any
    val hourlyRateOverride: Double? = null, // Optional rate override for that specific day
    val notes: String = "",
    val isSaturdaySalaryPaid: Boolean = false // Used on Saturday date or week tracking
)

enum class AppThemeMode(val key: String, val label: String) {
    LIGHT("LIGHT", "Light"),
    DARK("DARK", "Dark"),
    SYSTEM("SYSTEM", "System");

    companion object {
        fun fromKey(key: String?): AppThemeMode {
            return entries.find { it.key.equals(key, ignoreCase = true) } ?: SYSTEM
        }
    }
}

@Entity(tableName = "pay_settings")
data class PaySettingsEntity(
    @PrimaryKey val id: Int = 1,
    val hourlyRateRs: Double = 40.0,           // Default: 40 Rs / hour as requested
    val overtimeRateRs: Double = 40.0,         // Overtime rate in Rs / hour (can be 40, 60 (1.5x), 80 (2x), or custom)
    val standardDailyHours: Double = 8.0,      // Regular hours per day before auto-overtime
    val autoOvertimeBeyondStandard: Boolean = true,
    val deductLunchTime: Boolean = true,       // Deduct lunch time from total shift hours
    val defaultStartMinutes: Int = 540,        // 09:00 AM
    val defaultEndMinutes: Int = 1080,         // 06:00 PM
    val defaultLunchMinutes: Int = 60,         // 60 mins lunch
    val themeMode: String = "SYSTEM"           // "LIGHT", "DARK", or "SYSTEM"
)

data class DayCalculation(
    val isLogged: Boolean = false,
    val grossShiftHours: Double = 0.0,
    val lunchMinutes: Int = 0,
    val lunchHours: Double = 0.0,
    val netShiftHours: Double = 0.0,
    val regularHours: Double = 0.0,
    val autoOvertimeHours: Double = 0.0,
    val extraOvertimeHours: Double = 0.0,
    val totalOvertimeHours: Double = 0.0,
    val totalWorkingHours: Double = 0.0,
    val effectiveHourlyRateRs: Double = 40.0,
    val effectiveOvertimeRateRs: Double = 40.0,
    val regularPayRs: Double = 0.0,
    val overtimePayRs: Double = 0.0,
    val totalDailyIncomeRs: Double = 0.0
)

data class DaySlot(
    val dateIso: String,
    val dayOfWeekIndex: Int, // 1 = Monday .. 6 = Saturday, 7 = Sunday
    val dayNameFull: String,
    val dayNameShort: String,
    val dayMonthFormatted: String, // e.g. "03 Oct"
    val isToday: Boolean,
    val isSaturday: Boolean,
    val isSunday: Boolean,
    val shift: WorkShiftEntity?,
    val calc: DayCalculation
)

data class WeekSummary(
    val mondayDateIso: String,
    val saturdayDateIso: String,
    val sundayDateIso: String,
    val weekRangeLabel: String, // e.g., "Mon, 28 Sep – Sat, 03 Oct 2026"
    val saturdayDateLabel: String, // e.g., "Sat, 03 Oct 2026"
    val daySlots: List<DaySlot>, // 7 days (Mon..Sun), with Mon..Sat primary
    val monToSatDaysWorked: Int,
    val monToSatRegularHours: Double,
    val monToSatOvertimeHours: Double,
    val monToSatTotalHours: Double,
    val monToSatLunchMinutes: Int,
    val monToSatRegularPayRs: Double,
    val monToSatOvertimePayRs: Double,
    val monToSatTotalIncomeRs: Double, // Weekly Salary calculated on Saturday
    val wholeWeekTotalHours: Double,   // Mon–Sun total hours
    val wholeWeekTotalIncomeRs: Double,// Mon–Sun total income
    val wholeWeekOvertimeHours: Double,
    val wholeWeekOvertimePayRs: Double,
    val wholeWeekLunchMinutes: Int,
    val isSaturdaySalaryPaid: Boolean
)

data class MonthWeekPayout(
    val weekLabel: String,
    val saturdayLabel: String,
    val saturdayDateIso: String,
    val daysWorkedInWeek: Int,
    val totalHours: Double,
    val overtimeHours: Double,
    val lunchMinutes: Int,
    val totalSalaryRs: Double,
    val isPaid: Boolean
)

data class MonthSummary(
    val year: Int,
    val month: Int, // 1..12
    val monthTitle: String, // e.g. "September 2026"
    val totalDaysWorked: Int,
    val totalRegularHours: Double,
    val totalOvertimeHours: Double,
    val totalWorkingHours: Double,
    val totalLunchMinutes: Int,
    val totalRegularPayRs: Double,
    val totalOvertimePayRs: Double,
    val totalMonthlyIncomeRs: Double,
    val loggedDaySlots: List<DaySlot>,
    val saturdayPayouts: List<MonthWeekPayout>
)
