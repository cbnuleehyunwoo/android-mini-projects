package com.woowacourse.runpamine.shared.ui.screens.running

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.shared.ui.components.RunpamineConfirmationDialog
import com.woowacourse.runpamine.shared.ui.model.RunningPhase
import com.woowacourse.runpamine.shared.ui.model.RunningUiState
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTypography

@Composable
fun RunningScreen(
    state: RunningUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    locationPermissionGranted: Boolean = true,
    onOpenLocationSettings: () -> Unit = {},
    onDismissError: () -> Unit = {},
) {
    var isStopDialogVisible by remember { mutableStateOf(false) }
    val isPaused = state.phase == RunningPhase.Paused
    val isSaving = state.phase == RunningPhase.Saving

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            DistanceSection(distanceKm = state.distanceKm)
            DurationSection(elapsedText = state.elapsedText)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                RunningMetricCard(
                    title = "페이스",
                    value = state.paceText,
                    suffix = "/km",
                    modifier = Modifier.weight(1f),
                )
                RunningMetricCard(
                    title = "칼로리",
                    value = state.calories.toString(),
                    suffix = "kcal",
                    modifier = Modifier.weight(1f),
                )
            }

            RunningControls(
                isPaused = isPaused,
                enabled = !isSaving,
                onStop = { isStopDialogVisible = true },
                onTogglePause = if (isPaused) onResume else onPause,
                modifier = Modifier.padding(top = 16.dp, bottom = 42.dp),
            )
        }

        if (isSaving) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.78f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = RunpamineColors.Primary,
                        strokeWidth = 3.dp,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "러닝 기록을 저장하고 있어요",
                        style = RunpamineTypography.Body2.copy(fontWeight = FontWeight.Bold),
                        color = RunpamineColors.TextSecondary,
                    )
                }
            }
        }

        errorMessage?.let { message ->
            RunningErrorBanner(
                message = message,
                showLocationSettings = !locationPermissionGranted,
                onOpenLocationSettings = onOpenLocationSettings,
                onDismiss = onDismissError,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }

    if (isStopDialogVisible) {
        RunpamineConfirmationDialog(
            title = "러닝 종료",
            message = "러닝을 종료하시겠습니까?",
            dismissText = "취소",
            confirmText = "종료",
            onDismiss = { isStopDialogVisible = false },
            onConfirm = {
                isStopDialogVisible = false
                onStop()
            },
        )
    }
}

@Composable
private fun RunningErrorBanner(
    message: String,
    showLocationSettings: Boolean,
    onOpenLocationSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFF2F2))
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = message,
            style = RunpamineTypography.Body2.copy(fontWeight = FontWeight.SemiBold),
            color = RunpamineColors.Danger,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "닫기",
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onDismiss).padding(8.dp),
                style = RunpamineTypography.Caption1.copy(fontWeight = FontWeight.Bold),
                color = RunpamineColors.TextSecondary,
            )
            if (showLocationSettings) {
                Text(
                    text = "설정 열기",
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onOpenLocationSettings)
                            .padding(8.dp),
                    style = RunpamineTypography.Caption1.copy(fontWeight = FontWeight.Bold),
                    color = RunpamineColors.Primary,
                )
            }
        }
    }
}

@Composable
private fun DistanceSection(distanceKm: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "거리",
            style = RunpamineTypography.Body2,
            color = Color.Black,
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                text = formatDistanceKm(distanceKm),
                style =
                    RunpamineTypography.Header1.copy(
                        fontSize = 80.sp,
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = Color.Black,
            )
            Text(
                text = "km",
                modifier = Modifier.padding(bottom = 11.dp),
                style =
                    RunpamineTypography.Header2.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                    ),
                color = RunningColors.Unit,
            )
        }
    }
}

@Composable
private fun DurationSection(elapsedText: String) {
    Column(
        modifier = Modifier.padding(top = 8.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "시간",
            style = RunpamineTypography.Caption1.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
            color = Color.Black,
        )
        Text(
            text = elapsedText,
            style = RunpamineTypography.Header1,
            color = Color.Black,
        )
    }
}

@Composable
private fun RunningMetricCard(
    title: String,
    value: String,
    suffix: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .height(110.dp)
                .shadow(
                    elevation = 15.dp,
                    shape = RoundedCornerShape(12.dp),
                    clip = false,
                ).clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = title,
            style = RunpamineTypography.Body2,
            color = RunpamineColors.TextPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = value,
                style =
                    RunpamineTypography.Header1.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                color = Color.Black,
            )
            Text(
                text = suffix,
                modifier = Modifier.padding(bottom = 3.dp),
                style = RunpamineTypography.Caption1.copy(fontWeight = FontWeight.Medium),
                color = RunningColors.Unit,
            )
        }
    }
}

@Composable
private fun RunningControls(
    isPaused: Boolean,
    enabled: Boolean,
    onStop: () -> Unit,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(70.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isPaused) {
            RunningControlButton(
                type = RunningControlType.Stop,
                accessibilityLabel = "러닝 중지",
                backgroundColor = Color.Black,
                foregroundColor = Color.White,
                enabled = enabled,
                onClick = onStop,
            )
            Spacer(Modifier.size(32.dp))
        }
        RunningControlButton(
            type = if (isPaused) RunningControlType.Play else RunningControlType.Pause,
            accessibilityLabel = if (isPaused) "러닝 재개" else "러닝 일시 정지",
            backgroundColor = if (isPaused) RunpamineColors.Success else Color.Black,
            foregroundColor = if (isPaused) Color.Black else Color.White,
            enabled = enabled,
            onClick = onTogglePause,
        )
    }
}

@Composable
private fun RunningControlButton(
    type: RunningControlType,
    accessibilityLabel: String,
    backgroundColor: Color,
    foregroundColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(backgroundColor.copy(alpha = if (enabled) 1f else 0.4f))
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .semantics { contentDescription = accessibilityLabel },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(32.dp)) {
            when (type) {
                RunningControlType.Pause -> {
                    val width = 8.dp.toPx()
                    drawRoundRect(
                        color = foregroundColor,
                        topLeft = Offset(3.dp.toPx(), 1.dp.toPx()),
                        size = Size(width, 30.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx()),
                    )
                    drawRoundRect(
                        color = foregroundColor,
                        topLeft = Offset(21.dp.toPx(), 1.dp.toPx()),
                        size = Size(width, 30.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx()),
                    )
                }
                RunningControlType.Play -> {
                    val path =
                        Path().apply {
                            moveTo(8.dp.toPx(), 3.dp.toPx())
                            lineTo(28.dp.toPx(), 16.dp.toPx())
                            lineTo(8.dp.toPx(), 29.dp.toPx())
                            close()
                        }
                    drawPath(path, foregroundColor)
                }
                RunningControlType.Stop -> {
                    drawRoundRect(
                        color = foregroundColor,
                        topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                        size = Size(24.dp.toPx(), 24.dp.toPx()),
                        cornerRadius = CornerRadius(3.dp.toPx()),
                    )
                }
            }
        }
    }
}

private enum class RunningControlType {
    Pause,
    Play,
    Stop,
}

private object RunningColors {
    val Unit = Color(0xFF8B8B8B)
}

@Preview
@Composable
private fun RunningScreenPreview() {
    RunpamineTheme {
        RunningScreen(
            state =
                RunningUiState(
                    phase = RunningPhase.Running,
                    elapsedText = "00:31:14",
                    distanceKm = 5.24,
                    paceText = "5'58\"",
                    calories = 304,
                ),
            onPause = {},
            onResume = {},
            onStop = {},
        )
    }
}

@Preview
@Composable
private fun PausedRunningScreenPreview() {
    RunpamineTheme {
        RunningScreen(
            state =
                RunningUiState(
                    phase = RunningPhase.Paused,
                    elapsedText = "00:31:14",
                    distanceKm = 5.24,
                    paceText = "5'58\"",
                    calories = 304,
                ),
            onPause = {},
            onResume = {},
            onStop = {},
        )
    }
}
