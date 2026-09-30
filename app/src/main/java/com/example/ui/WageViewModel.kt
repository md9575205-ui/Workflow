package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DateHelper
import com.example.data.DaySlot
import com.example.data.MonthSummary
import com.example.data.PaySettingsEntity
import com.example.data.WageCalculator
import com.example.data.WageRepository
import com.example.data.WeekSummary
import com.example.data.WorkShiftEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    WEEKLY_TRACKER,
    MONTHLY_REPORT,
    CALCULATOR_SETTINGS
}

data class WageUiState(
    val currentTab: AppTab = AppTab.WEEKLY_TRACKER,
    val todayIso: String = DateHelper.getTodayIso(),
    val settings: PaySettingsEntity = PaySettingsEntity(),
    val currentWeekSummary: WeekSummary,
    val currentMonthSummary: MonthSummary,
    val editingDaySlot: DaySlot? = null,
    val showingMonthlyReportModal: Boolean = false,
    val showSundayInWeek: Boolean = false,
    val statusBannerMessage: String? = null
)

class WageViewModel(private val repository: WageRepository) : ViewModel() {

    private val todayIso = DateHelper.getTodayIso()
    private val initialMondayIso = DateHelper.getMondayOfWeek(todayIso)
    private val initialYearMonth = DateHelper.getCurrentYearMonth()

    private val selectedTabFlow = MutableStateFlow(AppTab.WEEKLY_TRACKER)
    private val selectedMondayIsoFlow = MutableStateFlow(initialMondayIso)
    private val selectedYearMonthFlow = MutableStateFlow(initialYearMonth)
    private val editingDateIsoFlow = MutableStateFlow<String?>(null)
    private val showingReportModalFlow = MutableStateFlow(false)
    private val showSundayFlow = MutableStateFlow(false)
    private val statusMessageFlow = MutableStateFlow<String?>(null)

    private data class NavigationState(
        val tab: AppTab,
        val mondayIso: String,
        val yearMonth: Pair<Int, Int>,
        val editingDateIso: String?,
        val showReportModal: Boolean,
        val showSunday: Boolean,
        val statusMessage: String?
    )

    private val navStateFlow = combine(
        selectedTabFlow,
        selectedMondayIsoFlow,
        selectedYearMonthFlow,
        editingDateIsoFlow,
        combine(showingReportModalFlow, showSundayFlow, statusMessageFlow) { a, b, c -> Triple(a, b, c) }
    ) { tab, mondayIso, yearMonth, editingDateIso, extras ->
        NavigationState(
            tab = tab,
            mondayIso = mondayIso,
            yearMonth = yearMonth,
            editingDateIso = editingDateIso,
            showReportModal = extras.first,
            showSunday = extras.second,
            statusMessage = extras.third
        )
    }

    private val defaultSettings = PaySettingsEntity()
    private val initialWeekSummary = WageCalculator.calculateWeek(
        mondayDateIso = initialMondayIso,
        shiftsByDate = emptyMap(),
        settings = defaultSettings,
        todayIso = todayIso
    )
    private val initialMonthSummary = WageCalculator.calculateMonth(
        year = initialYearMonth.first,
        month = initialYearMonth.second,
        allShifts = emptyList(),
        settings = defaultSettings,
        todayIso = todayIso
    )

    val uiState: StateFlow<WageUiState> = combine(
        repository.allShifts,
        repository.paySettings,
        navStateFlow
    ) { shifts, storedSettings, nav ->
        val settings = storedSettings ?: defaultSettings
        val shiftsByDate = shifts.associateBy { it.dateIso }

        val weekSummary = WageCalculator.calculateWeek(
            mondayDateIso = nav.mondayIso,
            shiftsByDate = shiftsByDate,
            settings = settings,
            todayIso = todayIso
        )

        val monthSummary = WageCalculator.calculateMonth(
            year = nav.yearMonth.first,
            month = nav.yearMonth.second,
            allShifts = shifts,
            settings = settings,
            todayIso = todayIso
        )

        val editingSlot = nav.editingDateIso?.let { dateIso ->
            val dayIndex = DateHelper.getDayOfWeekIndex(dateIso)
            val shift = shiftsByDate[dateIso]
            val calc = WageCalculator.calculateDay(shift, settings)
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

        WageUiState(
            currentTab = nav.tab,
            todayIso = todayIso,
            settings = settings,
            currentWeekSummary = weekSummary,
            currentMonthSummary = monthSummary,
            editingDaySlot = editingSlot,
            showingMonthlyReportModal = nav.showReportModal,
            showSundayInWeek = nav.showSunday,
            statusBannerMessage = nav.statusMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WageUiState(
            currentWeekSummary = initialWeekSummary,
            currentMonthSummary = initialMonthSummary
        )
    )

    fun selectTab(tab: AppTab) {
        selectedTabFlow.value = tab
    }

    fun previousWeek() {
        val nextMonday = DateHelper.addDays(selectedMondayIsoFlow.value, -7)
        selectedMondayIsoFlow.value = nextMonday
        // Keep month synced with Saturday of the selected week
        val satIso = DateHelper.addDays(nextMonday, 5)
        selectedYearMonthFlow.value = DateHelper.getYearMonthFromIso(satIso)
    }

    fun nextWeek() {
        val nextMonday = DateHelper.addDays(selectedMondayIsoFlow.value, 7)
        selectedMondayIsoFlow.value = nextMonday
        val satIso = DateHelper.addDays(nextMonday, 5)
        selectedYearMonthFlow.value = DateHelper.getYearMonthFromIso(satIso)
    }

    fun jumpToCurrentWeek() {
        selectedMondayIsoFlow.value = initialMondayIso
        selectedYearMonthFlow.value = initialYearMonth
    }

    fun previousMonth() {
        val (y, m) = selectedYearMonthFlow.value
        selectedYearMonthFlow.value = DateHelper.previousMonth(y, m)
    }

    fun nextMonth() {
        val (y, m) = selectedYearMonthFlow.value
        selectedYearMonthFlow.value = DateHelper.nextMonth(y, m)
    }

    fun jumpToCurrentMonth() {
        selectedYearMonthFlow.value = initialYearMonth
    }

    fun openShiftEditor(dateIso: String) {
        editingDateIsoFlow.value = dateIso
    }

    fun closeShiftEditor() {
        editingDateIsoFlow.value = null
    }

    fun openMonthlyReportModal() {
        showingReportModalFlow.value = true
    }

    fun closeMonthlyReportModal() {
        showingReportModalFlow.value = false
    }

    fun toggleShowSunday() {
        showSundayFlow.value = !showSundayFlow.value
    }

    fun clearStatusMessage() {
        statusMessageFlow.value = null
    }

    fun showStatusMessage(message: String) {
        statusMessageFlow.value = message
    }

    fun quickLogStandardDay(dateIso: String) {
        val s = uiState.value.settings
        viewModelScope.launch {
            val shift = WorkShiftEntity(
                dateIso = dateIso,
                startMinutes = s.defaultStartMinutes,
                endMinutes = s.defaultEndMinutes,
                lunchMinutes = s.defaultLunchMinutes,
                extraOvertimeHours = 0.0,
                notes = "Standard shift"
            )
            repository.upsertShift(shift)
            statusMessageFlow.value = "Logged shift for ${DateHelper.formatDayMonth(dateIso)}"
        }
    }

    fun quickFillMonToSat() {
        val state = uiState.value
        val s = state.settings
        val monToSatSlots = state.currentWeekSummary.daySlots.filter { it.dayOfWeekIndex in 1..6 }
        viewModelScope.launch {
            val newShifts = monToSatSlots.map { slot ->
                slot.shift ?: WorkShiftEntity(
                    dateIso = slot.dateIso,
                    startMinutes = s.defaultStartMinutes,
                    endMinutes = s.defaultEndMinutes,
                    lunchMinutes = s.defaultLunchMinutes,
                    extraOvertimeHours = 0.0,
                    notes = "Mon–Sat shift"
                )
            }
            repository.upsertShifts(newShifts)
            statusMessageFlow.value = "Filled Monday to Saturday shifts for the week"
        }
    }

    fun clearCurrentWeekShifts() {
        val state = uiState.value
        val dates = state.currentWeekSummary.daySlots.map { it.dateIso }
        viewModelScope.launch {
            repository.deleteShifts(dates)
            statusMessageFlow.value = "Cleared current week shifts"
        }
    }

    fun saveShift(
        dateIso: String,
        startMinutes: Int,
        endMinutes: Int,
        lunchMinutes: Int,
        extraOvertimeHours: Double,
        notes: String
    ) {
        val existing = uiState.value.currentWeekSummary.daySlots.find { it.dateIso == dateIso }?.shift
        viewModelScope.launch {
            repository.upsertShift(
                WorkShiftEntity(
                    dateIso = dateIso,
                    startMinutes = startMinutes,
                    endMinutes = endMinutes,
                    lunchMinutes = lunchMinutes.coerceIn(0, 360),
                    extraOvertimeHours = extraOvertimeHours.coerceIn(0.0, 16.0),
                    hourlyRateOverride = existing?.hourlyRateOverride,
                    notes = notes.trim(),
                    isSaturdaySalaryPaid = existing?.isSaturdaySalaryPaid ?: false
                )
            )
            editingDateIsoFlow.value = null
            statusMessageFlow.value = "Saved shift for ${DateHelper.formatFullDateWithDay(dateIso)}"
        }
    }

    fun deleteShift(dateIso: String) {
        viewModelScope.launch {
            repository.deleteShift(dateIso)
            if (editingDateIsoFlow.value == dateIso) {
                editingDateIsoFlow.value = null
            }
            statusMessageFlow.value = "Removed shift for ${DateHelper.formatDayMonth(dateIso)}"
        }
    }

    fun toggleSaturdayPaidStatus(saturdayDateIso: String) {
        val state = uiState.value
        val currentPaid = state.currentWeekSummary.isSaturdaySalaryPaid
        val s = state.settings
        val existingSat = state.currentWeekSummary.daySlots.find { it.dateIso == saturdayDateIso }?.shift
        viewModelScope.launch {
            if (existingSat != null) {
                repository.upsertShift(existingSat.copy(isSaturdaySalaryPaid = !currentPaid))
            } else {
                // Log Saturday standard shift and mark paid status
                repository.upsertShift(
                    WorkShiftEntity(
                        dateIso = saturdayDateIso,
                        startMinutes = s.defaultStartMinutes,
                        endMinutes = s.defaultEndMinutes,
                        lunchMinutes = s.defaultLunchMinutes,
                        isSaturdaySalaryPaid = !currentPaid
                    )
                )
            }
            statusMessageFlow.value = if (!currentPaid) {
                "Saturday salary marked as Paid"
            } else {
                "Saturday salary marked as Pending"
            }
        }
    }

    fun savePaySettings(newSettings: PaySettingsEntity) {
        viewModelScope.launch {
            repository.savePaySettings(newSettings.copy(id = 1))
            statusMessageFlow.value = "Pay rate & shift settings saved (₹${newSettings.hourlyRateRs.toInt()}/hr)"
        }
    }

    fun updateOvertimeRateMultiplier(multiplier: Double) {
        val current = uiState.value.settings
        val newOtRate = current.hourlyRateRs * multiplier
        viewModelScope.launch {
            repository.savePaySettings(current.copy(id = 1, overtimeRateRs = newOtRate))
            statusMessageFlow.value = "Overtime rate set to ${DateHelper.formatRs(newOtRate)}/hr (${multiplier}x)"
        }
    }

    fun setThemeMode(mode: com.example.data.AppThemeMode) {
        val current = uiState.value.settings
        viewModelScope.launch {
            repository.savePaySettings(current.copy(id = 1, themeMode = mode.key))
            statusMessageFlow.value = "Switched to ${mode.label} Theme"
        }
    }

    fun toggleDarkLightTheme(currentlyDark: Boolean) {
        val nextMode = if (currentlyDark) {
            com.example.data.AppThemeMode.LIGHT
        } else {
            com.example.data.AppThemeMode.DARK
        }
        setThemeMode(nextMode)
    }

    companion object {
        fun provideFactory(repository: WageRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WageViewModel(repository) as T
                }
            }
    }
}
