package com.woowacourse.runpamine.shared.ui.screens.team

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.shared.generated.resources.Res
import com.woowacourse.runpamine.shared.generated.resources.bk
import com.woowacourse.runpamine.shared.generated.resources.encho
import com.woowacourse.runpamine.shared.generated.resources.ic_team_plus
import com.woowacourse.runpamine.shared.generated.resources.icon_key_svg
import com.woowacourse.runpamine.shared.generated.resources.stamp
import com.woowacourse.runpamine.shared.ui.components.RunpamineConfirmationDialog
import com.woowacourse.runpamine.shared.ui.model.CharacterMotion
import com.woowacourse.runpamine.shared.ui.model.RunpamineSamples
import com.woowacourse.runpamine.shared.ui.model.TeamDashboardUi
import com.woowacourse.runpamine.shared.ui.model.TeamMemberUi
import com.woowacourse.runpamine.shared.ui.model.TeamSummaryUi
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTypography
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

@Composable
fun TeamScreen(
    team: TeamSummaryUi?,
    dashboard: TeamDashboardUi,
    onCreateTeam: () -> Unit,
    onJoinTeam: () -> Unit,
    onInvite: () -> Unit,
    onLeaveTeam: () -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onSelectMember: (TeamMemberUi) -> Unit,
    modifier: Modifier = Modifier,
    isLeavingTeam: Boolean = false,
    leaveTeamErrorMessage: String? = null,
) {
    var showTeamMenu by rememberSaveable { mutableStateOf(false) }
    var showLeaveConfirmation by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(team) {
        if (team == null) showLeaveConfirmation = false
    }

    when {
        team == null -> {
            TeamNoTeamContent(
                onCreateTeam = onCreateTeam,
                onJoinTeam = onJoinTeam,
                modifier = modifier.fillMaxSize(),
            )
        }

        dashboard.isLoading -> {
            TeamDashboardSkeleton(modifier = modifier.fillMaxSize())
        }

        else -> {
            TeamDashboardContent(
                team = team,
                dashboard = dashboard,
                showTeamMenu = showTeamMenu,
                onDismissTeamMenu = { showTeamMenu = false },
                onToggleTeamMenu = { showTeamMenu = !showTeamMenu },
                onInvite = {
                    showTeamMenu = false
                    onInvite()
                },
                onLeaveTeamRequest = {
                    showTeamMenu = false
                    showLeaveConfirmation = true
                },
                onPreviousDate = onPreviousDate,
                onNextDate = onNextDate,
                onSelectMember = onSelectMember,
                modifier = modifier.fillMaxSize(),
            )
        }
    }

    if (showLeaveConfirmation) {
        RunpamineConfirmationDialog(
            title = "팀 탈퇴",
            message = leaveTeamErrorMessage ?: "정말 팀을 탈퇴하시겠습니까?",
            dismissText = "취소",
            confirmText = if (isLeavingTeam) "탈퇴 중..." else "팀 탈퇴",
            onDismiss = {
                if (!isLeavingTeam) showLeaveConfirmation = false
            },
            onConfirm = {
                if (!isLeavingTeam) onLeaveTeam()
            },
            isDanger = true,
        )
    }
}

@Composable
fun TeamNoTeamContent(
    onCreateTeam: () -> Unit,
    onJoinTeam: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "아직 참여한 팀이 없어요",
                style = RunpamineTypography.Header2.copy(fontSize = 25.sp, fontWeight = FontWeight.Black),
                color = RunpamineColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "팀을 만들거나 초대 코드로\n팀에 참가해보세요.",
                style = RunpamineTypography.Body1.copy(fontWeight = FontWeight.SemiBold),
                color = RunpamineColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
            )
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
                    .padding(top = 34.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TeamEmptyActionCard(
                title = "팀 생성하기",
                description = "새 팀을 만들고 팀원을 초대해요",
                onClick = onCreateTeam,
                filled = true,
            )
            TeamEmptyActionCard(
                title = "팀 참가하기",
                description = "초대 코드로 기존 팀에 들어가요",
                onClick = onJoinTeam,
                filled = false,
            )
        }
    }
}

@Composable
private fun TeamEmptyActionCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    filled: Boolean,
    modifier: Modifier = Modifier,
) {
    val foreground = if (filled) Color.White else RunpamineColors.Primary
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(94.dp)
                .shadow(if (filled) 0.dp else 1.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(if (filled) RunpamineColors.Primary else RunpamineColors.Surface)
                .then(
                    if (filled) {
                        Modifier
                    } else {
                        Modifier.border(1.5.dp, RunpamineColors.Primary, RoundedCornerShape(22.dp))
                    },
                ).clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        Image(
            painter =
                painterResource(
                    if (filled) {
                        Res.drawable.ic_team_plus
                    } else {
                        Res.drawable.icon_key_svg
                    },
                ),
            contentDescription = null,
            modifier = Modifier.size(if (filled) 32.dp else 26.dp),
            colorFilter = if (filled) null else ColorFilter.tint(RunpamineColors.Primary),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = RunpamineTypography.Body1.copy(fontSize = 18.sp, fontWeight = FontWeight.Black),
                color = foreground,
            )
            Text(
                text = description,
                style = RunpamineTypography.Caption1.copy(fontWeight = FontWeight.SemiBold),
                color = foreground.copy(alpha = if (filled) 0.82f else 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = "›",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = foreground,
        )
    }
}

@Composable
fun TeamDashboardContent(
    team: TeamSummaryUi,
    dashboard: TeamDashboardUi,
    showTeamMenu: Boolean,
    onDismissTeamMenu: () -> Unit,
    onToggleTeamMenu: () -> Unit,
    onInvite: () -> Unit,
    onLeaveTeamRequest: () -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onSelectMember: (TeamMemberUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(Color.White)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 98.dp),
        ) {
            item {
                TeamDashboardHeader(
                    teamName = team.name,
                    onToggleMenu = onToggleTeamMenu,
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, top = 32.dp, end = 24.dp),
                )
            }
            item {
                TeamDateSelector(
                    dateText = dashboard.dateText,
                    canMoveNextDate = dashboard.canMoveNextDate,
                    isLoading = dashboard.isLoading,
                    onPreviousDate = onPreviousDate,
                    onNextDate = onNextDate,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TeamSummaryMetricCard(
                        value = dashboard.totalDistanceKm.toDistanceText(),
                        label = "팀 총 거리",
                        modifier = Modifier.weight(1f),
                    )
                    TeamSummaryMetricCard(
                        value = "${dashboard.completedMemberCount} / ${dashboard.totalMemberCount}",
                        label = "완료 / 전체",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (dashboard.members.isEmpty()) {
                item {
                    Text(
                        text = "아직 표시할 팀원 기록이 없어요.",
                        style = RunpamineTypography.Body1,
                        color = RunpamineColors.TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 48.dp),
                    )
                }
            } else {
                items(
                    items = dashboard.members,
                    key = { member -> member.id },
                ) { member ->
                    TeamMemberRunCard(
                        member = member,
                        onClick = { onSelectMember(member) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp).padding(top = 28.dp),
                    )
                }
            }
        }

        if (showTeamMenu) {
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismissTeamMenu,
                        ),
            )
            TeamMenu(
                onInvite = onInvite,
                onLeaveTeam = onLeaveTeamRequest,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 82.dp, end = 24.dp),
            )
        }
    }
}

@Composable
private fun TeamDashboardHeader(
    teamName: String,
    onToggleMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = teamName,
            style = RunpamineTypography.Header1.copy(fontSize = 30.sp, fontWeight = FontWeight.Bold),
            color = RunpamineColors.Primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onToggleMenu),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "•••",
                color = RunpamineColors.Primary,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun TeamDateSelector(
    dateText: String,
    canMoveNextDate: Boolean,
    isLoading: Boolean,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TeamDateButton(
            glyph = "‹",
            enabled = !isLoading,
            onClick = onPreviousDate,
        )
        Text(
            text = dateText,
            style = RunpamineTypography.Body1.copy(fontSize = 18.sp),
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(210.dp).alpha(if (isLoading) 0.55f else 1f),
        )
        TeamDateButton(
            glyph = "›",
            enabled = canMoveNextDate && !isLoading,
            onClick = onNextDate,
        )
    }
}

@Composable
private fun TeamDateButton(
    glyph: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = glyph,
            color = if (enabled) Color.Black else Color.Gray.copy(alpha = 0.3f),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun TeamSummaryMetricCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .height(90.dp)
                .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = Color.Black.copy(alpha = 0.1f))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = value,
            style = RunpamineTypography.Header2.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
            color = RunpamineColors.Primary,
            maxLines = 1,
        )
        Text(
            text = label,
            style = RunpamineTypography.Body2,
            color = RunpamineColors.TextSecondary,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun TeamMenu(
    onInvite: () -> Unit,
    onLeaveTeam: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .width(98.dp)
                .shadow(10.dp, RoundedCornerShape(14.dp), ambientColor = Color.Black.copy(alpha = 0.12f))
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFB8B8B8), RoundedCornerShape(14.dp))
                .padding(vertical = 6.dp),
    ) {
        TeamMenuButton(text = "팀원 초대", color = Color.Black, onClick = onInvite)
        Box(
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(78.dp)
                    .height(1.dp)
                    .background(Color(0xFFC7C7C7)),
        )
        TeamMenuButton(text = "팀 탈퇴", color = RunpamineColors.Danger, onClick = onLeaveTeam)
    }
}

@Composable
private fun TeamMenuButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(42.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = RunpamineTypography.Body1.copy(fontSize = 18.sp),
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
private fun TeamMemberRunCard(
    member: TeamMemberUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .shadow(9.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.22f))
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(start = 26.dp, top = 26.dp, end = 25.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = member.nickname,
                style = RunpamineTypography.Header2.copy(fontWeight = FontWeight.Bold),
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (member.isCurrentUser) {
                Box(
                    modifier =
                        Modifier
                            .size(width = 19.dp, height = 20.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(RunpamineColors.Primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "나",
                        style = RunpamineTypography.Caption1.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                        color = Color.White,
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TeamMemberCharacter(member = member, modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TeamMemberMetricRow(label = "거리", value = member.distanceKm.toDistanceText())
                TeamMemberMetricRow(label = "시간", value = member.durationText.ifBlank { "--:--" })
                TeamMemberMetricRow(label = "페이스", value = member.paceText.toPaceText())
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier.size(width = 70.dp, height = 80.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (member.isCompleted) {
                    Image(
                        painter = painterResource(Res.drawable.stamp),
                        contentDescription = "러닝 완료",
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamMemberCharacter(
    member: TeamMemberUi,
    modifier: Modifier = Modifier,
) {
    Image(
        painter =
            painterResource(
                when (member.motion) {
                    CharacterMotion.Hamburger,
                    CharacterMotion.Idle,
                    -> Res.drawable.bk

                    CharacterMotion.Running,
                    CharacterMotion.Reverse,
                    CharacterMotion.Cheetah,
                    -> Res.drawable.encho
                },
            ),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun TeamMemberMetricRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = RunpamineTypography.Body2,
            color = Color(0xFF94A3B8),
            modifier = Modifier.width(48.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = RunpamineTypography.Body2.copy(fontWeight = FontWeight.Bold),
            color = RunpamineColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TeamDashboardSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.background(Color.White),
        contentPadding = PaddingValues(start = 24.dp, top = 32.dp, end = 24.dp, bottom = 98.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBlock(modifier = Modifier.width(210.dp).height(34.dp), radius = 8)
                Spacer(modifier = Modifier.weight(1f))
                SkeletonBlock(modifier = Modifier.size(48.dp), radius = 12)
            }
        }
        item {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SkeletonBlock(modifier = Modifier.width(190.dp).height(22.dp), radius = 8)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBlock(modifier = Modifier.weight(1f).height(90.dp), radius = 12)
                SkeletonBlock(modifier = Modifier.weight(1f).height(90.dp), radius = 12)
            }
        }
        items(3) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth().height(190.dp), radius = 22)
        }
    }
}

@Composable
private fun SkeletonBlock(
    modifier: Modifier,
    radius: Int,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(radius.dp))
                .background(Color(0xFFEBF0F7)),
    )
}

private fun Double.toDistanceText(): String {
    val scaled = (coerceAtLeast(0.0) * 100).roundToInt()
    val whole = scaled / 100
    val fraction = (scaled % 100).toString().padStart(2, '0')
    return "$whole.$fraction km"
}

private fun String.toPaceText(): String = if (endsWith("/km")) this else "$this/km"

@Preview
@Composable
private fun TeamNoTeamPreview() {
    RunpamineTheme {
        TeamNoTeamContent(
            onCreateTeam = {},
            onJoinTeam = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview
@Composable
private fun TeamDashboardPreview() {
    RunpamineTheme {
        TeamScreen(
            team = RunpamineSamples.team,
            dashboard =
                TeamDashboardUi(
                    totalDistanceKm = 8.4,
                    completedMemberCount = 2,
                    totalMemberCount = 3,
                    members = RunpamineSamples.members,
                ),
            onCreateTeam = {},
            onJoinTeam = {},
            onInvite = {},
            onLeaveTeam = {},
            onPreviousDate = {},
            onNextDate = {},
            onSelectMember = {},
        )
    }
}
