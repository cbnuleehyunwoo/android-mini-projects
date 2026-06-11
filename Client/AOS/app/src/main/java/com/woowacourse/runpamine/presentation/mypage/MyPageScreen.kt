package com.woowacourse.runpamine.presentation.mypage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.di.runpamineContainer
import com.woowacourse.runpamine.presentation.component.RunpamineConfirmationDialog
import com.woowacourse.runpamine.presentation.component.ScreenTopBar
import com.woowacourse.runpamine.presentation.mypage.components.MyPageMenuRow
import com.woowacourse.runpamine.presentation.mypage.components.MyPageProfile
import com.woowacourse.runpamine.presentation.mypage.components.MyPageSection
import com.woowacourse.runpamine.presentation.mypage.viewmodel.MyPageUiState
import com.woowacourse.runpamine.presentation.mypage.viewmodel.MyPageViewModel
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

private const val PRIVACY_POLICY_URL =
    "https://sheer-mimosa-20f.notion.site/37958b8d8e6c80cdb6b8c29d6d6935f5?pvs=74"
private const val TERMS_OF_SERVICE_URL =
    "https://sheer-mimosa-20f.notion.site/2b658b8d8e6c80f386e1ef1910dbff34?pvs=73"

@Composable
fun MyPageScreen(
    onChangeNicknameClick: () -> Unit,
    onBackClick: () -> Unit,
    onLogoutCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.runpamineContainer
    val viewModel: MyPageViewModel =
        viewModel(
            factory =
                MyPageViewModel.Factory(
                    profileRepository = container.profileRepository,
                    authRepository = container.authRepository,
                ),
        )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLogoutCompleted()
        }
    }

    MyPageContent(
        uiState = uiState,
        onChangeNicknameClick = onChangeNicknameClick,
        onLogoutClick = viewModel::logout,
        onDeleteAccountClick = viewModel::deleteAccount,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
private fun MyPageContent(
    uiState: MyPageUiState,
    onChangeNicknameClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    var showDeleteAccountDialog by rememberSaveable { mutableStateOf(false) }

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
            name = uiState.nickname.ifBlank { "러너" },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = Color(0xFFDC2626),
                modifier = Modifier.padding(top = 12.dp),
            )
        }
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
                description = if (uiState.isLoggingOut) "로그아웃 중입니다" else "계정에서 로그아웃합니다",
                titleColor = Color(0xFFDC2626),
                onClick = onLogoutClick,
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
                onClick = { uriHandler.openUri(PRIVACY_POLICY_URL) },
            )
            MyPageMenuRow(
                iconResId = R.drawable.ic_page,
                title = "이용약관",
                description = "서비스 이용에 관한 약관을 확인하세요",
                onClick = { uriHandler.openUri(TERMS_OF_SERVICE_URL) },
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        MyPageSection(
            title = "기타",
        ) {
            MyPageMenuRow(
                iconResId = R.drawable.ic_infomation,
                title = "앱 정보",
                description = "버전 1.2.3",
                showArrow = false,
            )
        }
        Text(
            text = if (uiState.isDeletingAccount) "회원 탈퇴 중입니다" else stringResource(R.string.delete_account),
            color = Color(0xFFDC2626),
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier
                    .align(Alignment.End)
                    .navigationBarsPadding()
                    .padding(top = 18.dp, bottom = 48.dp)
                    .clickable(enabled = !uiState.isDeletingAccount) {
                        showDeleteAccountDialog = true
                    },
        )
    }

    if (showDeleteAccountDialog) {
        RunpamineConfirmationDialog(
            title = stringResource(R.string.delete_account),
            message = stringResource(R.string.delete_account_confirmation),
            dismissText = stringResource(R.string.cancel),
            confirmText = stringResource(R.string.delete_account),
            onDismiss = { showDeleteAccountDialog = false },
            onConfirm = {
                showDeleteAccountDialog = false
                onDeleteAccountClick()
            },
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun MyPageScreenPreview() {
    RunpamineTheme {
        MyPageContent(
            uiState = MyPageUiState(nickname = "러너", isLoading = false),
            onChangeNicknameClick = {},
            onLogoutClick = {},
            onDeleteAccountClick = {},
            onBackClick = {},
        )
    }
}
