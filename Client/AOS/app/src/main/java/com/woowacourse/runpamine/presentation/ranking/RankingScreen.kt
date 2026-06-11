package com.woowacourse.runpamine.presentation.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.woowacourse.runpamine.ui.theme.Blue40
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
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        RankingScopeTabs(
            selectedScope = uiState.selectedScope,
            onScopeSelect = onScopeSelect,
            modifier = Modifier.fillMaxWidth(),
        )
        if (uiState.selectedScope == RankingScope.PERSONAL) {
            Spacer(modifier = Modifier.height(26.dp))
            PersonalMetricTabs(
                selectedMetric = uiState.selectedMetric,
                onMetricSelect = onMetricSelect,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        MyRankingCard(
            item = myRanking,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(
            color = DividerDefaults.color.copy(alpha = 0.7f),
            thickness = 1.dp,
        )
        Spacer(modifier = Modifier.height(24.dp))
        RankingListCard(
            selectedScope = uiState.selectedScope,
            items = rankingItems,
            modifier = Modifier.fillMaxWidth(),
        )
        RankingStateMessage(
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            isEmpty = rankingItems.isEmpty(),
            onRetryClick = onRetryClick,
        )
        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
fun RankingScopeTabs(
    selectedScope: RankingScope,
    onScopeSelect: (RankingScope) -> Unit,
    modifier: Modifier = Modifier,
) {
    SegmentedTabs(
        items = RankingScope.entries,
        selectedItem = selectedScope,
        label = RankingScope::label,
        onItemSelect = onScopeSelect,
        modifier = modifier,
    )
}

@Composable
private fun <T> SegmentedTabs(
    items: List<T>,
    selectedItem: T,
    label: (T) -> String,
    onItemSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(4.dp),
) {
    Row(
        modifier =
            modifier
                .height(54.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFFE0E0E0))
                .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            val selected = item == selectedItem
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selected) Blue40 else Color.Transparent)
                        .clickable(
                            role = Role.Tab,
                            onClick = { onItemSelect(item) },
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label(item),
                    color = if (selected) Color.White else Color(0xFF9AA3B2),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
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
                .background(if (item.isMine) Color(0xFFEAF4FF) else Color(0xFFF8F8F8))
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
private fun PersonalMetricTabs(
    selectedMetric: RankingMetric,
    onMetricSelect: (RankingMetric) -> Unit,
    modifier: Modifier = Modifier,
) {
    SegmentedTabs(
        items = RankingMetric.entries,
        selectedItem = selectedMetric,
        label = RankingMetric::label,
        onItemSelect = onMetricSelect,
        modifier = modifier,
        contentPadding = PaddingValues(2.dp),
    )
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
                .background(Color(0xFFEAF4FF))
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
                    text = "팀 총 거리 기준",
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

private fun RankingUiState.toRankingItems(): List<RankingItem> =
    when (selectedScope) {
        RankingScope.TEAM ->
            teamRankings.map { ranking ->
                ranking.toRankingItem(
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
                name = homeState?.team?.name ?: "내 팀",
                valueText = "10km 이상 필요",
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

private fun TeamRanking.toRankingItem(isMine: Boolean): RankingItem =
    RankingItem(
        rank = rank,
        name = teamName,
        valueText = distanceMeters.toKilometerText(),
        isMine = isMine,
    )

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
        RankingMetric.CONSISTENCY -> "${runCount}회"
    }

private fun MyRankingSummary.valueTextFor(metric: RankingMetric): String =
    when (metric) {
        RankingMetric.DISTANCE -> distanceMeters.toKilometerText()
        RankingMetric.PACE -> averagePaceSecondsPerKm.toPaceText()
        RankingMetric.CONSISTENCY -> "${runCount}회"
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

private val RankingMetric.label: String
    get() =
        when (this) {
            RankingMetric.DISTANCE -> "KM"
            RankingMetric.PACE -> "페이스"
            RankingMetric.CONSISTENCY -> "횟수"
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
                TeamRanking(1, "team-1", "롯23데", 298_300, 0, 32, 12),
                TeamRanking(2, "team-2", "김영희", 253_100, 0, 28, 10),
                TeamRanking(3, "team-3", "롯데55", 241_800, 0, 24, 8),
            ),
    )

private const val METERS_PER_KILOMETER = 1_000.0
private const val SECONDS_PER_MINUTE = 60

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
