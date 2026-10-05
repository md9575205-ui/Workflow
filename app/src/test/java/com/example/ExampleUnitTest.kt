package com.example

import com.example.data.PaySettingsEntity
import com.example.data.WageCalculator
import com.example.data.WorkShiftEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun calculateDay_standard8HoursWith1HourLunchAt40Rs() {
        val settings = PaySettingsEntity(
            hourlyRateRs = 40.0,
            overtimeRateRs = 60.0,
            standardDailyHours = 8.0,
            deductLunchTime = true,
            autoOvertimeBeyondStandard = true
        )
        // 09:00 AM (540) to 06:00 PM (1080) = 9 gross hours - 60m lunch = 8.0 net working hours
        val shift = WorkShiftEntity(
            dateIso = "2026-09-28",
            startMinutes = 540,
            endMinutes = 1080,
            lunchMinutes = 60,
            extraOvertimeHours = 2.0
        )
        val result = WageCalculator.calculateDay(shift, settings)
        assertEquals(8.0, result.regularHours, 0.001)
        assertEquals(2.0, result.totalOvertimeHours, 0.001)
        assertEquals(10.0, result.totalWorkingHours, 0.001)
        assertEquals(320.0, result.regularPayRs, 0.001)
        assertEquals(120.0, result.overtimePayRs, 0.001)
        assertEquals(440.0, result.totalDailyIncomeRs, 0.001)
    }

    @Test
    fun calculateWeek_mondayToSaturdaySalaryCalculation() {
        val settings = PaySettingsEntity(hourlyRateRs = 40.0, overtimeRateRs = 40.0)
        val shifts = (0..5).associate { offset ->
            val date = "2026-09-${28 + offset}" // Wait: let's use DateHelper.addDays
            val iso = com.example.data.DateHelper.addDays("2026-09-28", offset)
            iso to WorkShiftEntity(
                dateIso = iso,
                startMinutes = 540,
                endMinutes = 1080,
                lunchMinutes = 60
            )
        }
        val week = WageCalculator.calculateWeek(
            mondayDateIso = "2026-09-28",
            shiftsByDate = shifts,
            settings = settings,
            todayIso = "2026-09-29"
        )
        assertEquals(6, week.monToSatDaysWorked)
        assertEquals(48.0, week.monToSatTotalHours, 0.001)
        assertEquals(1920.0, week.monToSatTotalIncomeRs, 0.001) // 48 hours * 40 Rs = 1920 Rs
        assertTrue(week.saturdayDateIso == "2026-10-03")
    }

    @Test
    fun calculateWeek_advancesAndDeductionsNetSaturdayPayout() {
        val settings = PaySettingsEntity(hourlyRateRs = 40.0)
        val shifts = (0..5).associate { offset ->
            val iso = com.example.data.DateHelper.addDays("2026-09-28", offset)
            iso to WorkShiftEntity(dateIso = iso, startMinutes = 540, endMinutes = 1080, lunchMinutes = 60)
        }
        val advances = listOf(
            com.example.data.AdvanceDeductionEntity(id = 1, dateIso = "2026-09-30", amountRs = 200.0, isDeduction = false, note = "Cash advance"),
            com.example.data.AdvanceDeductionEntity(id = 2, dateIso = "2026-10-01", amountRs = 50.0, isDeduction = true, note = "Canteen deduction")
        )
        val week = WageCalculator.calculateWeek(
            mondayDateIso = "2026-09-28",
            shiftsByDate = shifts,
            settings = settings,
            todayIso = "2026-09-29",
            advancesDeductions = advances
        )
        assertEquals(1920.0, week.monToSatTotalIncomeRs, 0.001)
        assertEquals(250.0, week.totalAdvancesDeductionsRs, 0.001)
        assertEquals(1670.0, week.netSaturdaySalaryRs, 0.001) // 1920 - 250 = 1670 Rs

        val paySlip = WageCalculator.generateSaturdayPaySlipText(week, settings)
        assertTrue(paySlip.contains("WEEKLY SATURDAY SALARY PAYSLIP"))
        assertTrue(paySlip.contains("NET CASH DUE (SATURDAY): ₹1,670"))
    }

    @Test
    fun generateMonthCsv_producesValidCsvHeaderAndRows() {
        val settings = PaySettingsEntity(hourlyRateRs = 40.0)
        val shift = WorkShiftEntity(dateIso = "2026-09-28", startMinutes = 540, endMinutes = 1080, lunchMinutes = 60)
        val monthSummary = WageCalculator.calculateMonth(
            year = 2026,
            month = 9,
            allShifts = listOf(shift),
            settings = settings,
            todayIso = "2026-09-29"
        )
        val csv = WageCalculator.generateMonthCsv(monthSummary, settings)
        assertTrue(csv.contains("Date,Day,Start Time,End Time,Lunch Minutes,Regular Hours,Overtime Hours,Total Working Hours,Hourly Rate (Rs),Daily Income (Rs),Notes"))
        assertTrue(csv.contains("2026-09-28,MON,09:00 AM,06:00 PM,60,8,0,8,40.0,320.0,\"\""))
    }
}
