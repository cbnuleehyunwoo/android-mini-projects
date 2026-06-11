package com.woowacourse.runpamine.presentation.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woowacourse.runpamine.di.runpamineContainer
import com.woowacourse.runpamine.domain.profile.HomeState
import com.woowacourse.runpamine.domain.profile.TeamSummary
import com.woowacourse.runpamine.domain.profile.UserProfile
import com.woowacourse.runpamine.domain.ranking.MyRankingSummary
import com.woowacourse.runpamine.domain.ranking.RankingMetric
import com.woowacourse.runpamine.domain.ranking.RankingSeason
import com.woowacourse.runpamine.domain.ranking.TeamRanking
import com.woowacourse.runpamine.domain.ranking.UserRanking
import com.woowacourse.runpamine.presentation.ranking.components.RankBadge
import com.woowacourse.runpamine.presentation.ranking.components.RankingStateMessage
import com.woowacourse.runpamine.presentation.ranking.model.RankingScope
import com.woowacourse.runpamine.presentation.ranking.model.RankingUiState
import com.woowacourse.runpamine.presentation.ranking.viewmodel.RankingViewModel
import com.woowacourse.runpamine.ui.theme.Blue10
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.Gray40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme
import java.util.Locale

@Composable
fun RankingScreen(modifier: Modifier = Modifier) {
    val container = LocalContext.current.runpamineContainer
    val viewModel: RankingViewModel =
        viewModel(
            factory =
                RankingViewModel.Factory(
                    rankingRepository = container.rankingRepository,
                    profileRepository = container.profileRepository,
                ),
        )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RankingContent(
        uiState = uiState,
        onScopeSelect = viewModel::selectScope,
        onMetricSelect = viewModel::selectMetric,
        onRetryClick = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
private fun RankingContent(
    uiState: RankingUiState,
    onScopeSelect: (RankingScope) -> Unit,
    onMetricSelect: (RankingMetric) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rankingItems = uiState.toRankingItems()
    val myRanking = uiState.toMyRankingItem(rankingItems)

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        RankingScopeTabs(
            selectedScope = uiState.selectedScope,
            onScopeSelect = onScopeSelect,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(26.dp))
        RankingMetricTabs(
            selectedScope = uiState.selectedScope,
            selectedMetric = uiState.selectedMetric,
            onMetricSelect = onMetricSelect,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
        )
        Spacer(modifier = Modifier.height(28.dp))
        if (uiState.isLoading) {
            RankingSkeletonBody(
                selectedScope = uiState.selectedScope,
                selectedMetric = uiState.selectedMetric,
                modifier = Modifier.weight(1f),
            )
        } else {
            MyRankingCard(
                item = myRanking,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(
                color = DividerDefaults.color.copy(alpha = 0.7f),
                thickness = 1.dp,
            )
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                RankingListCard(
                    selectedScope = uiState.selectedScope,
                    selectedMetric = uiState.selectedMetric,
                    items = rankingItems,
                    modifier = Modifier.fillMaxWidth(),
                )
                RankingStateMessage(
                    isLoading = false,
                    errorMessage = uiState.errorMessage,
                    isEmpty = rankingItems.isEmpty(),
                    onRetryClick = onRetryClick,
                )
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
fun RankingScopeTabs(
    selectedScope: RankingScope,
    onScopeSelect: (RankingScope) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(72.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RankingScope.entries.forEach { scope ->
                RankingScopeTab(
                    scope = scope,
                    selected = scope == selectedScope,
                    onClick = { onScopeSelect(scope) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun RankingScopeTab(
    scope: RankingScope,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(72.dp)
                .clickable(
                    role = Role.Tab,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = scope.label,
            color = if (selected) Blue40 else Gray40,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 18.sp),
            textAlign = TextAlign.Center,
        )
        if (selected) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Blue40),
            )
        }
    }
}

@Composable
private fun RankingRow(
    item: RankingItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (item.isMine) Blue10 else Color(0xFFF8F8F8))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RankBadge(
            rank = item.rank,
            selected = item.isMine,
        )
        Text(
            text = item.name,
            color = if (item.isMine) Blue40 else Color(0xFF384152),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = item.valueText,
            color = if (item.isMine) Blue40 else Color(0xFFA2ACBA),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun RankingMetricTabs(
    selectedScope: RankingScope,
    selectedMetric: RankingMetric,
    onMetricSelect: (RankingMetric) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RankingMetric.entries.forEach { metric ->
            val selected = metric == selectedMetric
            val tabShape = RoundedCornerShape(32.dp)
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(tabShape)
                        .background(if (selected) Blue10 else Color(0xFFF2F3F6))
                        .border(if (selected) 2.dp else 0.dp, if (selected) Blue40 else Color.Transparent, tabShape)
                        .clickable(role = Role.Tab, onClick = { onMetricSelect(metric) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = metric.label(selectedScope),
                    color = if (selected) Blue40 else Color(0xFF6B7280),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun MyRankingCard(
    item: RankingItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Blue10)
                .padding(horizontal = 16.dp, vertical = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RankBadge(
            rank = item.rank,
            selected = true,
        )
        Text(
            text = item.name,
            color = Blue40,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = item.highlightText,
            color = Blue40,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun RankingListCard(
    selectedScope: RankingScope,
    selectedMetric: RankingMetric,
    items: List<RankingItem>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (selectedScope == RankingScope.TEAM) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "전체 팀 순위",
                    color = Color(0xFF111827),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = selectedMetric.teamStandardLabel,
                    color = Color(0xFFA6AFBD),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        items.forEach { item ->
            RankingRow(item = item)
        }
    }
}

@Composable
private fun RankingSkeletonBody(
    selectedScope: RankingScope,
    selectedMetric: RankingMetric,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        MyRankingSkeletonCard(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(
            color = DividerDefaults.color.copy(alpha = 0.7f),
            thickness = 1.dp,
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            RankingListSkeletonCard(
                selectedScope = selectedScope,
                selectedMetric = selectedMetric,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun MyRankingSkeletonCard(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Blue10)
                .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        RankingSkeletonBox(
            modifier =
                Modifier
                    .height(46.dp)
                    .weight(0.18f),
            color = RankingSkeletonDarkColor,
            shape = RoundedCornerShape(14.dp),
        )
        RankingSkeletonBox(
            modifier =
                Modifier
                    .height(24.dp)
                    .weight(0.42f),
        )
        Spacer(modifier = Modifier.weight(0.12f))
        RankingSkeletonBox(
            modifier =
                Modifier
                    .height(24.dp)
                    .weight(0.56f),
        )
    }
}

@Composable
private fun RankingListSkeletonCard(
    selectedScope: RankingScope,
    selectedMetric: RankingMetric,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selectedScope.skeletonTitle,
                color = Color(0xFF111827),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = selectedMetric.standardLabel(selectedScope),
                color = Color(0xFFA6AFBD),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        repeat(RANKING_SKELETON_ROW_COUNT) { index ->
            RankingRowSkeleton(selected = index == 1)
        }
    }
}

@Composable
private fun RankingRowSkeleton(selected: Boolean) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) Blue10 else Color(0xFFF8F8F8))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RankingSkeletonBox(
            modifier =
                Modifier
                    .height(34.dp)
                    .weight(0.16f),
            color = RankingSkeletonDarkColor,
            shape = RoundedCornerShape(10.dp),
        )
        RankingSkeletonBox(
            modifier =
                Modifier
                    .height(22.dp)
                    .weight(0.5f),
        )
        Spacer(modifier = Modifier.weight(0.35f))
        RankingSkeletonBox(
            modifier =
                Modifier
                    .height(22.dp)
                    .weight(0.55f),
        )
    }
}

@Composable
private fun RankingSkeletonBox(
    modifier: Modifier = Modifier,
    color: Color = RankingSkeletonColor,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
) {
    Box(
        modifier =
            modifier
                .clip(shape)
                .background(color),
    )
}

private fun RankingUiState.toRankingItems(): List<RankingItem> =
    when (selectedScope) {
        RankingScope.TEAM ->
            teamRankings.map { ranking ->
                ranking.toRankingItem(
                    metric = selectedMetric,
                    isMine = ranking.teamId == homeState?.team?.id,
                )
            }

        RankingScope.PERSONAL ->
            userRankings.map { ranking ->
                ranking.toRankingItem(
                    metric = selectedMetric,
                    isMine = ranking.rank == myRankingSummary?.rankFor(selectedMetric),
                )
            }
    }

private fun RankingUiState.toMyRankingItem(items: List<RankingItem>): RankingItem {
    val topPercentText = myRankingSummary?.topPercentTextFor(selectedMetric)
    items.firstOrNull { it.isMine }?.let {
        return it.copy(percentileText = topPercentText)
    }

    return when (selectedScope) {
        RankingScope.TEAM ->
            RankingItem(
                rank = null,
                name = homeState?.team?.name ?: "팀 없음",
                valueText = "랭킹 집계 전",
                isMine = true,
            )

        RankingScope.PERSONAL ->
            RankingItem(
                rank = myRankingSummary?.rankFor(selectedMetric),
                name = homeState?.profile?.nickname ?: "내 기록",
                valueText = myRankingSummary?.valueTextFor(selectedMetric) ?: "-",
                isMine = true,
                percentileText = topPercentText,
            )
    }
}

private fun TeamRanking.toRankingItem(
    metric: RankingMetric,
    isMine: Boolean,
): RankingItem =
    RankingItem(
        rank = rank,
        name = teamName,
        valueText = valueTextFor(metric),
        isMine = isMine,
    )

private fun TeamRanking.valueTextFor(metric: RankingMetric): String =
    when (metric) {
        RankingMetric.DISTANCE -> distanceMeters.toKilometerText()
        RankingMetric.PACE -> averagePaceSecondsPerKm.toPaceText()
        RankingMetric.CONSISTENCY -> averageActiveDays.toActiveDaysText()
    }

private fun UserRanking.toRankingItem(
    metric: RankingMetric,
    isMine: Boolean,
): RankingItem =
    RankingItem(
        rank = rank,
        name = nickname,
        valueText = valueTextFor(metric),
        isMine = isMine,
    )

private fun UserRanking.valueTextFor(metric: RankingMetric): String =
    when (metric) {
        RankingMetric.DISTANCE -> distanceMeters.toKilometerText()
        RankingMetric.PACE -> averagePaceSecondsPerKm.toPaceText()
        RankingMetric.CONSISTENCY -> activeDays.toActiveDaysText()
    }

private fun MyRankingSummary.valueTextFor(metric: RankingMetric): String =
    when (metric) {
        RankingMetric.DISTANCE -> distanceMeters.toKilometerText()
        RankingMetric.PACE -> averagePaceSecondsPerKm.toPaceText()
        RankingMetric.CONSISTENCY -> activeDays.toActiveDaysText()
    }

private fun MyRankingSummary.rankFor(metric: RankingMetric): Int? =
    when (metric) {
        RankingMetric.DISTANCE -> distanceRank
        RankingMetric.PACE -> paceRank
        RankingMetric.CONSISTENCY -> consistencyRank
    }

private fun MyRankingSummary.topPercentTextFor(metric: RankingMetric): String? =
    when (metric) {
        RankingMetric.DISTANCE -> distanceTopPercent
        RankingMetric.PACE -> paceTopPercent
        RankingMetric.CONSISTENCY -> consistencyTopPercent
    }?.let { "상위 ${it.toPercentText()}%" }

private fun Double.toPercentText(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", this)
    }

private fun RankingMetric.label(scope: RankingScope): String =
    when (this) {
        RankingMetric.DISTANCE -> "전체 거리"
        RankingMetric.PACE -> if (scope == RankingScope.TEAM) "평균 페이스" else "페이스"
        RankingMetric.CONSISTENCY -> if (scope == RankingScope.TEAM) "평균 활동일" else "횟수"
    }

private val RankingMetric.teamStandardLabel: String
    get() =
        when (this) {
            RankingMetric.DISTANCE -> "팀 총 거리 기준"
            RankingMetric.PACE -> "팀 평균 페이스 기준"
            RankingMetric.CONSISTENCY -> "평균 활동일 기준"
        }

private val RankingScope.skeletonTitle: String
    get() =
        when (this) {
            RankingScope.TEAM -> "전체 팀 순위"
            RankingScope.PERSONAL -> "전체 개인 순위"
        }

private fun RankingMetric.standardLabel(scope: RankingScope): String =
    when (scope) {
        RankingScope.TEAM -> teamStandardLabel
        RankingScope.PERSONAL ->
            when (this) {
                RankingMetric.DISTANCE -> "개인 총 거리 기준"
                RankingMetric.PACE -> "개인 페이스 기준"
                RankingMetric.CONSISTENCY -> "개인 횟수 기준"
            }
    }

private data class RankingItem(
    val rank: Int?,
    val name: String,
    val valueText: String,
    val isMine: Boolean = false,
    val percentileText: String? = null,
) {
    val highlightText: String
        get() = listOfNotNull(valueText, percentileText?.let { "($it)" }).joinToString(" ")
}

private fun Int.toKilometerText(): String = String.format(Locale.getDefault(), "%.1f km", this / METERS_PER_KILOMETER)

private fun Int.toPaceText(): String {
    if (this <= 0) return "-'--\"/km"
    val minutes = this / SECONDS_PER_MINUTE
    val seconds = this % SECONDS_PER_MINUTE
    return String.format(Locale.getDefault(), "%d'%02d\"/km", minutes, seconds)
}

private fun Double.toActiveDaysText(): String =
    if (this % 1.0 == 0.0) {
        "${toInt()}일"
    } else {
        String.format(Locale.getDefault(), "%.1f일", this)
    }

private fun Int.toActiveDaysText(): String = "${this}일"

private fun previewUiState(scope: RankingScope = RankingScope.PERSONAL): RankingUiState =
    RankingUiState(
        selectedScope = scope,
        homeState =
            HomeState(
                profile = UserProfile("user-2", "김영희", null, "team-2"),
                team = TeamSummary("team-2", "김영희", null, null, 5, false),
            ),
        myRankingSummary =
            MyRankingSummary(
                season = RankingSeason("season", "2026-06", 2026, 6, 10),
                eligible = true,
                requiredDistanceMeters = 10_000,
                distanceMeters = 253_100,
                remainingDistanceMeters = 0,
                durationSeconds = 12_000,
                averagePaceSecondsPerKm = 278,
                runCount = 28,
                activeDays = 12,
                consistencyRate = 80,
                distanceRank = 2,
                distanceTopPercent = 1.0,
                paceRank = 2,
                paceTopPercent = 1.0,
                consistencyRank = 2,
                consistencyTopPercent = 1.0,
            ),
        userRankings =
            listOf(
                UserRanking(1, "user-1", "롯23데", null, null, null, 298_300, 0, 252, 32, 12, 10, 100),
                UserRanking(2, "user-2", "김영희", null, "team-2", "런파민", 253_100, 0, 278, 28, 10, 10, 90),
                UserRanking(3, "user-3", "롯데55", null, null, null, 241_800, 0, 292, 24, 8, 10, 80),
            ),
        teamRankings =
            listOf(
                TeamRanking(1, "team-1", "롯23데", 298_300, 87_016, 292, 32, 12, 2.4),
                TeamRanking(2, "team-2", "김영희", 253_100, 77_563, 307, 28, 10, 2.0),
                TeamRanking(3, "team-3", "롯데55", 241_800, 76_884, 318, 24, 8, 1.6),
            ),
    )

private const val METERS_PER_KILOMETER = 1_000.0
private const val SECONDS_PER_MINUTE = 60
private const val RANKING_SKELETON_ROW_COUNT = 6
private val RankingSkeletonColor = Color(0xFFD8E1ED)
private val RankingSkeletonDarkColor = Color(0xFFB8C4D4)

@Preview(showBackground = true)
@Composable
private fun PersonalRankingScreenPreview() {
    RunpamineTheme {
        RankingContent(
            uiState = previewUiState(),
            onScopeSelect = {},
            onMetricSelect = {},
            onRetryClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamRankingScreenPreview() {
    RunpamineTheme {
        RankingContent(
            uiState = previewUiState(RankingScope.TEAM),
            onScopeSelect = {},
            onMetricSelect = {},
            onRetryClick = {},
        )
    }
}
