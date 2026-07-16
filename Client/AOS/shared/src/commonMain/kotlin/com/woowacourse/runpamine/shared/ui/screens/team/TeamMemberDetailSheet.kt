package com.woowacourse.runpamine.shared.ui.screens.team

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.woowacourse.runpamine.shared.ui.model.RunpamineSamples
import com.woowacourse.runpamine.shared.ui.model.TeamMemberUi
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTypography
import kotlin.math.roundToInt

@Composable
fun TeamMemberDetailSheet(
    member: TeamMemberUi,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.34f)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            TeamMemberDetailSheetContent(
                member = member,
                onDismiss = onDismiss,
                modifier = modifier,
            )
        }
    }
}

@Composable
fun TeamMemberDetailSheetContent(
    member: TeamMemberUi,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(
                    brush =
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF3B7BFA), RunpamineColors.Primary),
                        ),
                ).padding(horizontal = 28.dp)
                .padding(top = 28.dp, bottom = 48.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = member.nickname,
                style =
                    RunpamineTypography.Header2.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier.size(44.dp).clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "×",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Row(
            modifier = Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalendarOutlineIcon(
                color = Color.White.copy(alpha = 0.72f),
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = "${member.joinedAtText.ifBlank { "-" }} 합류",
                style = RunpamineTypography.Body1.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            TeamMemberDetailMetric(
                label = "총 거리 (km)",
                value = member.seasonDistanceKm.toSeasonDistanceText(),
                modifier = Modifier.weight(1f),
            )
            TeamMemberDetailMetric(
                label = "총 러닝 횟수",
                value = member.totalRunCount.toString(),
                modifier = Modifier.weight(1f),
            )
            TeamMemberDetailMetric(
                label = "평균 페이스",
                value = member.averagePaceText.removeSuffix("/km"),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TeamMemberDetailMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = RunpamineTypography.Body2.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
            color = Color.White.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style =
                RunpamineTypography.Header1.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                ),
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CalendarOutlineIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.3.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, size.height * 0.16f),
            size = Size(size.width, size.height * 0.80f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = strokeWidth),
        )
        drawLine(
            color = color,
            start = Offset(0f, size.height * 0.43f),
            end = Offset(size.width, size.height * 0.43f),
            strokeWidth = strokeWidth,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.26f, 0f),
            end = Offset(size.width * 0.26f, size.height * 0.29f),
            strokeWidth = strokeWidth,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.74f, 0f),
            end = Offset(size.width * 0.74f, size.height * 0.29f),
            strokeWidth = strokeWidth,
        )
    }
}

private fun Double.toSeasonDistanceText(): String {
    val scaled = (coerceAtLeast(0.0) * 100).roundToInt()
    val whole = scaled / 100
    val fraction = (scaled % 100).toString().padStart(2, '0')
    return "$whole.$fraction"
}

@Preview
@Composable
private fun TeamMemberDetailSheetPreview() {
    RunpamineTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.34f)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            TeamMemberDetailSheetContent(
                member = RunpamineSamples.members.first(),
                onDismiss = {},
            )
        }
    }
}
