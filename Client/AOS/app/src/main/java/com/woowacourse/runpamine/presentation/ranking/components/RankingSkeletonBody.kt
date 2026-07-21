package com.woowacourse.runpamine.presentation.ranking.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.domain.ranking.RankingMetric
import com.woowacourse.runpamine.domain.ranking.teamStandardLabel
import com.woowacourse.runpamine.presentation.ranking.model.RankingScope
import com.woowacourse.runpamine.ui.theme.Blue10

private val RankingSkeletonDarkColor = Color(0xFFB8C4D4)

private const val RANKING_SKELETON_ROW_COUNT = 6
private val RankingSkeletonColor = Color(0xFFD8E1ED)

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

@Composable
fun RankingSkeletonBody(
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
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(
            color = DividerDefaults.color.copy(alpha = 0.1f),
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
                .height(65.dp)
                .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        RankingSkeletonBox(
            modifier =
                Modifier
                    .size(28.dp),
            color = RankingSkeletonDarkColor,
            shape = RoundedCornerShape(12.dp),
        )
        RankingSkeletonBox(
            modifier =
                Modifier
                    .width(78.dp)
                    .height(22.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        RankingSkeletonBox(
            modifier =
                Modifier
                    .width(118.dp)
                    .height(24.dp),
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
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = selectedScope.skeletonTitle,
                color = Color(0xFF111827),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = selectedMetric.standardLabel(selectedScope),
                color = Color(0xFFA6AFBD),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
            )
        }
        repeat(RANKING_SKELETON_ROW_COUNT) { index ->
            RankingRowSkeleton(selected = index == 1)
        }
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

@Composable
private fun RankingRowSkeleton(selected: Boolean) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) Blue10 else Color(0xFFF8F8F8))
                .height(52.dp)
                .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RankingSkeletonBox(
            modifier =
                Modifier
                    .size(28.dp),
            color = RankingSkeletonDarkColor,
            shape = RoundedCornerShape(12.dp),
        )
        RankingSkeletonBox(
            modifier =
                Modifier
                    .width(96.dp)
                    .height(22.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        RankingSkeletonBox(
            modifier =
                Modifier
                    .width(68.dp)
                    .height(22.dp),
        )
    }
}
