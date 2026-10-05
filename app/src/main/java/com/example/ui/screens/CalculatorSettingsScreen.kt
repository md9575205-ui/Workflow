package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.AppThemeMode
import com.example.data.DateHelper
import com.example.data.PaySettingsEntity
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalculatorSettingsScreen(
    settings: PaySettingsEntity,
    onSaveSettings: (PaySettingsEntity) -> Unit,
    onSelectThemeMode: (AppThemeMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 1. Interactive What-If Salary Estimator State
    var calcWorkHoursPerDay by remember(settings.standardDailyHours) {
        mutableDoubleStateOf(settings.standardDailyHours)
    }
    var calcOvertimePerDay by remember { mutableDoubleStateOf(0.0) }
    var calcLunchMinutesPerDay by remember(settings.defaultLunchMinutes) {
        mutableIntStateOf(settings.defaultLunchMinutes)
    }
    var calcDaysPerWeek by remember { mutableIntStateOf(6) } // Default 6 days: Monday to Saturday

    // 2. Persistent Pay Rate & Shift Settings Form State
    var hourlyRateText by remember(settings.hourlyRateRs) {
        mutableStateOf(DateHelper.formatHours(settings.hourlyRateRs).removeSuffix("h"))
    }
    var overtimeRateText by remember(settings.overtimeRateRs) {
        mutableStateOf(DateHelper.formatHours(settings.overtimeRateRs).removeSuffix("h"))
    }
    var standardHoursText by remember(settings.standardDailyHours) {
        mutableStateOf(DateHelper.formatHours(settings.standardDailyHours).removeSuffix("h"))
    }
    var defaultLunchText by remember(settings.defaultLunchMinutes) {
        mutableStateOf(settings.defaultLunchMinutes.toString())
    }
    var deductLunch by remember(settings.deductLunchTime) {
        mutableStateOf(settings.deductLunchTime)
    }
    var autoOvertime by remember(settings.autoOvertimeBeyondStandard) {
        mutableStateOf(settings.autoOvertimeBeyondStandard)
    }
    var targetGoalText by remember(settings.monthlyTargetGoalRs) {
        mutableStateOf(settings.monthlyTargetGoalRs.toInt().toString())
    }

    val effectiveBaseRate = hourlyRateText.toDoubleOrNull() ?: settings.hourlyRateRs
    val effectiveOtRate = overtimeRateText.toDoubleOrNull() ?: settings.overtimeRateRs

    val estDailyRegularPay = calcWorkHoursPerDay * effectiveBaseRate
    val estDailyOtPay = calcOvertimePerDay * effectiveOtRate
    val estDailyTotalPay = estDailyRegularPay + estDailyOtPay
    val estDailyTotalHours = calcWorkHoursPerDay + calcOvertimePerDay

    val estWeeklyHours = estDailyTotalHours * calcDaysPerWeek
    val estWeeklyPay = estDailyTotalPay * calcDaysPerWeek
    val estMonthlyPay26Days = estDailyTotalPay * 26 // 26 working days in a standard Mon-Sat month

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calculator_settings_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Instant Working Hours & Salary Calculator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Quick Salary & Hours Calculator",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Instant estimate at ${DateHelper.formatRs(effectiveBaseRate)}/hr (OT: ${DateHelper.formatRs(effectiveOtRate)}/hr)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Results Grid
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "DAILY INCOME",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                    )
                                    Text(
                                        text = DateHelper.formatRs(estDailyTotalPay),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.testTag("calc_daily_income_value")
                                    )
                                    Text(
                                        text = "${DateHelper.formatHours(estDailyTotalHours)} work + ${calcLunchMinutesPerDay}m lunch",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "SATURDAY WEEKLY (${calcDaysPerWeek}d)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                    )
                                    Text(
                                        text = DateHelper.formatRs(estWeeklyPay),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.testTag("calc_weekly_income_value")
                                    )
                                    Text(
                                        text = "${DateHelper.formatHours(estWeeklyHours)} total week hours",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estimated Monthly Income (26 Mon–Sat Days):",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = DateHelper.formatRs(estMonthlyPay26Days),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.testTag("calc_monthly_income_value")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sliders
                    Text(
                        text = "Regular Working Hours / Day: ${DateHelper.formatHours(calcWorkHoursPerDay)} (${DateHelper.formatRs(estDailyRegularPay)})",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = calcWorkHoursPerDay.toFloat(),
                        onValueChange = { calcWorkHoursPerDay = (it * 2).roundToInt() / 2.0 },
                        valueRange = 1f..12f,
                        steps = 21,
                        modifier = Modifier.testTag("slider_regular_hours")
                    )

                    Text(
                        text = "Overtime Hours / Day: +${DateHelper.formatHours(calcOvertimePerDay)} (${DateHelper.formatRs(estDailyOtPay)})",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = calcOvertimePerDay.toFloat(),
                        onValueChange = { calcOvertimePerDay = (it * 2).roundToInt() / 2.0 },
                        valueRange = 0f..8f,
                        steps = 15,
                        modifier = Modifier.testTag("slider_overtime_hours")
                    )

                    Text(
                        text = "Lunch Time / Day: ${DateHelper.formatLunchDuration(calcLunchMinutesPerDay)}",
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        listOf(0, 30, 45, 60, 90).forEach { mins ->
                            FilterChip(
                                selected = calcLunchMinutesPerDay == mins,
                                onClick = { calcLunchMinutesPerDay = mins },
                                label = { Text("${mins}m") }
                            )
                        }
                    }

                    Text(
                        text = "Working Days / Week: $calcDaysPerWeek days (Mon–Sat = 6)",
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        (4..7).forEach { d ->
                            FilterChip(
                                selected = calcDaysPerWeek == d,
                                onClick = { calcDaysPerWeek = d },
                                label = { Text(if (d == 6) "6d (Mon–Sat)" else "${d}d") }
                            )
                        }
                    }
                }
            }
        }

        // Card 2: App Theme (Light / Dark / System)
        item {
            val activeThemeMode = AppThemeMode.fromKey(settings.themeMode)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_settings_card"),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Appearance & Theme",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Switch between Light theme, Dark theme, or System default",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = activeThemeMode == AppThemeMode.LIGHT,
                            onClick = { onSelectThemeMode(AppThemeMode.LIGHT) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LightMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            label = { Text("Light Theme") },
                            modifier = Modifier.testTag("theme_option_light")
                        )

                        FilterChip(
                            selected = activeThemeMode == AppThemeMode.DARK,
                            onClick = { onSelectThemeMode(AppThemeMode.DARK) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            label = { Text("Dark Theme") },
                            modifier = Modifier.testTag("theme_option_dark")
                        )

                        FilterChip(
                            selected = activeThemeMode == AppThemeMode.SYSTEM,
                            onClick = { onSelectThemeMode(AppThemeMode.SYSTEM) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.BrightnessAuto,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            label = { Text("System Default") },
                            modifier = Modifier.testTag("theme_option_system")
                        )
                    }
                }
            }
        }

        // Card 3: Persistent Pay Rate & Shift Rules Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Salary Rate & Shift Settings",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Default is ₹40/hour • Salary calculated every Saturday",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = hourlyRateText,
                            onValueChange = { hourlyRateText = it },
                            label = { Text("Hourly Rate (₹/hr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_hourly_rate")
                        )

                        OutlinedTextField(
                            value = overtimeRateText,
                            onValueChange = { overtimeRateText = it },
                            label = { Text("Overtime Rate (₹/hr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_overtime_rate")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Quick Overtime Rate Presets:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        listOf(1.0, 1.25, 1.5, 2.0).forEach { mult ->
                            val r = effectiveBaseRate * mult
                            FilterChip(
                                selected = (overtimeRateText.toDoubleOrNull() ?: 0.0) == r,
                                onClick = {
                                    overtimeRateText = DateHelper.formatHours(r).removeSuffix("h")
                                },
                                label = { Text("${mult}x (₹${r.toInt()})") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = standardHoursText,
                            onValueChange = { standardHoursText = it },
                            label = { Text("Standard Hours/Day") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_standard_hours")
                        )

                        OutlinedTextField(
                            value = defaultLunchText,
                            onValueChange = { defaultLunchText = it },
                            label = { Text("Default Lunch (mins)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_default_lunch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Deduct Lunch Time from Shift Hours",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "E.g., 9:00 AM–6:00 PM (9h) minus 60m lunch = 8.0h work",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = deductLunch,
                            onCheckedChange = { deductLunch = it },
                            modifier = Modifier.testTag("switch_deduct_lunch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Track Overtime Beyond Standard Hours",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Any hours worked above standard daily hours count as Overtime",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoOvertime,
                            onCheckedChange = { autoOvertime = it },
                            modifier = Modifier.testTag("switch_auto_overtime")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = targetGoalText,
                        onValueChange = { targetGoalText = it },
                        label = { Text("Monthly Income Target Goal (₹)") },
                        leadingIcon = {
                            Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        supportingText = { Text("Used for Monthly Goal Progress on report screen") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_target_goal")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Quick Goal Presets:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        listOf(8000, 10000, 12000, 15000, 20000).forEach { goal ->
                            FilterChip(
                                selected = targetGoalText == goal.toString(),
                                onClick = { targetGoalText = goal.toString() },
                                label = { Text("₹$goal") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val newHourly = (hourlyRateText.toDoubleOrNull() ?: 40.0).coerceAtLeast(1.0)
                            val newOt = (overtimeRateText.toDoubleOrNull() ?: newHourly).coerceAtLeast(1.0)
                            val newStdHours = (standardHoursText.toDoubleOrNull() ?: 8.0).coerceIn(1.0, 24.0)
                            val newLunch = (defaultLunchText.toIntOrNull() ?: 60).coerceIn(0, 240)
                            val newGoal = (targetGoalText.toDoubleOrNull() ?: 10000.0).coerceAtLeast(0.0)
                            onSaveSettings(
                                settings.copy(
                                    hourlyRateRs = newHourly,
                                    overtimeRateRs = newOt,
                                    standardDailyHours = newStdHours,
                                    defaultLunchMinutes = newLunch,
                                    deductLunchTime = deductLunch,
                                    autoOvertimeBeyondStandard = autoOvertime,
                                    monthlyTargetGoalRs = newGoal
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Pay Settings")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
