package com.woowacourse.runpamine.presentation.mypage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.presentation.component.SkeletonBox

@Composable
fun MyPageSkeletonContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SkeletonBox(
            modifier = Modifier.size(76.dp),
            shape = CircleShape,
        )
        Spacer(modifier = Modifier.height(16.dp))
        SkeletonBox(
            modifier =
                Modifier
                    .width(92.dp)
                    .height(24.dp),
            shape = RoundedCornerShape(10.dp),
        )
        Spacer(modifier = Modifier.height(48.dp))

        MyPageSectionSkeleton(rowCount = 3)
        Spacer(modifier = Modifier.height(24.dp))
        MyPageSectionSkeleton(rowCount = 2)
        Spacer(modifier = Modifier.height(24.dp))
        MyPageSectionSkeleton(rowCount = 1)
    }
}

@Composable
private fun MyPageSectionSkeleton(rowCount: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SkeletonBox(
            modifier =
                Modifier
                    .width(104.dp)
                    .height(24.dp),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(rowCount) {
                MyPageMenuRowSkeleton()
            }
        }
    }
}

@Composable
private fun MyPageMenuRowSkeleton() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SkeletonBox(
            modifier = Modifier.size(24.dp),
            shape = RoundedCornerShape(8.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            SkeletonBox(
                modifier =
                    Modifier
                        .fillMaxWidth(0.42f)
                        .height(16.dp),
            )
            SkeletonBox(
                modifier =
                    Modifier
                        .fillMaxWidth(0.68f)
                        .height(13.dp),
            )
        }
        SkeletonBox(
            modifier = Modifier.size(16.dp),
            shape = CircleShape,
        )
    }
}
