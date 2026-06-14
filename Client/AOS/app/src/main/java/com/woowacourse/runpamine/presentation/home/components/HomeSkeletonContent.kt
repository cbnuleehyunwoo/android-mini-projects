package com.woowacourse.runpamine.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.presentation.component.SkeletonBox

@Composable
fun HomeSkeletonContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SkeletonBox(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SkeletonBox(
                    modifier =
                        Modifier
                            .width(132.dp)
                            .height(16.dp),
                )
                SkeletonBox(
                    modifier =
                        Modifier
                            .width(174.dp)
                            .height(16.dp),
                )
            }
            SkeletonBox(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(112.dp)
                    .background(
                        color = Color(0xFFF4F7FB),
                        shape = RoundedCornerShape(15.dp),
                    ).padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SkeletonBox(
                modifier =
                    Modifier
                        .fillMaxWidth(0.48f)
                        .height(20.dp),
            )
            SkeletonBox(
                modifier =
                    Modifier
                        .fillMaxWidth(0.76f)
                        .height(16.dp),
            )
            SkeletonBox(
                modifier =
                    Modifier
                        .fillMaxWidth(0.58f)
                        .height(16.dp),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFF4F7FB)),
        ) {
            SkeletonBox(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .size(88.dp),
                shape = CircleShape,
            )
        }
    }
}
