package com.woowacourse.runpamine.presentation.mypage

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.presentation.component.ScreenTopBar
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .safeDrawingPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            ScreenTopBar(
                title = "마이페이지",
                onBackClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            )
            Spacer(modifier = Modifier.height(70.dp))
            MyPageProfile(
                name = "호이",
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(76.dp))
            MyPageSection(
                title = "계정 설정",
            ) {
                MyPageMenuRow(
                    iconResId = R.drawable.ic_edit,
                    title = "닉네임 변경",
                    description = "닉네임을 변경할 수 있습니다.",
                )
                MyPageMenuRow(
                    iconResId = R.drawable.ic_logout,
                    title = "로그아웃",
                    description = "계정에서 로그아웃합니다",
                    titleColor = Color(0xFFDC2626),
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            MyPageSection(
                title = "약관 및 정책",
            ) {
                MyPageMenuRow(
                    iconResId = R.drawable.ic_shield,
                    title = "개인정보처리방침",
                    description = "개인정보 수집 및 이용에 대한 안내",
                )
                MyPageMenuRow(
                    iconResId = R.drawable.ic_page,
                    title = "이용약관",
                    description = "서비스 이용에 관한 약관을 확인하세요",
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            MyPageSection(
                title = "기타",
                contentPadding = PaddingValues(bottom = 48.dp),
            ) {
                MyPageMenuRow(
                    iconResId = R.drawable.ic_infomation,
                    title = "앱 정보",
                    description = "버전 1.2.3",
                    showArrow = false,
                )
            }
        }
    }
}

@Composable
private fun MyPageProfile(
    name: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(86.dp)
                .background(Color(0xFFF9FAFB), CircleShape)
                .border(2.dp, Color(0xFFE5E7EB), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_profile),
                contentDescription = "프로필 이미지",
                modifier = Modifier.size(44.dp),
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, lineHeight = 38.sp),
            fontWeight = FontWeight.Black,
            color = Color(0xFF111827),
        )
    }
}

@Composable
private fun MyPageSection(
    title: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.padding(contentPadding)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, lineHeight = 32.sp),
            fontWeight = FontWeight.Black,
            color = Color(0xFF111827),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun MyPageMenuRow(
    @DrawableRes iconResId: Int,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    titleColor: Color = Color(0xFF111827),
    showArrow: Boolean = true,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = Color(0xFFEDEFF3),
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
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
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp, lineHeight = 26.sp),
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

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun MyPageScreenPreview() {
    RunpamineTheme {
        MyPageScreen()
    }
}
