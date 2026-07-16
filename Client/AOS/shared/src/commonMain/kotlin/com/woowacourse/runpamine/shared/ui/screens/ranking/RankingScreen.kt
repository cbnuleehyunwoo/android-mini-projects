package com.woowacourse.runpamine.shared.ui.screens.ranking

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.shared.ui.model.RankingEntryUi
import com.woowacourse.runpamine.shared.ui.model.RankingMetric
import com.woowacourse.runpamine.shared.ui.model.RankingScope
import com.woowacourse.runpamine.shared.ui.model.RankingUiState
import com.woowacourse.runpamine.shared.ui.model.RunpamineSamples
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTypography

@Composable
fun RankingScreen(
    state: RankingUiState,
    onScopeSelected: (RankingScope) -> Unit,
    onMetricSelected: (RankingMetric) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().background(Color.White),
    ) {
        RankingScopeControl(
            selectedScope = state.scope,
            onScopeSelected = onScopeSelected,
            modifier = Modifier.padding(top = 18.dp),
        )
        RankingMetricControl(
            scope = state.scope,
            selectedMetric = state.metric,
            onMetricSelected = onMetricSelected,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 15.dp),
        )
        RankingSummary(
            summary = state.summary,
            isLoading = state.isLoading,
            message = state.errorMessage,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 13.dp),
        )

        Spacer(Modifier.height(11.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(RunpamineColors.Border))

        RankingList(
            state = state,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RankingScopeControl(
    selectedScope: RankingScope,
    onScopeSelected: (RankingScope) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth().height(72.dp)) {
        RankingScope.entries.forEach { scope ->
            val isSelected = scope == selectedScope
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clickable(role = Role.Tab) { onScopeSelected(scope) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = scope.label,
                        style =
                            RunpamineTypography.Body2.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        color =
                            if (isSelected) {
                                RunpamineColors.Primary
                            } else {
                                RankingColors.Inactive
                            },
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(if (isSelected) RunpamineColors.Primary else Color.Transparent),
                )
            }
        }
    }
}

@Composable
private fun RankingMetricControl(
    scope: RankingScope,
    selectedMetric: RankingMetric,
    onMetricSelected: (RankingMetric) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RankingMetric.entries.forEach { metric ->
            val isSelected = metric == selectedMetric
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(
                            if (isSelected) RankingColors.SelectedPill else RankingColors.Pill,
                        ).clickable(role = Role.RadioButton) { onMetricSelected(metric) },
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(19.dp))
                            .background(RunpamineColors.Primary.copy(alpha = 0.10f)),
                    )
                }
                Text(
                    text = metric.label(scope),
                    style =
                        RunpamineTypography.Caption1.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    color = if (isSelected) RunpamineColors.Primary else RankingColors.MetricText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RankingSummary(
    summary: RankingEntryUi?,
    isLoading: Boolean,
    message: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(if (summary == null && !isLoading) 96.dp else 65.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(RankingColors.Highlight),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoading && summary == null -> RankingSummarySkeleton()
            summary != null -> {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    RankBadge(rank = summary.rank, isHighlighted = true)
                    Text(
                        text = summary.name,
                        modifier = Modifier.weight(1f),
                        style = RunpamineTypography.Body2.copy(fontWeight = FontWeight.Bold),
                        color = RunpamineColors.Primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = summary.summaryValue,
                        style = RunpamineTypography.Body2.copy(fontWeight = FontWeight.Bold),
                        color = RunpamineColors.Primary,
                        maxLines = 1,
                    )
                }
            }
            else -> {
                Text(
                    text = message ?: "랭킹 데이터가 없습니다.",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = RunpamineTypography.Body1.copy(fontWeight = FontWeight.SemiBold),
                    color = RunpamineColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun RankingList(
    state: RankingUiState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding =
            PaddingValues(
                start = 22.dp,
                top = 24.dp,
                end = 22.dp,
                bottom = 26.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = if (state.scope == RankingScope.Team) "전체 팀 순위" else "전체 개인 순위",
                    modifier = Modifier.weight(1f),
                    style = RunpamineTypography.Title2.copy(fontWeight = FontWeight.Bold),
                    color = RunpamineColors.TextPrimary,
                )
                Text(
                    text = state.metric.subtitle(state.scope),
                    style = RunpamineTypography.Caption1,
                    color = RankingColors.Subtitle,
                )
            }
        }

        when {
            state.isLoading && state.entries.isEmpty() -> {
                items(6) { index ->
                    RankingRowSkeleton(isHighlighted = index == 1)
                }
            }
            state.entries.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.errorMessage ?: "아직 표시할 랭킹이 없습니다.",
                            style =
                                RunpamineTypography.Body1.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            color = RunpamineColors.TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            else -> {
                items(state.entries, key = RankingEntryUi::id) { entry ->
                    RankingRow(entry)
                }
            }
        }
    }
}

@Composable
private fun RankingRow(entry: RankingEntryUi) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (entry.isCurrent) RankingColors.Highlight else RankingColors.Row)
                .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RankBadge(rank = entry.rank, isHighlighted = entry.isCurrent)
        Text(
            text = entry.name,
            modifier = Modifier.weight(1f),
            style = RunpamineTypography.Body2.copy(fontWeight = FontWeight.Bold),
            color = if (entry.isCurrent) RunpamineColors.Primary else RankingColors.Name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = entry.value,
            style = RunpamineTypography.Body2.copy(fontWeight = FontWeight.SemiBold),
            color = if (entry.isCurrent) RunpamineColors.Primary else RankingColors.Subtitle,
            maxLines = 1,
        )
    }
}

@Composable
private fun RankBadge(
    rank: Int,
    isHighlighted: Boolean,
) {
    Box(
        modifier =
            Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isHighlighted) RunpamineColors.Primary else RankingColors.Inactive),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = rank.toString(),
            style = RunpamineTypography.Body2.copy(fontWeight = FontWeight.Black),
            color = Color.White,
        )
    }
}

@Composable
private fun RankingSummarySkeleton() {
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SkeletonBlock(Modifier.size(28.dp), RoundedCornerShape(12.dp))
        SkeletonBlock(Modifier.size(width = 82.dp, height = 14.dp))
        Spacer(Modifier.weight(1f))
        SkeletonBlock(Modifier.size(width = 108.dp, height = 14.dp))
    }
}

@Composable
private fun RankingRowSkeleton(isHighlighted: Boolean) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isHighlighted) RankingColors.Highlight else RankingColors.Row)
                .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SkeletonBlock(Modifier.size(28.dp), RoundedCornerShape(12.dp))
        SkeletonBlock(Modifier.size(width = if (isHighlighted) 82.dp else 68.dp, height = 14.dp))
        Spacer(Modifier.weight(1f))
        SkeletonBlock(Modifier.size(width = if (isHighlighted) 92.dp else 76.dp, height = 14.dp))
    }
}

@Composable
private fun SkeletonBlock(
    modifier: Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(6.dp),
) {
    Box(modifier = modifier.clip(shape).background(RankingColors.Skeleton))
}

private val RankingScope.label: String
    get() = if (this == RankingScope.Team) "팀 랭킹" else "개인 랭킹"

private fun RankingMetric.label(scope: RankingScope): String =
    when (this) {
        RankingMetric.Distance -> "전체 거리"
        RankingMetric.Pace -> "페이스"
        RankingMetric.Activity -> if (scope == RankingScope.Team) "평균 활동일" else "횟수"
    }

private fun RankingMetric.subtitle(scope: RankingScope): String =
    when (this) {
        RankingMetric.Distance -> if (scope == RankingScope.Team) "팀 총 거리 기준" else "누적 거리 기준"
        RankingMetric.Pace -> if (scope == RankingScope.Team) "팀 평균 페이스 기준" else "평균 페이스 기준"
        RankingMetric.Activity -> if (scope == RankingScope.Team) "평균 활동일 기준" else "활동일 기준"
    }

private val RankingEntryUi.summaryValue: String
    get() = percentile?.let { "$value (상위 ${it.coerceAtLeast(0)}%)" } ?: value

private object RankingColors {
    val Inactive = Color(0xFF9EAAB9)
    val MetricText = Color(0xFF6B737F)
    val Pill = Color(0xFFF2F2F5)
    val SelectedPill = Color(0xFFEEF3FF)
    val Highlight = Color(0xFFEBF4FF)
    val Row = Color(0xFFFAFAFC)
    val Name = Color(0xFF38455C)
    val Subtitle = Color(0xFF99A6B8)
    val Skeleton = Color(0xFFDBE3EF)
}

@Preview
@Composable
private fun RankingScreenPreview() {
    RunpamineTheme {
        RankingScreen(
            state =
                RankingUiState(
                    summary = RunpamineSamples.rankingEntries[1],
                    entries = RunpamineSamples.rankingEntries,
                ),
            onScopeSelected = {},
            onMetricSelected = {},
        )
    }
}

@Preview
@Composable
private fun RankingLoadingPreview() {
    RunpamineTheme {
        RankingScreen(
            state = RankingUiState(isLoading = true),
            onScopeSelected = {},
            onMetricSelected = {},
        )
    }
}
