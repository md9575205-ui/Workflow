package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DateHelper
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

enum class PunchState {
    IDLE,
    WORKING,
    ON_LUNCH
}

@Composable
fun LiveShiftPunchClock(
    hourlyRateRs: Double,
    onSaveFinishedShift: (startMinutes: Int, endMinutes: Int, lunchMinutes: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var punchState by remember { mutableStateOf(PunchState.IDLE) }
    var clockInTimestamp by remember { mutableLongStateOf(0L) }
    var clockInMinutesOfDay by remember { mutableIntStateOf(0) }
    var accumulatedWorkSeconds by remember { mutableLongStateOf(0L) }
    var lastWorkResumeTimestamp by remember { mutableLongStateOf(0L) }

    var lunchStartTimestamp by remember { mutableLongStateOf(0L) }
    var accumulatedLunchSeconds by remember { mutableLongStateOf(0L) }

    // Live ticking effect while shift is active
    LaunchedEffect(punchState) {
        while (punchState != PunchState.IDLE) {
            delay(1000L)
            val now = System.currentTimeMillis()
            if (punchState == PunchState.WORKING) {
                val elapsedSinceResume = (now - lastWorkResumeTimestamp) / 1000L
                accumulatedWorkSeconds += elapsedSinceResume
                lastWorkResumeTimestamp = now
            } else if (punchState == PunchState.ON_LUNCH) {
                accumulatedLunchSeconds += 1
            }
        }
    }

    val liveEarningsRs = (accumulatedWorkSeconds / 3600.0) * hourlyRateRs

    val statusBgColor by animateColorAsState(
        targetValue = when (punchState) {
            PunchState.IDLE -> MaterialTheme.colorScheme.surfaceVariant
            PunchState.WORKING -> MaterialTheme.colorScheme.primaryContainer
            PunchState.ON_LUNCH -> MaterialTheme.colorScheme.tertiaryContainer
        },
        label = "punch_status_bg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_punch_clock_card"),
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(statusBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (punchState) {
                                PunchState.IDLE -> Icons.Default.Timer
                                PunchState.WORKING -> Icons.Default.PlayArrow
                                PunchState.ON_LUNCH -> Icons.Default.Restaurant
                            },
                            contentDescription = null,
                            tint = when (punchState) {
                                PunchState.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
                                PunchState.WORKING -> MaterialTheme.colorScheme.primary
                                PunchState.ON_LUNCH -> MaterialTheme.colorScheme.tertiary
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Live Shift Clock & Real-time Pay",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (punchState) {
                                PunchState.IDLE -> "Punch in when you start work today"
                                PunchState.WORKING -> "Active Shift • Earning at ${DateHelper.formatRs(hourlyRateRs)}/hr"
                                PunchState.ON_LUNCH -> "On Lunch Break • Work clock paused"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (punchState != PunchState.IDLE) {
                    IconButton(
                        onClick = {
                            punchState = PunchState.IDLE
                            accumulatedWorkSeconds = 0L
                            accumulatedLunchSeconds = 0L
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel clock",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Display Screen
            Surface(
                color = statusBgColor,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (punchState == PunchState.IDLE) "WORK TIMER" else "TIME WORKED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatSecondsToHms(accumulatedWorkSeconds),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (punchState != PunchState.IDLE) {
                            Text(
                                text = "Started at: ${DateHelper.formatMinutesToTime(clockInMinutesOfDay)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "EARNED TODAY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = DateHelper.formatRs(liveEarningsRs),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Lunch: ${formatSecondsToMs(accumulatedLunchSeconds)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (punchState == PunchState.ON_LUNCH) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = if (punchState == PunchState.ON_LUNCH) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Controls
            when (punchState) {
                PunchState.IDLE -> {
                    Button(
                        onClick = {
                            val cal = Calendar.getInstance()
                            clockInTimestamp = cal.timeInMillis
                            clockInMinutesOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                            lastWorkResumeTimestamp = System.currentTimeMillis()
                            accumulatedWorkSeconds = 0L
                            accumulatedLunchSeconds = 0L
                            punchState = PunchState.WORKING
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("punch_clock_in_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clock In (Start Work)", fontWeight = FontWeight.Bold)
                    }
                }

                PunchState.WORKING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                lunchStartTimestamp = System.currentTimeMillis()
                                punchState = PunchState.ON_LUNCH
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("punch_lunch_button")
                        ) {
                            Icon(imageVector = Icons.Default.FreeBreakfast, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Take Lunch")
                        }

                        Button(
                            onClick = {
                                val cal = Calendar.getInstance()
                                val endMins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                                val lunchMins = ((accumulatedLunchSeconds + 30) / 60).toInt().coerceAtLeast(0)
                                onSaveFinishedShift(clockInMinutesOfDay, endMins, lunchMins)
                                punchState = PunchState.IDLE
                                accumulatedWorkSeconds = 0L
                                accumulatedLunchSeconds = 0L
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("punch_clock_out_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clock Out & Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                PunchState.ON_LUNCH -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                lastWorkResumeTimestamp = System.currentTimeMillis()
                                punchState = PunchState.WORKING
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("punch_resume_work_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resume Work", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance()
                                val endMins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                                val lunchMins = ((accumulatedLunchSeconds + 30) / 60).toInt().coerceAtLeast(0)
                                onSaveFinishedShift(clockInMinutesOfDay, endMins, lunchMins)
                                punchState = PunchState.IDLE
                                accumulatedWorkSeconds = 0L
                                accumulatedLunchSeconds = 0L
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("punch_finish_shift_button")
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Finish Shift")
                        }
                    }
                }
            }
        }
    }
}

private fun formatSecondsToHms(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
}

private fun formatSecondsToMs(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.US, "%02dm %02ds", m, s)
}
