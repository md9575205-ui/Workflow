package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DateHelper
import com.example.data.DaySlot
import com.example.data.MonthSummary
import com.example.data.PaySettingsEntity
import com.example.data.WeekSummary
import com.example.ui.components.LiveShiftPunchClock
import com.example.ui.components.WeeklyAdvancesCard
import com.example.ui.components.WeeklyEarningsBarChart

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeeklyTrackerScreen(
    weekSummary: WeekSummary,
    monthSummary: MonthSummary,
    settings: PaySettingsEntity,
    showSunday: Boolean,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onJumpToday: () -> Unit,
    onQuickLogDay: (String) -> Unit,
    onEditDay: (String) -> Unit,
    onQuickFillMonToSat: () -> Unit,
    onClearWeek: () -> Unit,
    onToggleSaturdayPaid: (String) -> Unit,
    onToggleShowSunday: () -> Unit,
    onNavigateToMonthlyReport: () -> Unit,
    onNavigateToRateSettings: () -> Unit,
    onSaveClockShift: (startMinutes: Int, endMinutes: Int, lunchMinutes: Int) -> Unit = { _, _, _ -> },
    onAddAdvanceClick: () -> Unit = {},
    onDeleteAdvance: (Long) -> Unit = {},
    onOpenSaturdayPaySlip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val visibleDaySlots = if (showSunday) {
        weekSummary.daySlots
    } else {
        weekSummary.daySlots.filter { it.dayOfWeekIndex in 1..6 }
    }

    val defaultStandardPay = settings.standardDailyHours * settings.hourlyRateRs

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("weekly_tracker_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Banner Card: Saturday Salary & Monthly Income Overview
        item {
            HeroPaydayCard(
                weekSummary = weekSummary,
                monthSummary = monthSummary,
                settings = settings,
                onNavigateToMonthlyReport = onNavigateToMonthlyReport,
                onNavigateToRateSettings = onNavigateToRateSettings
            )
        }

        // 2. Live Shift Punch Clock
        item {
            LiveShiftPunchClock(
                hourlyRateRs = settings.hourlyRateRs,
                onSaveFinishedShift = onSaveClockShift
            )
        }

        // 3. Week Selector Bar
        item {
            WeekSelectorCard(
                weekRangeLabel = weekSummary.weekRangeLabel,
                saturdayDateLabel = weekSummary.saturdayDateLabel,
                onPrevWeek = onPrevWeek,
                onNextWeek = onNextWeek,
                onJumpToday = onJumpToday
            )
        }

        // 4. Mon-Sat Weekly Metrics & Quick Actions Card
        item {
            WeeklyMetricsGridCard(
                weekSummary = weekSummary,
                onQuickFillMonToSat = onQuickFillMonToSat,
                onClearWeek = onClearWeek,
                onToggleSaturdayPaid = { onToggleSaturdayPaid(weekSummary.saturdayDateIso) },
                onOpenSaturdayPaySlip = onOpenSaturdayPaySlip
            )
        }

        // 5. Weekly Daily Earnings Bar Chart
        item {
            WeeklyEarningsBarChart(
                weekSummary = weekSummary,
                showSunday = showSunday,
                onDayClick = onEditDay
            )
        }

        // 6. Weekly Advances & Deductions Card (Net Saturday Take-Home Pay)
        item {
            WeeklyAdvancesCard(
                weekSummary = weekSummary,
                onAddAdvanceClick = onAddAdvanceClick,
                onDeleteAdvance = onDeleteAdvance
            )
        }

        // 4. Section Header for Monday to Saturday Daily Log
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monday – Saturday Daily Log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Daily working hours, lunch time & daily income (₹${settings.hourlyRateRs.toInt()}/hr)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilterChip(
                    selected = showSunday,
                    onClick = onToggleShowSunday,
                    label = { Text(if (showSunday) "Sun Shown" else "+ Sun OT") },
                    modifier = Modifier.testTag("toggle_sunday_button")
                )
            }
        }

        // 5. Day-by-Day Cards (Monday through Saturday + optional Sunday)
        items(visibleDaySlots, key = { it.dateIso }) { slot ->
            DayShiftCard(
                slot = slot,
                defaultStandardHours = settings.standardDailyHours,
                defaultStandardPayRs = defaultStandardPay,
                onQuickLog = { onQuickLogDay(slot.dateIso) },
                onEdit = { onEditDay(slot.dateIso) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroPaydayCard(
    weekSummary: WeekSummary,
    monthSummary: MonthSummary,
    settings: PaySettingsEntity,
    onNavigateToMonthlyReport: () -> Unit,
    onNavigateToRateSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_banner),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xD906291E),
                                Color(0xF2083829)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Top Row: App Title + Rate Pill + Monthly Income Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable(onClick = onNavigateToRateSettings)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("hero_rate_badge")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CurrencyRupee,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Rate: ${DateHelper.formatRs(settings.hourlyRateRs)}/hr",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFFBBF24).copy(alpha = 0.22f))
                            .border(1.dp, Color(0xFFFBBF24).copy(alpha = 0.5f), RoundedCornerShape(50))
                            .clickable(onClick = onNavigateToMonthlyReport)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("hero_month_income_badge")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFFFDE68A),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${monthSummary.monthTitle.substringBefore(" ")}: ${DateHelper.formatRs(monthSummary.totalMonthlyIncomeRs)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFEF3C7)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Main Saturday Salary & Whole Week Hours
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SATURDAY SALARY (MON–SAT)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA7F3D0),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = DateHelper.formatRs(weekSummary.monToSatTotalIncomeRs),
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("hero_saturday_salary_value")
                        )
                        if (weekSummary.totalAdvancesDeductionsRs > 0) {
                            Text(
                                text = "Net Due: ${DateHelper.formatRs(weekSummary.netSaturdaySalaryRs)} (-${DateHelper.formatRs(weekSummary.totalAdvancesDeductionsRs)} adv)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE68A)
                            )
                        } else {
                            Text(
                                text = "Calculated on ${weekSummary.saturdayDateLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "WHOLE WEEK HOURS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFDE68A)
                        )
                        Text(
                            text = DateHelper.formatHours(weekSummary.wholeWeekTotalHours),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("hero_whole_week_hours_value")
                        )
                        Text(
                            text = "${weekSummary.monToSatDaysWorked}/6 Mon–Sat days",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekSelectorCard(
    weekRangeLabel: String,
    saturdayDateLabel: String,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onJumpToday: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onPrevWeek,
                modifier = Modifier.testTag("prev_week_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous week"
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = weekRangeLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Payday: $saturdayDateLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• Today",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onJumpToday)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("today_week_button")
                    )
                }
            }

            IconButton(
                onClick = onNextWeek,
                modifier = Modifier.testTag("next_week_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next week"
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeeklyMetricsGridCard(
    weekSummary: WeekSummary,
    onQuickFillMonToSat: () -> Unit,
    onClearWeek: () -> Unit,
    onToggleSaturdayPaid: () -> Unit,
    onOpenSaturdayPaySlip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    icon = Icons.Default.Payments,
                    label = "Mon–Sat Salary",
                    value = DateHelper.formatRs(weekSummary.monToSatTotalIncomeRs),
                    subValue = "Reg: ${DateHelper.formatRs(weekSummary.monToSatRegularPayRs)}",
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("weekly_mon_sat_income")
                )
                MetricTile(
                    icon = Icons.Default.Schedule,
                    label = "Whole Week Hours",
                    value = DateHelper.formatHours(weekSummary.wholeWeekTotalHours),
                    subValue = "Mon–Sat: ${DateHelper.formatHours(weekSummary.monToSatTotalHours)}",
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("weekly_total_hours")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    icon = Icons.Default.Restaurant,
                    label = "Week Lunch Time",
                    value = DateHelper.formatLunchDuration(weekSummary.wholeWeekLunchMinutes),
                    subValue = "${weekSummary.monToSatDaysWorked} shifts logged",
                    accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("weekly_lunch_time")
                )
                MetricTile(
                    icon = Icons.Default.MoreTime,
                    label = "Overtime Pay",
                    value = DateHelper.formatRs(weekSummary.wholeWeekOvertimePayRs),
                    subValue = "+${DateHelper.formatHours(weekSummary.wholeWeekOvertimeHours)} OT worked",
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("weekly_overtime_pay")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onQuickFillMonToSat,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_fill_week_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Quick Fill Mon–Sat")
                }

                OutlinedButton(
                    onClick = onOpenSaturdayPaySlip,
                    modifier = Modifier.testTag("open_saturday_payslip_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pay Slip")
                }

                OutlinedButton(
                    onClick = onToggleSaturdayPaid,
                    colors = if (weekSummary.isSaturdaySalaryPaid) {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    },
                    modifier = Modifier.testTag("toggle_saturday_paid_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (weekSummary.isSaturdaySalaryPaid) "Sat Salary: PAID" else "Mark Sat Paid")
                }

                if (weekSummary.wholeWeekTotalHours > 0) {
                    IconButton(
                        onClick = onClearWeek,
                        modifier = Modifier.testTag("clear_week_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear week shifts",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subValue,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DayShiftCard(
    slot: DaySlot,
    defaultStandardHours: Double,
    defaultStandardPayRs: Double,
    onQuickLog: () -> Unit,
    onEdit: () -> Unit
) {
    val isLogged = slot.calc.isLogged
    val borderMod = when {
        slot.isSaturday -> Modifier.border(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.medium
        )
        slot.isToday -> Modifier.border(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.medium
        )
        else -> Modifier
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderMod)
            .clickable(onClick = onEdit)
            .testTag("day_card_${slot.dayNameShort.lowercase()}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (slot.isSaturday) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.28f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLogged) 2.dp else 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Day Badge + Date + Status Tags
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    slot.isSaturday -> MaterialTheme.colorScheme.secondary
                                    isLogged -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                    ) {
                        Text(
                            text = slot.dayNameShort,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                slot.isSaturday -> MaterialTheme.colorScheme.onSecondary
                                isLogged -> MaterialTheme.colorScheme.onPrimary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = slot.dayNameFull,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = slot.dayMonthFormatted,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (slot.isToday) {
                                Spacer(modifier = Modifier.width(6.dp))
                                BadgePill(
                                    text = "TODAY",
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            if (slot.isSaturday) {
                                Spacer(modifier = Modifier.width(6.dp))
                                BadgePill(
                                    text = "SALARY DAY",
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        if (isLogged && slot.shift != null) {
                            val timeRange = "${DateHelper.formatMinutesToTime(slot.shift.startMinutes)} – ${DateHelper.formatMinutesToTime(slot.shift.endMinutes)}"
                            Text(
                                text = timeRange,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "No shift logged yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Right: Daily Income or Quick Log Actions
                if (isLogged) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = DateHelper.formatRs(slot.calc.totalDailyIncomeRs),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("day_income_${slot.dayNameShort.lowercase()}")
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit ${slot.dayNameFull} shift",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onQuickLog,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("quick_log_${slot.dayNameShort.lowercase()}")
                        ) {
                            Text(
                                text = "+ ${DateHelper.formatHours(defaultStandardHours)} (${DateHelper.formatRs(defaultStandardPayRs)})",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("edit_day_${slot.dayNameShort.lowercase()}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Custom shift for ${slot.dayNameFull}"
                            )
                        }
                    }
                }
            }

            // Bottom Detail Strip when Logged: Daily Working Hours, Lunch Time, Overtime Pay
            if (isLogged) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Work: ${DateHelper.formatHours(slot.calc.totalWorkingHours)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Lunch: ${slot.calc.lunchMinutes}m",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (slot.calc.totalOvertimeHours > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MoreTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "OT: +${DateHelper.formatHours(slot.calc.totalOvertimeHours)} (${DateHelper.formatRs(slot.calc.overtimePayRs)})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    } else {
                        Text(
                            text = "Regular: ${DateHelper.formatRs(slot.calc.regularPayRs)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgePill(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(containerColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}
