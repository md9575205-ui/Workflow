package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.AppThemeMode
import com.example.data.DateHelper
import com.example.data.WageCalculator
import com.example.ui.components.AdvanceDeductionDialog
import com.example.ui.components.MonthlyReportDialog
import com.example.ui.components.SaturdayPaySlipDialog
import com.example.ui.components.ShiftEditorDialog
import com.example.ui.components.TimesheetCsvDialog
import com.example.ui.screens.CalculatorSettingsScreen
import com.example.ui.screens.MonthlyReportScreen
import com.example.ui.screens.WeeklyTrackerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WageFlowApp(
    viewModel: WageViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val systemDark = isSystemInDarkTheme()
    val isCurrentlyDark = when (AppThemeMode.fromKey(state.settings.themeMode)) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> systemDark
    }

    // Handle Back navigation on secondary tabs
    BackHandler(enabled = state.currentTab != AppTab.WEEKLY_TRACKER) {
        viewModel.selectTab(AppTab.WEEKLY_TRACKER)
    }

    LaunchedEffect(state.statusBannerMessage) {
        val msg = state.statusBannerMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val generatedReportText = remember(state.currentMonthSummary, state.settings) {
        WageCalculator.generateMonthlyReportText(
            monthSummary = state.currentMonthSummary,
            settings = state.settings
        )
    }

    val shareReportAction: (String) -> Unit = { reportBody ->
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "WageFlow Monthly Salary Report - ${state.currentMonthSummary.monthTitle}")
            putExtra(Intent.EXTRA_TEXT, reportBody)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Monthly Salary Report")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    val copyReportAction: (String) -> Unit = { reportBody ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("WageFlow Monthly Report", reportBody))
        viewModel.showStatusMessage("Monthly salary report copied to clipboard")
    }

    val generatedPaySlipText = remember(state.currentWeekSummary, state.settings) {
        WageCalculator.generateSaturdayPaySlipText(
            weekSummary = state.currentWeekSummary,
            settings = state.settings
        )
    }

    val generatedCsvText = remember(state.currentMonthSummary, state.settings) {
        WageCalculator.generateMonthCsv(
            monthSummary = state.currentMonthSummary,
            settings = state.settings
        )
    }

    val sharePaySlipAction: (String) -> Unit = { slipBody ->
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "WageFlow Saturday Pay Slip - ${state.currentWeekSummary.saturdayDateLabel}")
            putExtra(Intent.EXTRA_TEXT, slipBody)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Saturday Pay Slip")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    val copyPaySlipAction: (String) -> Unit = { slipBody ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("WageFlow Saturday Pay Slip", slipBody))
        viewModel.showStatusMessage("Weekly Saturday pay slip copied to clipboard")
    }

    val shareCsvAction: (String) -> Unit = { csvBody ->
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "WageFlow Timesheet CSV - ${state.currentMonthSummary.monthTitle}")
            putExtra(Intent.EXTRA_TEXT, csvBody)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Timesheet CSV")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    val copyCsvAction: (String) -> Unit = { csvBody ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("WageFlow Timesheet CSV", csvBody))
        viewModel.showStatusMessage("Timesheet CSV copied to clipboard")
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(id = R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${DateHelper.formatRs(state.settings.hourlyRateRs)}/hr • Saturday Salary & Overtime",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.toggleDarkLightTheme(isCurrentlyDark) },
                            modifier = Modifier.testTag("top_bar_theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isCurrentlyDark) {
                                    Icons.Default.LightMode
                                } else {
                                    Icons.Default.DarkMode
                                },
                                contentDescription = stringResource(id = R.string.action_toggle_theme),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.openMonthlyReportModal() },
                            modifier = Modifier.testTag("top_bar_report_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = stringResource(id = R.string.action_generate_report),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            floatingActionButton = {
                if (state.currentTab == AppTab.WEEKLY_TRACKER) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openShiftEditor(state.todayIso) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("fab_log_today_shift")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddTask,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Today")
                    }
                }
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBar(
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        NavigationBarItem(
                            selected = state.currentTab == AppTab.WEEKLY_TRACKER,
                            onClick = { viewModel.selectTab(AppTab.WEEKLY_TRACKER) },
                            icon = {
                                Icon(
                                    imageVector = if (state.currentTab == AppTab.WEEKLY_TRACKER) {
                                        Icons.Filled.DateRange
                                    } else {
                                        Icons.Outlined.DateRange
                                    },
                                    contentDescription = stringResource(id = R.string.nav_weekly)
                                )
                            },
                            label = { Text(stringResource(id = R.string.nav_weekly)) },
                            modifier = Modifier.testTag("nav_tab_weekly")
                        )
                        NavigationBarItem(
                            selected = state.currentTab == AppTab.MONTHLY_REPORT,
                            onClick = { viewModel.selectTab(AppTab.MONTHLY_REPORT) },
                            icon = {
                                Icon(
                                    imageVector = if (state.currentTab == AppTab.MONTHLY_REPORT) {
                                        Icons.Filled.Assessment
                                    } else {
                                        Icons.Outlined.Assessment
                                    },
                                    contentDescription = stringResource(id = R.string.nav_monthly)
                                )
                            },
                            label = { Text(stringResource(id = R.string.nav_monthly)) },
                            modifier = Modifier.testTag("nav_tab_monthly")
                        )
                        NavigationBarItem(
                            selected = state.currentTab == AppTab.CALCULATOR_SETTINGS,
                            onClick = { viewModel.selectTab(AppTab.CALCULATOR_SETTINGS) },
                            icon = {
                                Icon(
                                    imageVector = if (state.currentTab == AppTab.CALCULATOR_SETTINGS) {
                                        Icons.Filled.Calculate
                                    } else {
                                        Icons.Outlined.Calculate
                                    },
                                    contentDescription = stringResource(id = R.string.nav_calculator)
                                )
                            },
                            label = { Text(stringResource(id = R.string.nav_calculator)) },
                            modifier = Modifier.testTag("nav_tab_calculator")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("side_navigation_rail")
                    ) {
                        NavigationRailItem(
                            selected = state.currentTab == AppTab.WEEKLY_TRACKER,
                            onClick = { viewModel.selectTab(AppTab.WEEKLY_TRACKER) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = stringResource(id = R.string.nav_weekly)
                                )
                            },
                            label = { Text(stringResource(id = R.string.nav_weekly)) },
                            modifier = Modifier.testTag("rail_tab_weekly")
                        )
                        NavigationRailItem(
                            selected = state.currentTab == AppTab.MONTHLY_REPORT,
                            onClick = { viewModel.selectTab(AppTab.MONTHLY_REPORT) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = stringResource(id = R.string.nav_monthly)
                                )
                            },
                            label = { Text(stringResource(id = R.string.nav_monthly)) },
                            modifier = Modifier.testTag("rail_tab_monthly")
                        )
                        NavigationRailItem(
                            selected = state.currentTab == AppTab.CALCULATOR_SETTINGS,
                            onClick = { viewModel.selectTab(AppTab.CALCULATOR_SETTINGS) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = stringResource(id = R.string.nav_calculator)
                                )
                            },
                            label = { Text(stringResource(id = R.string.nav_calculator)) },
                            modifier = Modifier.testTag("rail_tab_calculator")
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val contentMod = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 760.dp)

                    when (state.currentTab) {
                        AppTab.WEEKLY_TRACKER -> {
                            WeeklyTrackerScreen(
                                weekSummary = state.currentWeekSummary,
                                monthSummary = state.currentMonthSummary,
                                settings = state.settings,
                                showSunday = state.showSundayInWeek,
                                onPrevWeek = { viewModel.previousWeek() },
                                onNextWeek = { viewModel.nextWeek() },
                                onJumpToday = { viewModel.jumpToCurrentWeek() },
                                onQuickLogDay = { dateIso -> viewModel.quickLogStandardDay(dateIso) },
                                onEditDay = { dateIso -> viewModel.openShiftEditor(dateIso) },
                                onQuickFillMonToSat = { viewModel.quickFillMonToSat() },
                                onClearWeek = { viewModel.clearCurrentWeekShifts() },
                                onToggleSaturdayPaid = { satIso -> viewModel.toggleSaturdayPaidStatus(satIso) },
                                onToggleShowSunday = { viewModel.toggleShowSunday() },
                                onNavigateToMonthlyReport = { viewModel.selectTab(AppTab.MONTHLY_REPORT) },
                                onNavigateToRateSettings = { viewModel.selectTab(AppTab.CALCULATOR_SETTINGS) },
                                onSaveClockShift = { start, end, lunch -> viewModel.saveClockInShift(start, end, lunch) },
                                onAddAdvanceClick = { viewModel.openAddAdvanceModal(state.currentWeekSummary.saturdayDateIso) },
                                onDeleteAdvance = { id -> viewModel.deleteAdvanceDeduction(id) },
                                onOpenSaturdayPaySlip = { viewModel.openSaturdayPaySlipModal() },
                                modifier = contentMod
                            )
                        }

                        AppTab.MONTHLY_REPORT -> {
                            MonthlyReportScreen(
                                monthSummary = state.currentMonthSummary,
                                settings = state.settings,
                                todayIso = state.todayIso,
                                onPrevMonth = { viewModel.previousMonth() },
                                onNextMonth = { viewModel.nextMonth() },
                                onOpenReportModal = { viewModel.openMonthlyReportModal() },
                                onShareReport = { shareReportAction(generatedReportText) },
                                onCopyReport = { copyReportAction(generatedReportText) },
                                onSelectOvertimeMultiplier = { mult ->
                                    viewModel.updateOvertimeRateMultiplier(mult)
                                },
                                onEditDay = { dateIso -> viewModel.openShiftEditor(dateIso) },
                                onGoToWeeklyTracker = { viewModel.selectTab(AppTab.WEEKLY_TRACKER) },
                                onOpenCsvExport = { viewModel.openTimesheetCsvModal() },
                                modifier = contentMod
                            )
                        }

                        AppTab.CALCULATOR_SETTINGS -> {
                            CalculatorSettingsScreen(
                                settings = state.settings,
                                onSaveSettings = { newSettings ->
                                    viewModel.savePaySettings(newSettings)
                                },
                                onSelectThemeMode = { mode ->
                                    viewModel.setThemeMode(mode)
                                },
                                modifier = contentMod
                            )
                        }
                    }
                }
            }
        }
    }

    // Shift Editor Modal
    state.editingDaySlot?.let { slot ->
        ShiftEditorDialog(
            daySlot = slot,
            settings = state.settings,
            onDismiss = { viewModel.closeShiftEditor() },
            onSave = { startMins, endMins, lunchMins, extraOt, notes ->
                viewModel.saveShift(
                    dateIso = slot.dateIso,
                    startMinutes = startMins,
                    endMinutes = endMins,
                    lunchMinutes = lunchMins,
                    extraOvertimeHours = extraOt,
                    notes = notes
                )
            },
            onDelete = { viewModel.deleteShift(slot.dateIso) }
        )
    }

    // Monthly Report Statement Modal
    if (state.showingMonthlyReportModal) {
        MonthlyReportDialog(
            monthSummary = state.currentMonthSummary,
            settings = state.settings,
            reportText = generatedReportText,
            onDismiss = { viewModel.closeMonthlyReportModal() },
            onShareReport = { text -> shareReportAction(text) },
            onCopyReport = { text -> copyReportAction(text) }
        )
    }

    // Advance / Deduction Record Modal
    if (state.showingAddAdvanceModal) {
        AdvanceDeductionDialog(
            initialDateIso = state.advanceModalDateIso,
            onDismiss = { viewModel.closeAddAdvanceModal() },
            onSave = { dateIso, amountRs, isDeduction, note ->
                viewModel.saveAdvanceDeduction(dateIso, amountRs, isDeduction, note)
            }
        )
    }

    // Saturday Pay Slip Voucher Modal
    if (state.showingSaturdayPaySlipModal) {
        SaturdayPaySlipDialog(
            weekSummary = state.currentWeekSummary,
            settings = state.settings,
            paySlipText = generatedPaySlipText,
            onDismiss = { viewModel.closeSaturdayPaySlipModal() },
            onShare = { text -> sharePaySlipAction(text) },
            onCopy = { text -> copyPaySlipAction(text) }
        )
    }

    // Monthly Timesheet CSV Export Modal
    if (state.showingTimesheetCsvModal) {
        TimesheetCsvDialog(
            monthSummary = state.currentMonthSummary,
            csvContent = generatedCsvText,
            onDismiss = { viewModel.closeTimesheetCsvModal() },
            onShareCsv = { text -> shareCsvAction(text) },
            onCopyCsv = { text -> copyCsvAction(text) }
        )
    }
}
