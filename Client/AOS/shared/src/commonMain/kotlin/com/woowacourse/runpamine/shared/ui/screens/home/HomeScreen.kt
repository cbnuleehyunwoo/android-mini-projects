package com.woowacourse.runpamine.shared.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.shared.generated.resources.Res
import com.woowacourse.runpamine.shared.generated.resources.app_logo_face
import com.woowacourse.runpamine.shared.generated.resources.ic_gps_error
import com.woowacourse.runpamine.shared.generated.resources.icon_locate_fix
import com.woowacourse.runpamine.shared.generated.resources.icon_team
import com.woowacourse.runpamine.shared.ui.components.RunpamineConfirmationDialog
import com.woowacourse.runpamine.shared.ui.model.TeamSummaryUi
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTypography
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeScreen(
    nickname: String,
    team: TeamSummaryUi?,
    onCreateTeam: () -> Unit,
    onJoinTeam: () -> Unit,
    onOpenTeam: () -> Unit,
    onOpenMyPage: () -> Unit,
    onStartRunning: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    modifier: Modifier = Modifier,
    locationPermissionGranted: Boolean = false,
    permissionButtonTitle: String = "위치 권한 허용하기",
    isStartButtonVisible: Boolean = locationPermissionGranted,
    mapContent: @Composable BoxScope.() -> Unit = {
        HomeMapPlaceholder(modifier = Modifier.fillMaxSize())
    },
) {
    var showStartConfirmation by rememberSaveable { mutableStateOf(false) }

    HomeScreenContent(
        nickname = nickname,
        team = team,
        locationPermissionGranted = locationPermissionGranted,
        permissionButtonTitle = permissionButtonTitle,
        isStartButtonVisible = isStartButtonVisible,
        onCreateTeam = onCreateTeam,
        onJoinTeam = onJoinTeam,
        onOpenTeam = onOpenTeam,
        onOpenMyPage = onOpenMyPage,
        onStartClick = { showStartConfirmation = true },
        onRequestLocationPermission = onRequestLocationPermission,
        modifier = modifier,
        mapContent = mapContent,
    )

    if (showStartConfirmation) {
        RunpamineConfirmationDialog(
            title = "러닝 시작",
            message = "러닝을 시작하시겠습니까?",
            dismissText = "취소",
            confirmText = "시작",
            onDismiss = { showStartConfirmation = false },
            onConfirm = {
                showStartConfirmation = false
                onStartRunning()
            },
        )
    }
}

@Composable
fun HomeScreenContent(
    nickname: String,
    team: TeamSummaryUi?,
    locationPermissionGranted: Boolean,
    permissionButtonTitle: String,
    isStartButtonVisible: Boolean,
    onCreateTeam: () -> Unit,
    onJoinTeam: () -> Unit,
    onOpenTeam: () -> Unit,
    onOpenMyPage: () -> Unit,
    onStartClick: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    modifier: Modifier = Modifier,
    mapContent: @Composable BoxScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        HomeHeader(
            nickname = nickname,
            onOpenMyPage = onOpenMyPage,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(end = HOME_CONTENT_PADDING.dp),
        )
        HomeTeamStatusCard(
            team = team,
            onCreateTeam = onCreateTeam,
            onJoinTeam = onJoinTeam,
            onOpenTeam = onOpenTeam,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HOME_CONTENT_PADDING.dp),
        )
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = HOME_CONTENT_PADDING.dp)
                        .clip(RoundedCornerShape(18.dp)),
            ) {
                if (locationPermissionGranted) {
                    mapContent()
                } else {
                    HomeLocationPermissionContent(
                        buttonTitle = permissionButtonTitle,
                        onRequestPermission = onRequestLocationPermission,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            if (isStartButtonVisible) {
                HomeStartButton(
                    onClick = onStartClick,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    nickname: String,
    onOpenMyPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(90.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(78.dp).fillMaxHeight()) {
            Image(
                painter = painterResource(Res.drawable.app_logo_face),
                contentDescription = null,
                modifier = Modifier.size(90.dp).align(Alignment.CenterStart),
                contentScale = ContentScale.Fit,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = "안녕하세요",
                style = RunpamineTypography.Caption1.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFF737B8F),
            )
            Text(
                text = "$nickname 님",
                style = RunpamineTypography.Body1.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                color = RunpamineColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier =
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onOpenMyPage),
            contentAlignment = Alignment.Center,
        ) {
            ProfileOutlineIcon(modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun ProfileOutlineIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.8.dp.toPx()
        drawCircle(
            color = RunpamineColors.TextPrimary,
            radius = size.minDimension * 0.19f,
            center = center.copy(y = size.height * 0.30f),
            style = Stroke(width = strokeWidth),
        )
        drawArc(
            color = RunpamineColors.TextPrimary,
            startAngle = 202f,
            sweepAngle = 136f,
            useCenter = false,
            topLeft = Offset(size.width * 0.12f, size.height * 0.43f),
            size = Size(size.width * 0.76f, size.height * 0.62f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun HomeTeamStatusCard(
    team: TeamSummaryUi?,
    onCreateTeam: () -> Unit,
    onJoinTeam: () -> Unit,
    onOpenTeam: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(24.dp))
                .background(RunpamineColors.Primary)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (team == null) {
                Text(
                    text = "참여한 팀이 없어요!",
                    style = RunpamineTypography.Body1.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Text(
                    text = "팀에 참여하면 함께 달릴 수 있어요",
                    style = RunpamineTypography.Body2.copy(fontSize = 13.sp),
                    color = Color.White.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Row(
                    modifier = Modifier.padding(top = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HomeTeamActionButton(
                        text = "팀 생성하기",
                        onClick = onCreateTeam,
                        filled = true,
                    )
                    HomeTeamActionButton(
                        text = "팀 참가하기",
                        onClick = onJoinTeam,
                        filled = false,
                    )
                }
            } else {
                Text(
                    text = team.name,
                    style = RunpamineTypography.Body1.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "오늘 달린 인원 ${team.completedMemberCount} / ${team.totalMemberCount}",
                    style = RunpamineTypography.Body2.copy(fontSize = 13.sp),
                    color = Color.White.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )
                HomeTeamActionButton(
                    text = "팀 정보보기",
                    onClick = onOpenTeam,
                    filled = false,
                    modifier = Modifier.padding(top = 7.dp),
                )
            }
        }
        Image(
            painter = painterResource(Res.drawable.icon_team),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            colorFilter = ColorFilter.tint(Color.White),
        )
    }
}

@Composable
private fun HomeTeamActionButton(
    text: String,
    onClick: () -> Unit,
    filled: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .width(88.dp)
                .height(30.dp)
                .clip(CircleShape)
                .then(
                    if (filled) {
                        Modifier.background(Color.White)
                    } else {
                        Modifier.border(1.5.dp, Color.White, CircleShape)
                    },
                ).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = RunpamineTypography.Caption1.copy(fontWeight = FontWeight.Bold),
            color = if (filled) RunpamineColors.Primary else Color.White,
            maxLines = 1,
        )
    }
}

@Composable
fun HomeLocationPermissionContent(
    buttonTitle: String,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(RunpamineColors.Primary.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_gps_error),
                contentDescription = null,
                modifier = Modifier.size(42.dp),
            )
        }
        Text(
            text = "위치 권한이 필요해요",
            style = RunpamineTypography.Body1.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold),
            color = RunpamineColors.TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 22.dp),
        )
        Text(
            text = "러닝 거리 측정과 경로 기록을 위해\n위치 접근 권한을 허용해 주세요.",
            style = RunpamineTypography.Body2.copy(fontWeight = FontWeight.Normal),
            color = RunpamineColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(top = 12.dp),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 38.dp)
                    .padding(top = 28.dp)
                    .height(64.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = RunpamineColors.Primary.copy(alpha = 0.2f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(RunpamineColors.Primary)
                    .clickable(role = Role.Button, onClick = onRequestPermission),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = buttonTitle,
                style = RunpamineTypography.Button,
                color = Color.White,
            )
        }
    }
}

@Composable
fun HomeMapPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color(0xFFF2F4F7)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(Res.drawable.icon_locate_fix),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = "지도 영역",
                style = RunpamineTypography.Body2,
                color = RunpamineColors.TextSecondary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun HomeStartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(RunpamineColors.Primary)
                .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "시작",
            style = RunpamineTypography.Header2.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
    }
}

@Preview
@Composable
private fun HomeWithoutTeamPreview() {
    RunpamineTheme {
        HomeScreen(
            nickname = "러너",
            team = null,
            onCreateTeam = {},
            onJoinTeam = {},
            onOpenTeam = {},
            onOpenMyPage = {},
            onStartRunning = {},
            onRequestLocationPermission = {},
        )
    }
}

@Preview
@Composable
private fun HomeWithTeamPreview() {
    RunpamineTheme {
        HomeScreen(
            nickname = "커비",
            team = TeamSummaryUi("team-1", "런앤런", "A1B2C3", 2, 4),
            onCreateTeam = {},
            onJoinTeam = {},
            onOpenTeam = {},
            onOpenMyPage = {},
            onStartRunning = {},
            onRequestLocationPermission = {},
            locationPermissionGranted = true,
        )
    }
}

private const val HOME_CONTENT_PADDING = 20
