package com.woowacourse.runpamine.presentation.record.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.presentation.record.model.RecordPeriod
import com.woowacourse.runpamine.ui.theme.Gray40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RecordHeader(
    period: RecordPeriod,
    onPeriodSelect: (RecordPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.record_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        RecordPeriodToggle(
            period = period,
            onPeriodSelect = onPeriodSelect,
        )
    }
}

@Composable
private fun RecordPeriodToggle(
    period: RecordPeriod,
    onPeriodSelect: (RecordPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Gray40.copy(alpha = 0.12f))
                .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PeriodChip(
            text = stringResource(R.string.record_period_week),
            isSelected = period == RecordPeriod.WEEK,
            onClick = { onPeriodSelect(RecordPeriod.WEEK) },
        )
        PeriodChip(
            text = stringResource(R.string.record_period_month),
            isSelected = period == RecordPeriod.MONTH,
            onClick = { onPeriodSelect(RecordPeriod.MONTH) },
        )
    }
}

@Composable
private fun PeriodChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (isSelected) Color.White else Gray40,
        modifier =
            modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 6.dp),
    )
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun RecordHeaderPreview() {
    RunpamineTheme {
        RecordHeader(
            period = RecordPeriod.WEEK,
            onPeriodSelect = {},
        )
    }
}
