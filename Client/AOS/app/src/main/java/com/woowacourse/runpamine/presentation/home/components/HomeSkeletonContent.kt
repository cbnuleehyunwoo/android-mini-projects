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
import androidx.compose.ui.draw.clip
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
                    .padding(start = 20.dp, top = 20.dp, end = 30.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            SkeletonBox(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                SkeletonBox(
                    modifier =
                        Modifier
                            .width(72.dp)
                            .height(15.dp),
                )
                SkeletonBox(
                    modifier =
                        Modifier
                            .width(104.dp)
                            .height(20.dp),
                )
            }
            SkeletonBox(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(126.dp)
                    .background(
                        color = Color(0xFFF4F7FB),
                        shape = RoundedCornerShape(24.dp),
                    ).padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SkeletonBox(
                modifier =
                    Modifier
                        .fillMaxWidth(0.42f)
                        .height(25.dp),
            )
            SkeletonBox(
                modifier =
                    Modifier
                        .fillMaxWidth(0.62f)
                        .height(22.dp),
            )
            Spacer(modifier = Modifier.height(1.dp))
            SkeletonBox(
                modifier =
                    Modifier
                        .width(88.dp)
                        .height(30.dp),
                shape = RoundedCornerShape(15.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 5.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF4F7FB)),
        ) {
            SkeletonBox(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .size(100.dp),
                shape = CircleShape,
            )
        }
    }
}
