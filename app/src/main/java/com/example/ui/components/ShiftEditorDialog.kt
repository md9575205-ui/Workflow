package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.DateHelper
import com.example.data.DaySlot
import com.example.data.PaySettingsEntity
import com.example.data.WageCalculator
import com.example.data.WorkShiftEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShiftEditorDialog(
    daySlot: DaySlot,
    settings: PaySettingsEntity,
    onDismiss: () -> Unit,
    onSave: (startMinutes: Int, endMinutes: Int, lunchMinutes: Int, extraOvertimeHours: Double, notes: String) -> Unit,
    onDelete: () -> Unit
) {
    val existing = daySlot.shift
    var startMinutes by remember(daySlot.dateIso) {
        mutableIntStateOf(existing?.startMinutes ?: settings.defaultStartMinutes)
    }
    var endMinutes by remember(daySlot.dateIso) {
        mutableIntStateOf(existing?.endMinutes ?: settings.defaultEndMinutes)
    }
    var lunchMinutes by remember(daySlot.dateIso) {
        mutableIntStateOf(existing?.lunchMinutes ?: settings.defaultLunchMinutes)
    }
    var extraOvertimeHours by remember(daySlot.dateIso) {
        mutableDoubleStateOf(existing?.extraOvertimeHours ?: 0.0)
    }
    var notes by remember(daySlot.dateIso) {
        mutableStateOf(existing?.notes ?: "")
    }

    val previewShift = WorkShiftEntity(
        dateIso = daySlot.dateIso,
        startMinutes = startMinutes,
        endMinutes = endMinutes,
        lunchMinutes = lunchMinutes,
        extraOvertimeHours = extraOvertimeHours,
        hourlyRateOverride = existing?.hourlyRateOverride,
        notes = notes
    )
    val previewCalc = WageCalculator.calculateDay(previewShift, settings)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${daySlot.dayNameFull}, ${daySlot.dayMonthFormatted}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (daySlot.isSaturday) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "PAYDAY",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Rate: ${DateHelper.formatRs(previewCalc.effectiveHourlyRateRs)}/hr • OT: ${DateHelper.formatRs(previewCalc.effectiveOvertimeRateRs)}/hr",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_shift_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close shift editor"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Preview Calculation Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "DAILY INCOME PREVIEW",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = DateHelper.formatRs(previewCalc.totalDailyIncomeRs),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("preview_daily_income")
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Work: ${DateHelper.formatHours(previewCalc.totalWorkingHours)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Lunch: ${previewCalc.lunchMinutes}m | OT: ${DateHelper.formatHours(previewCalc.totalOvertimeHours)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                        if (previewCalc.totalOvertimeHours > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Regular (${DateHelper.formatHours(previewCalc.regularHours)}): ${DateHelper.formatRs(previewCalc.regularPayRs)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Overtime (+${DateHelper.formatHours(previewCalc.totalOvertimeHours)}): ${DateHelper.formatRs(previewCalc.overtimePayRs)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Shift Presets
                Text(
                    text = "Quick Shift Templates",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PresetChip(
                        label = "8h Standard (9–6, 1h Lunch)",
                        onClick = {
                            startMinutes = 540
                            endMinutes = 1080
                            lunchMinutes = 60
                            extraOvertimeHours = 0.0
                        }
                    )
                    PresetChip(
                        label = "8h + 2h OT (9–8, 1h Lunch)",
                        onClick = {
                            startMinutes = 540
                            endMinutes = 1200
                            lunchMinutes = 60
                            extraOvertimeHours = 0.0
                        }
                    )
                    PresetChip(
                        label = "10h Shift (8–7, 1h Lunch)",
                        onClick = {
                            startMinutes = 480
                            endMinutes = 1140
                            lunchMinutes = 60
                            extraOvertimeHours = 0.0
                        }
                    )
                    PresetChip(
                        label = "Half Day 4h (9–1, No Lunch)",
                        onClick = {
                            startMinutes = 540
                            endMinutes = 780
                            lunchMinutes = 0
                            extraOvertimeHours = 0.0
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Start & End Time Steppers
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Shift Timings",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                TimeStepperRow(
                    title = "Start Time",
                    formattedTime = DateHelper.formatMinutesToTime(startMinutes),
                    onMinus30 = { startMinutes = ((startMinutes - 30) + 1440) % 1440 },
                    onPlus30 = { startMinutes = (startMinutes + 30) % 1440 },
                    testTagPrefix = "start_time"
                )
                Spacer(modifier = Modifier.height(8.dp))
                TimeStepperRow(
                    title = "End Time",
                    formattedTime = DateHelper.formatMinutesToTime(endMinutes),
                    onMinus30 = { endMinutes = ((endMinutes - 30) + 1440) % 1440 },
                    onPlus30 = { endMinutes = (endMinutes + 30) % 1440 },
                    testTagPrefix = "end_time"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Lunch Break Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lunch Time: ${DateHelper.formatLunchDuration(lunchMinutes)}",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(0, 30, 45, 60, 90).forEach { mins ->
                        FilterChip(
                            selected = lunchMinutes == mins,
                            onClick = { lunchMinutes = mins },
                            label = { Text(if (mins == 60) "60m (1h)" else "${mins}m") },
                            modifier = Modifier.testTag("lunch_chip_$mins")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Extra Overtime Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bonus / Extra Overtime Hours: +${DateHelper.formatHours(extraOvertimeHours)}",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Text(
                    text = if (settings.autoOvertimeBeyondStandard) {
                        "Hours above ${DateHelper.formatHours(settings.standardDailyHours)} are automatically counted as overtime (${DateHelper.formatHours(previewCalc.autoOvertimeHours)} auto OT). Add extra manual OT below if needed:"
                    } else {
                        "Select extra overtime hours worked today:"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(0.0, 1.0, 1.5, 2.0, 3.0, 4.0).forEach { ot ->
                        FilterChip(
                            selected = extraOvertimeHours == ot,
                            onClick = { extraOvertimeHours = ot },
                            label = { Text(if (ot == 0.0) "0h" else "+${DateHelper.formatHours(ot)}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ),
                            modifier = Modifier.testTag("ot_chip_${ot.toInt()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Day Note (optional)") },
                    placeholder = { Text("e.g., Evening overtime, Site shift") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shift_notes_input")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (existing != null) {
                        OutlinedButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.testTag("delete_shift_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete shift",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove")
                        }
                    }
                    Button(
                        onClick = {
                            onSave(startMinutes, endMinutes, lunchMinutes, extraOvertimeHours, notes)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_shift_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save (${DateHelper.formatRs(previewCalc.totalDailyIncomeRs)})")
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TimeStepperRow(
    title: String,
    formattedTime: String,
    onMinus30: () -> Unit,
    onPlus30: () -> Unit,
    testTagPrefix: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onMinus30,
                    modifier = Modifier.testTag("${testTagPrefix}_minus_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease $title by 30 minutes"
                    )
                }
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(
                    onClick = onPlus30,
                    modifier = Modifier.testTag("${testTagPrefix}_plus_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase $title by 30 minutes"
                    )
                }
            }
        }
    }
}
