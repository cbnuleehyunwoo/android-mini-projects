package com.woowacourse.runpamine.presentation.running.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.domain.run.RunSplit
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme
import kotlin.math.roundToInt

@Composable
fun SplitPaceSection(
    splits: List<RunSplit>,
    modifier: Modifier = Modifier,
) {
    if (splits.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "구간 페이스",
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                ),
            color = Color(0xFF101828),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            splits.forEach { split ->
                SplitPaceRow(
                    label = split.label(),
                    pace = "${split.paceSecondsPerKm.toPaceText()}/km",
                )
            }
        }
    }
}

@Composable
private fun SplitPaceRow(
    label: String,
    pace: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(
                    color = Color(0xFFFAFAFC),
                    shape = RoundedCornerShape(14.dp),
                ).padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Color(0xFF344054),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = pace,
            color = Blue40,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun RunSplit.label(): String =
    if (distanceMeters >= FULL_SPLIT_MINIMUM_METERS) {
        "$sequence km"
    } else {
        "마지막 $distanceMeters m"
    }

private fun Double.toPaceText(): String {
    val totalSeconds = coerceAtLeast(0.0).roundToInt()
    return "%d'%02d\"".format(totalSeconds / 60, totalSeconds % 60)
}

@Preview(showBackground = true, widthDp = 393)
@Composable
private fun SplitPaceSectionPreview() {
    RunpamineTheme {
        SplitPaceSection(
            splits =
                listOf(
                    RunSplit(1, 0, 1_000, 1_000, 252_000, 252.0),
                    RunSplit(2, 1_000, 1_064, 64, 19_392, 303.0),
                ),
            modifier = Modifier.padding(10.dp),
        )
    }
}

private const val FULL_SPLIT_MINIMUM_METERS = 1_000
