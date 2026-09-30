package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.DateHelper
import com.example.data.DaySlot
import com.example.data.MonthSummary
import com.example.data.MonthWeekPayout
import com.example.data.PaySettingsEntity
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonthlyReportScreen(
    monthSummary: MonthSummary,
    settings: PaySettingsEntity,
    todayIso: String,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenReportModal: () -> Unit,
    onShareReport: () -> Unit,
    onCopyReport: () -> Unit,
    onSelectOvertimeMultiplier: (Double) -> Unit,
    onEditDay: (String) -> Unit,
    onGoToWeeklyTracker: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("monthly_report_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Month Selector Bar
        item {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onPrevMonth,
                        modifier = Modifier.testTag("prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous month"
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = monthSummary.monthTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next month"
                        )
                    }
                }
            }
        }

        // 2. Total Monthly Income & Report Export Hero Card
        item {
            MonthlyOverviewCard(
                monthSummary = monthSummary,
                settings = settings,
                onOpenReportModal = onOpenReportModal,
                onShareReport = onShareReport,
                onCopyReport = onCopyReport
            )
        }

        // 3. Dedicated Overtime Pay Tracker Card
        item {
            OvertimeTrackerCard(
                monthSummary = monthSummary,
                settings = settings,
                onSelectOvertimeMultiplier = onSelectOvertimeMultiplier,
                onLogTodayOvertime = { onEditDay(todayIso) }
            )
        }

        // 4. Saturday Weekly Salary Payouts in this Month
        item {
            Text(
                text = "Saturday Weekly Salary Payouts (${monthSummary.monthTitle})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(monthSummary.saturdayPayouts, key = { it.saturdayDateIso }) { payout ->
            SaturdayPayoutRowCard(payout = payout)
        }

        // 5. Daily Shifts Logged in Month
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Daily Working Hours & Income Log (${monthSummary.totalDaysWorked} Days)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (monthSummary.loggedDaySlots.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No shifts logged in ${monthSummary.monthTitle}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Log your Monday to Saturday working hours to see your monthly total income and generate salary reports.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onGoToWeeklyTracker,
                            modifier = Modifier.testTag("empty_month_go_weekly_button")
                        ) {
                            Text("Go to Mon–Sat Week Tracker")
                        }
                    }
                }
            }
        } else {
            items(monthSummary.loggedDaySlots, key = { it.dateIso }) { slot ->
                MonthlyLoggedDayRow(
                    slot = slot,
                    onClick = { onEditDay(slot.dateIso) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MonthlyOverviewCard(
    monthSummary: MonthSummary,
    settings: PaySettingsEntity,
    onOpenReportModal: () -> Unit,
    onShareReport: () -> Unit,
    onCopyReport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "TOTAL INCOME IN ${monthSummary.monthTitle.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = DateHelper.formatRs(monthSummary.totalMonthlyIncomeRs),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag("monthly_total_income")
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${DateHelper.formatHours(monthSummary.totalWorkingHours)} Worked",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${monthSummary.totalDaysWorked} Days • Lunch: ${DateHelper.formatLunchDuration(monthSummary.totalLunchMinutes)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Regular vs Overtime Split Progress
            val totalIncome = monthSummary.totalMonthlyIncomeRs
            val regularFraction = if (totalIncome > 0) {
                (monthSummary.totalRegularPayRs / totalIncome).toFloat().coerceIn(0f, 1f)
            } else {
                1f
            }
            LinearProgressIndicator(
                progress = { regularFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Regular (${DateHelper.formatHours(monthSummary.totalRegularHours)}): ${DateHelper.formatRs(monthSummary.totalRegularPayRs)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Overtime (${DateHelper.formatHours(monthSummary.totalOvertimeHours)}): ${DateHelper.formatRs(monthSummary.totalOvertimePayRs)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            // Report Generation & Export Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenReportModal,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("generate_monthly_report_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Monthly Report")
                }

                FilledTonalButton(
                    onClick = onShareReport,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(48.dp)
                        .testTag("share_monthly_report_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share monthly report",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }

                IconButton(
                    onClick = onCopyReport,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .testTag("copy_monthly_report_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy monthly report",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OvertimeTrackerCard(
    monthSummary: MonthSummary,
    settings: PaySettingsEntity,
    onSelectOvertimeMultiplier: (Double) -> Unit,
    onLogTodayOvertime: () -> Unit
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MoreTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Overtime Pay Tracker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "After ${DateHelper.formatHours(settings.standardDailyHours)}/day • Current OT: ${DateHelper.formatRs(settings.overtimeRateRs)}/hr",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = DateHelper.formatRs(monthSummary.totalOvertimePayRs),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.testTag("monthly_overtime_pay_value")
                    )
                    Text(
                        text = "${DateHelper.formatHours(monthSummary.totalOvertimeHours)} OT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Overtime Rate Multiplier (Base: ${DateHelper.formatRs(settings.hourlyRateRs)}/hr):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(1.0, 1.25, 1.5, 2.0).forEach { mult ->
                    val rateForMult = settings.hourlyRateRs * mult
                    val isSelected = abs(settings.overtimeRateRs - rateForMult) < 0.05
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectOvertimeMultiplier(mult) },
                        label = {
                            Text("${mult}x (${DateHelper.formatRs(rateForMult)}/h)")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        modifier = Modifier.testTag("ot_multiplier_chip_$mult")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onLogTodayOvertime,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("log_today_overtime_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log / Edit Today's Working Hours & Overtime")
            }
        }
    }
}

@Composable
private fun SaturdayPayoutRowCard(payout: MonthWeekPayout) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payout.weekLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${payout.saturdayLabel} • ${payout.daysWorkedInWeek}/6 days • Lunch: ${DateHelper.formatLunchDuration(payout.lunchMinutes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateHelper.formatRs(payout.totalSalaryRs),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${DateHelper.formatHours(payout.totalHours)} (OT: ${DateHelper.formatHours(payout.overtimeHours)})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MonthlyLoggedDayRow(
    slot: DaySlot,
    onClick: () -> Unit
) {
    val shift = slot.shift ?: return
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${slot.dayNameShort}, ${slot.dayMonthFormatted}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${DateHelper.formatMinutesToTime(shift.startMinutes)} – ${DateHelper.formatMinutesToTime(shift.endMinutes)} | Lunch: ${slot.calc.lunchMinutes}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateHelper.formatRs(slot.calc.totalDailyIncomeRs),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${DateHelper.formatHours(slot.calc.totalWorkingHours)} worked" +
                        if (slot.calc.totalOvertimeHours > 0) " (+${DateHelper.formatHours(slot.calc.totalOvertimeHours)} OT)" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (slot.calc.totalOvertimeHours > 0) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
