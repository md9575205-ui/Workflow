package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DateHelper
import com.example.data.WeekSummary
import kotlin.math.max

@Composable
fun WeeklyEarningsBarChart(
    weekSummary: WeekSummary,
    showSunday: Boolean,
    onDayClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val displaySlots = if (showSunday) {
        weekSummary.daySlots
    } else {
        weekSummary.daySlots.filter { it.dayOfWeekIndex in 1..6 }
    }

    val maxDailyIncome = max(1.0, displaySlots.maxOfOrNull { it.calc.totalDailyIncomeRs } ?: 1.0)
    val maxBarHeightDp = 96.dp
    val minBarHeightDp = 8.dp

    val highestDaySlot = displaySlots.filter { it.calc.isLogged && it.calc.totalDailyIncomeRs > 0 }
        .maxByOrNull { it.calc.totalDailyIncomeRs }

    val daysWorked = displaySlots.count { it.calc.isLogged }
    val avgPerDay = if (daysWorked > 0) weekSummary.monToSatTotalIncomeRs / daysWorked else 0.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_earnings_bar_chart"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Daily Income Chart",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Monday to Saturday earning distribution",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Total: ${DateHelper.formatRs(weekSummary.monToSatTotalIncomeRs)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bars Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                displaySlots.forEach { slot ->
                    val income = slot.calc.totalDailyIncomeRs
                    val fraction = (income / maxDailyIncome).coerceIn(0.0, 1.0).toFloat()
                    val barHeight = minBarHeightDp + (maxBarHeightDp - minBarHeightDp) * fraction
                    val isLogged = slot.calc.isLogged

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDayClick(slot.dateIso) }
                            .padding(horizontal = 2.dp)
                            .testTag("bar_${slot.dayNameShort.lowercase()}")
                    ) {
                        // Earnings Label
                        Text(
                            text = if (isLogged && income > 0) "₹${income.toInt()}" else if (isLogged) "₹0" else "—",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = if (slot.isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (slot.isToday) {
                                MaterialTheme.colorScheme.primary
                            } else if (isLogged) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Bar
                        val barColor = when {
                            slot.isToday && isLogged -> Brush.verticalGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                            slot.isToday && !isLogged -> Brush.verticalGradient(
                                listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            )
                            isLogged && income > 0 -> Brush.verticalGradient(
                                listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f), MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                            )
                            else -> Brush.verticalGradient(
                                listOf(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.65f)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                .background(barColor)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Day Name
                        Text(
                            text = slot.dayNameShort,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (slot.isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (slot.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )

                        // Hours subtext
                        Text(
                            text = if (isLogged) "${DateHelper.formatHours(slot.calc.totalWorkingHours).removeSuffix("h")}h" else "Off",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (slot.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Footer Metrics Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (highestDaySlot != null) {
                            "Peak: ${highestDaySlot.dayNameShort} (${DateHelper.formatRs(highestDaySlot.calc.totalDailyIncomeRs)})"
                        } else {
                            "Peak: —"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Daily Avg: ${DateHelper.formatRs(avgPerDay)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${weekSummary.monToSatDaysWorked}/6 Days",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
