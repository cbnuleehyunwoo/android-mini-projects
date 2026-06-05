package com.woowacourse.runpamine.presentation.mypage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.presentation.component.ScreenTopBar
import com.woowacourse.runpamine.presentation.mypage.components.MyPageMenuRow
import com.woowacourse.runpamine.presentation.mypage.components.MyPageProfile
import com.woowacourse.runpamine.presentation.mypage.components.MyPageSection
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun MyPageScreen(
    onChangeNicknameClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
    ) {
        ScreenTopBar(
            title = "마이페이지",
            onBackClick = onBackClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
        )
        MyPageProfile(
            name = "호이",
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        MyPageSection(
            title = "계정 설정",
        ) {
            MyPageMenuRow(
                iconResId = R.drawable.ic_edit,
                title = "닉네임 변경",
                description = "닉네임을 변경할 수 있습니다.",
                onClick = onChangeNicknameClick,
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

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun MyPageScreenPreview() {
    RunpamineTheme {
        MyPageScreen(
            onChangeNicknameClick = {},
            onBackClick = {},
        )
    }
}
