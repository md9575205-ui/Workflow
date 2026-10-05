package com.example.data

import kotlin.math.max
import kotlin.math.min

object WageCalculator {

    fun calculateDay(
        shift: WorkShiftEntity?,
        settings: PaySettingsEntity
    ): DayCalculation {
        if (shift == null) {
            return DayCalculation(
                isLogged = false,
                effectiveHourlyRateRs = settings.hourlyRateRs,
                effectiveOvertimeRateRs = settings.overtimeRateRs
            )
        }

        val spanMinutes = if (shift.endMinutes >= shift.startMinutes) {
            shift.endMinutes - shift.startMinutes
        } else {
            // Overnight shift support
            (1440 - shift.startMinutes) + shift.endMinutes
        }

        val grossShiftHours = max(0.0, spanMinutes / 60.0)
        val lunchHours = max(0.0, shift.lunchMinutes / 60.0)
        val deductedLunchHours = if (settings.deductLunchTime) lunchHours else 0.0
        val netShiftHours = max(0.0, grossShiftHours - deductedLunchHours)

        val standardCap = max(0.0, settings.standardDailyHours)
        val regularHours = if (settings.autoOvertimeBeyondStandard) {
            min(netShiftHours, standardCap)
        } else {
            netShiftHours
        }

        val autoOvertimeHours = if (settings.autoOvertimeBeyondStandard) {
            max(0.0, netShiftHours - standardCap)
        } else {
            0.0
        }

        val extraOvertime = max(0.0, shift.extraOvertimeHours)
        val totalOvertimeHours = autoOvertimeHours + extraOvertime
        val totalWorkingHours = regularHours + totalOvertimeHours

        val baseRate = shift.hourlyRateOverride ?: settings.hourlyRateRs
        val otRate = if (shift.hourlyRateOverride != null && settings.hourlyRateRs > 0) {
            val ratio = settings.overtimeRateRs / settings.hourlyRateRs
            shift.hourlyRateOverride * ratio
        } else {
            settings.overtimeRateRs
        }

        val regularPayRs = regularHours * baseRate
        val overtimePayRs = totalOvertimeHours * otRate
        val totalDailyIncomeRs = regularPayRs + overtimePayRs

        return DayCalculation(
            isLogged = true,
            grossShiftHours = grossShiftHours,
            lunchMinutes = shift.lunchMinutes,
            lunchHours = lunchHours,
            netShiftHours = netShiftHours,
            regularHours = regularHours,
            autoOvertimeHours = autoOvertimeHours,
            extraOvertimeHours = extraOvertime,
            totalOvertimeHours = totalOvertimeHours,
            totalWorkingHours = totalWorkingHours,
            effectiveHourlyRateRs = baseRate,
            effectiveOvertimeRateRs = otRate,
            regularPayRs = regularPayRs,
            overtimePayRs = overtimePayRs,
            totalDailyIncomeRs = totalDailyIncomeRs
        )
    }

    fun calculateWeek(
        mondayDateIso: String,
        shiftsByDate: Map<String, WorkShiftEntity>,
        settings: PaySettingsEntity,
        todayIso: String,
        advancesDeductions: List<AdvanceDeductionEntity> = emptyList()
    ): WeekSummary {
        val weekDates = (0..6).map { DateHelper.addDays(mondayDateIso, it) }
        val daySlots = (0..6).map { offset ->
            val dateIso = weekDates[offset]
            val dayIndex = offset + 1 // 1 = Mon .. 6 = Sat, 7 = Sun
            val shift = shiftsByDate[dateIso]
            val calc = calculateDay(shift, settings)
            DaySlot(
                dateIso = dateIso,
                dayOfWeekIndex = dayIndex,
                dayNameFull = DateHelper.getDayNameFull(dayIndex),
                dayNameShort = DateHelper.getDayNameShort(dayIndex),
                dayMonthFormatted = DateHelper.formatDayMonth(dateIso),
                isToday = dateIso == todayIso,
                isSaturday = dayIndex == 6,
                isSunday = dayIndex == 7,
                shift = shift,
                calc = calc
            )
        }

        val monToSatSlots = daySlots.filter { it.dayOfWeekIndex in 1..6 }
        val monToSatLogged = monToSatSlots.filter { it.calc.isLogged }

        val monToSatDaysWorked = monToSatLogged.size
        val monToSatRegularHours = monToSatLogged.sumOf { it.calc.regularHours }
        val monToSatOvertimeHours = monToSatLogged.sumOf { it.calc.totalOvertimeHours }
        val monToSatTotalHours = monToSatLogged.sumOf { it.calc.totalWorkingHours }
        val monToSatLunchMinutes = monToSatLogged.sumOf { it.calc.lunchMinutes }
        val monToSatRegularPayRs = monToSatLogged.sumOf { it.calc.regularPayRs }
        val monToSatOvertimePayRs = monToSatLogged.sumOf { it.calc.overtimePayRs }
        val monToSatTotalIncomeRs = monToSatLogged.sumOf { it.calc.totalDailyIncomeRs }

        val wholeWeekLogged = daySlots.filter { it.calc.isLogged }
        val wholeWeekTotalHours = wholeWeekLogged.sumOf { it.calc.totalWorkingHours }
        val wholeWeekTotalIncomeRs = wholeWeekLogged.sumOf { it.calc.totalDailyIncomeRs }
        val wholeWeekOvertimeHours = wholeWeekLogged.sumOf { it.calc.totalOvertimeHours }
        val wholeWeekOvertimePayRs = wholeWeekLogged.sumOf { it.calc.overtimePayRs }
        val wholeWeekLunchMinutes = wholeWeekLogged.sumOf { it.calc.lunchMinutes }

        val saturdayIso = DateHelper.addDays(mondayDateIso, 5)
        val sundayIso = DateHelper.addDays(mondayDateIso, 6)
        val satShift = shiftsByDate[saturdayIso]
        val isPaid = satShift?.isSaturdaySalaryPaid == true ||
            monToSatSlots.any { it.shift?.isSaturdaySalaryPaid == true }

        val weekAdvances = advancesDeductions.filter { it.dateIso in weekDates }
        val totalAdvancesRs = weekAdvances.sumOf { it.amountRs }

        return WeekSummary(
            mondayDateIso = mondayDateIso,
            saturdayDateIso = saturdayIso,
            sundayDateIso = sundayIso,
            weekRangeLabel = DateHelper.formatWeekRangeLabel(mondayDateIso, saturdayIso),
            saturdayDateLabel = DateHelper.formatFullDateWithDay(saturdayIso),
            daySlots = daySlots,
            monToSatDaysWorked = monToSatDaysWorked,
            monToSatRegularHours = monToSatRegularHours,
            monToSatOvertimeHours = monToSatOvertimeHours,
            monToSatTotalHours = monToSatTotalHours,
            monToSatLunchMinutes = monToSatLunchMinutes,
            monToSatRegularPayRs = monToSatRegularPayRs,
            monToSatOvertimePayRs = monToSatOvertimePayRs,
            monToSatTotalIncomeRs = monToSatTotalIncomeRs,
            wholeWeekTotalHours = wholeWeekTotalHours,
            wholeWeekTotalIncomeRs = wholeWeekTotalIncomeRs,
            wholeWeekOvertimeHours = wholeWeekOvertimeHours,
            wholeWeekOvertimePayRs = wholeWeekOvertimePayRs,
            wholeWeekLunchMinutes = wholeWeekLunchMinutes,
            isSaturdaySalaryPaid = isPaid,
            advancesDeductions = weekAdvances,
            totalAdvancesDeductionsRs = totalAdvancesRs,
            netSaturdaySalaryRs = monToSatTotalIncomeRs - totalAdvancesRs
        )
    }

    fun calculateMonth(
        year: Int,
        month: Int,
        allShifts: List<WorkShiftEntity>,
        settings: PaySettingsEntity,
        todayIso: String,
        allAdvancesDeductions: List<AdvanceDeductionEntity> = emptyList()
    ): MonthSummary {
        val shiftsByDate = allShifts.associateBy { it.dateIso }
        val monthDates = DateHelper.getDaysInMonth(year, month)

        val monthSlots = monthDates.map { dateIso ->
            val dayIndex = DateHelper.getDayOfWeekIndex(dateIso)
            val shift = shiftsByDate[dateIso]
            val calc = calculateDay(shift, settings)
            DaySlot(
                dateIso = dateIso,
                dayOfWeekIndex = dayIndex,
                dayNameFull = DateHelper.getDayNameFull(dayIndex),
                dayNameShort = DateHelper.getDayNameShort(dayIndex),
                dayMonthFormatted = DateHelper.formatDayMonth(dateIso),
                isToday = dateIso == todayIso,
                isSaturday = dayIndex == 6,
                isSunday = dayIndex == 7,
                shift = shift,
                calc = calc
            )
        }

        val loggedSlots = monthSlots.filter { it.calc.isLogged }

        val totalDaysWorked = loggedSlots.size
        val totalRegularHours = loggedSlots.sumOf { it.calc.regularHours }
        val totalOvertimeHours = loggedSlots.sumOf { it.calc.totalOvertimeHours }
        val totalWorkingHours = loggedSlots.sumOf { it.calc.totalWorkingHours }
        val totalLunchMinutes = loggedSlots.sumOf { it.calc.lunchMinutes }
        val totalRegularPayRs = loggedSlots.sumOf { it.calc.regularPayRs }
        val totalOvertimePayRs = loggedSlots.sumOf { it.calc.overtimePayRs }
        val totalMonthlyIncomeRs = loggedSlots.sumOf { it.calc.totalDailyIncomeRs }

        val monthAdvances = allAdvancesDeductions.filter { it.dateIso in monthDates }
        val totalAdvancesRs = monthAdvances.sumOf { it.amountRs }

        // Group month's days by their Monday week start so each week's Mon-Sat Saturday salary is shown
        val distinctMondays = monthDates.map { DateHelper.getMondayOfWeek(it) }.distinct()
        val saturdayPayouts = distinctMondays.mapIndexed { idx, mondayIso ->
            val weekSummary = calculateWeek(mondayIso, shiftsByDate, settings, todayIso, allAdvancesDeductions)
            MonthWeekPayout(
                weekLabel = "Week ${idx + 1} (${DateHelper.formatDayMonth(mondayIso)} – ${DateHelper.formatDayMonth(weekSummary.saturdayDateIso)})",
                saturdayLabel = "Payday: Sat, ${DateHelper.formatDayMonth(weekSummary.saturdayDateIso)}",
                saturdayDateIso = weekSummary.saturdayDateIso,
                daysWorkedInWeek = weekSummary.monToSatDaysWorked,
                totalHours = weekSummary.monToSatTotalHours,
                overtimeHours = weekSummary.monToSatOvertimeHours,
                lunchMinutes = weekSummary.monToSatLunchMinutes,
                totalSalaryRs = weekSummary.monToSatTotalIncomeRs,
                isPaid = weekSummary.isSaturdaySalaryPaid,
                advancesDeductionsRs = weekSummary.totalAdvancesDeductionsRs,
                netSalaryRs = weekSummary.netSaturdaySalaryRs
            )
        }

        return MonthSummary(
            year = year,
            month = month,
            monthTitle = DateHelper.getMonthTitle(year, month),
            totalDaysWorked = totalDaysWorked,
            totalRegularHours = totalRegularHours,
            totalOvertimeHours = totalOvertimeHours,
            totalWorkingHours = totalWorkingHours,
            totalLunchMinutes = totalLunchMinutes,
            totalRegularPayRs = totalRegularPayRs,
            totalOvertimePayRs = totalOvertimePayRs,
            totalMonthlyIncomeRs = totalMonthlyIncomeRs,
            loggedDaySlots = loggedSlots,
            saturdayPayouts = saturdayPayouts,
            totalAdvancesDeductionsRs = totalAdvancesRs,
            netMonthlyIncomeRs = totalMonthlyIncomeRs - totalAdvancesRs
        )
    }

    fun generateMonthlyReportText(
        monthSummary: MonthSummary,
        settings: PaySettingsEntity
    ): String {
        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("   WAGEFLOW - MONTHLY SALARY REPORT")
        sb.appendLine("   Month: ${monthSummary.monthTitle}")
        sb.appendLine("==========================================")
        sb.appendLine("Base Hourly Rate : ${DateHelper.formatRs(settings.hourlyRateRs)} / hr")
        sb.appendLine("Overtime Rate    : ${DateHelper.formatRs(settings.overtimeRateRs)} / hr")
        sb.appendLine("Salary Cycle     : Calculated Every Saturday (Mon–Sat)")
        sb.appendLine("------------------------------------------")
        sb.appendLine("MONTHLY SUMMARY")
        sb.appendLine("Days Worked      : ${monthSummary.totalDaysWorked} days")
        sb.appendLine("Regular Hours    : ${DateHelper.formatHours(monthSummary.totalRegularHours)}")
        sb.appendLine("Overtime Hours   : ${DateHelper.formatHours(monthSummary.totalOvertimeHours)}")
        sb.appendLine("Total Work Hours : ${DateHelper.formatHours(monthSummary.totalWorkingHours)}")
        sb.appendLine("Total Lunch Time : ${DateHelper.formatLunchDuration(monthSummary.totalLunchMinutes)}")
        sb.appendLine("Gross Month Pay  : ${DateHelper.formatRs(monthSummary.totalMonthlyIncomeRs)}")
        if (monthSummary.totalAdvancesDeductionsRs > 0) {
            sb.appendLine("Advances / Deds  : -${DateHelper.formatRs(monthSummary.totalAdvancesDeductionsRs)}")
            sb.appendLine("NET TAKE-HOME    : ${DateHelper.formatRs(monthSummary.netMonthlyIncomeRs)}")
        }
        sb.appendLine("------------------------------------------")
        sb.appendLine("WEEKLY SATURDAY SALARY PAYOUTS (MON–SAT)")
        monthSummary.saturdayPayouts.forEach { wp ->
            val status = if (wp.isPaid) "[PAID]" else "[DUE SAT]"
            val advStr = if (wp.advancesDeductionsRs > 0) " (Net: ${DateHelper.formatRs(wp.netSalaryRs)})" else ""
            sb.appendLine(
                "• ${wp.weekLabel}: ${DateHelper.formatHours(wp.totalHours)} " +
                    "(OT: ${DateHelper.formatHours(wp.overtimeHours)}) -> Gross: ${DateHelper.formatRs(wp.totalSalaryRs)}$advStr $status"
            )
        }
        sb.appendLine("------------------------------------------")
        sb.appendLine("DAILY WORKING HOURS & INCOME LOG")
        if (monthSummary.loggedDaySlots.isEmpty()) {
            sb.appendLine("No shifts logged for ${monthSummary.monthTitle}.")
        } else {
            monthSummary.loggedDaySlots.forEach { slot ->
                val shift = slot.shift ?: return@forEach
                val c = slot.calc
                val timeSpan = "${DateHelper.formatMinutesToTime(shift.startMinutes)} - ${DateHelper.formatMinutesToTime(shift.endMinutes)}"
                val otBadge = if (c.totalOvertimeHours > 0) " (OT: +${DateHelper.formatHours(c.totalOvertimeHours)})" else ""
                sb.appendLine(
                    "${slot.dateIso} (${slot.dayNameShort}) | $timeSpan | " +
                        "Lunch: ${c.lunchMinutes}m | Work: ${DateHelper.formatHours(c.totalWorkingHours)}$otBadge | " +
                        "Pay: ${DateHelper.formatRs(c.totalDailyIncomeRs)}"
                )
            }
        }
        sb.appendLine("==========================================")
        return sb.toString()
    }

    fun generateMonthCsv(
        monthSummary: MonthSummary,
        settings: PaySettingsEntity
    ): String {
        val sb = StringBuilder()
        sb.appendLine("Date,Day,Start Time,End Time,Lunch Minutes,Regular Hours,Overtime Hours,Total Working Hours,Hourly Rate (Rs),Daily Income (Rs),Notes")
        monthSummary.loggedDaySlots.forEach { slot ->
            val shift = slot.shift ?: return@forEach
            val c = slot.calc
            val start = DateHelper.formatMinutesToTime(shift.startMinutes)
            val end = DateHelper.formatMinutesToTime(shift.endMinutes)
            val safeNote = "\"${shift.notes.replace("\"", "\"\"")}\""
            sb.appendLine(
                "${slot.dateIso},${slot.dayNameShort},$start,$end,${c.lunchMinutes}," +
                    "${DateHelper.formatHours(c.regularHours).removeSuffix("h")}," +
                    "${DateHelper.formatHours(c.totalOvertimeHours).removeSuffix("h")}," +
                    "${DateHelper.formatHours(c.totalWorkingHours).removeSuffix("h")}," +
                    "${c.effectiveHourlyRateRs}," +
                    "${c.totalDailyIncomeRs},$safeNote"
            )
        }
        return sb.toString()
    }

    fun generateSaturdayPaySlipText(
        weekSummary: WeekSummary,
        settings: PaySettingsEntity
    ): String {
        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("      WEEKLY SATURDAY SALARY PAYSLIP      ")
        sb.appendLine("==========================================")
        sb.appendLine("Period        : ${weekSummary.weekRangeLabel}")
        sb.appendLine("Payout Date   : ${weekSummary.saturdayDateLabel}")
        sb.appendLine("Hourly Rate   : ${DateHelper.formatRs(settings.hourlyRateRs)} / hr")
        sb.appendLine("Overtime Rate : ${DateHelper.formatRs(settings.overtimeRateRs)} / hr")
        sb.appendLine("Status        : ${if (weekSummary.isSaturdaySalaryPaid) "PAID [✔]" else "PENDING PAYOUT"}")
        sb.appendLine("------------------------------------------")
        sb.appendLine("HOURS & WORK SUMMARY (MON–SAT)")
        sb.appendLine("Days Worked   : ${weekSummary.monToSatDaysWorked} days")
        sb.appendLine("Regular Hours : ${DateHelper.formatHours(weekSummary.monToSatRegularHours)} (${DateHelper.formatRs(weekSummary.monToSatRegularPayRs)})")
        sb.appendLine("Overtime Hours: ${DateHelper.formatHours(weekSummary.monToSatOvertimeHours)} (${DateHelper.formatRs(weekSummary.monToSatOvertimePayRs)})")
        sb.appendLine("Total Hours   : ${DateHelper.formatHours(weekSummary.monToSatTotalHours)}")
        sb.appendLine("Lunch Time    : ${DateHelper.formatLunchDuration(weekSummary.monToSatLunchMinutes)}")
        sb.appendLine("------------------------------------------")
        sb.appendLine("DAILY BREAKDOWN (MON–SAT)")
        weekSummary.daySlots.filter { it.dayOfWeekIndex in 1..6 }.forEach { slot ->
            val shift = slot.shift
            if (shift == null || !slot.calc.isLogged) {
                sb.appendLine("• ${slot.dayNameShort} (${slot.dayMonthFormatted}): Day Off / Unlogged")
            } else {
                val span = "${DateHelper.formatMinutesToTime(shift.startMinutes)}–${DateHelper.formatMinutesToTime(shift.endMinutes)}"
                val ot = if (slot.calc.totalOvertimeHours > 0) " +${DateHelper.formatHours(slot.calc.totalOvertimeHours)} OT" else ""
                sb.appendLine("• ${slot.dayNameShort} (${slot.dayMonthFormatted}): $span | ${DateHelper.formatHours(slot.calc.totalWorkingHours)}$ot | ${DateHelper.formatRs(slot.calc.totalDailyIncomeRs)}")
            }
        }
        sb.appendLine("------------------------------------------")
        sb.appendLine("SALARY & TAKE-HOME CALCULATION")
        sb.appendLine("Gross Saturday Salary : ${DateHelper.formatRs(weekSummary.monToSatTotalIncomeRs)}")
        if (weekSummary.totalAdvancesDeductionsRs > 0) {
            sb.appendLine("Advances & Deductions : -${DateHelper.formatRs(weekSummary.totalAdvancesDeductionsRs)}")
            weekSummary.advancesDeductions.forEach { item ->
                val type = if (item.isDeduction) "Expense Ded" else "Cash Advance"
                sb.appendLine("  - ${item.dateIso} ($type): -${DateHelper.formatRs(item.amountRs)} [${item.note.ifBlank { "Recorded" }}]")
            }
        }
        sb.appendLine("------------------------------------------")
        sb.appendLine("NET CASH DUE (SATURDAY): ${DateHelper.formatRs(weekSummary.netSaturdaySalaryRs)}")
        sb.appendLine("==========================================")
        sb.appendLine("Generated by WageFlow App")
        return sb.toString()
    }
}

