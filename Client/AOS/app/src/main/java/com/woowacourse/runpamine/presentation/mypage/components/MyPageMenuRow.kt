package com.woowacourse.runpamine.presentation.mypage.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun MyPageMenuRow(
    @DrawableRes iconResId: Int,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    titleColor: Color = Color(0xFF111827),
    showArrow: Boolean = true,
    onClick: () -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(84.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = 1.dp,
                    color = Color(0xFFEDEFF3),
                    shape = RoundedCornerShape(14.dp),
                ).clickable(onClick = onClick)
                .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = title,
            modifier = Modifier.size(32.dp),
            colorFilter = if (titleColor == Color(0xFFDC2626)) null else ColorFilter.tint(Blue40),
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 17.sp, lineHeight = 21.sp),
                color = Color(0xFF9CA3AF),
            )
        }
        if (showArrow) {
            Image(
                painter = painterResource(id = R.drawable.ic_right_arrow),
                contentDescription = "이동",
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393)
@Composable
private fun MyPageMenuRowPreview() {
    RunpamineTheme {
        Column(
            modifier =
                Modifier
                    .background(Color.White)
                    .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MyPageMenuRow(
                iconResId = R.drawable.ic_edit,
                title = "닉네임 변경",
                description = "닉네임을 변경할 수 있습니다.",
                onClick = {},
            )
            MyPageMenuRow(
                iconResId = R.drawable.ic_logout,
                title = "로그아웃",
                description = "계정에서 로그아웃합니다",
                titleColor = Color(0xFFDC2626),
                onClick = {},
            )
            MyPageMenuRow(
                iconResId = R.drawable.ic_infomation,
                title = "앱 정보",
                description = "버전 1.2.3",
                showArrow = false,
                onClick = {},
            )
        }
    }
}
