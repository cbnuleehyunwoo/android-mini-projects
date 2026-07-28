package com.woowacourse.runpamine.presentation.mypage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woowacourse.runpamine.BuildConfig
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.di.runpamineContainer
import com.woowacourse.runpamine.presentation.component.ConfirmationDialogStyle
import com.woowacourse.runpamine.presentation.component.RunpamineConfirmationDialog
import com.woowacourse.runpamine.presentation.component.ScreenTopBar
import com.woowacourse.runpamine.presentation.mypage.components.MyPageMenuRow
import com.woowacourse.runpamine.presentation.mypage.components.MyPageProfile
import com.woowacourse.runpamine.presentation.mypage.components.MyPageSection
import com.woowacourse.runpamine.presentation.mypage.components.MyPageSkeletonContent
import com.woowacourse.runpamine.presentation.mypage.viewmodel.MyPageUiState
import com.woowacourse.runpamine.presentation.mypage.viewmodel.MyPageViewModel
import com.woowacourse.runpamine.ui.theme.RunpamineLayout
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageBottomSheet(
    onChangeNicknameClick: () -> Unit,
    onDismissRequest: () -> Unit,
    onLogoutCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        modifier = modifier,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f),
        ) {
            MyPageScreen(
                onChangeNicknameClick = onChangeNicknameClick,
                onBackClick = onDismissRequest,
                onLogoutCompleted = onLogoutCompleted,
                showBackButton = false,
            )
        }
    }
}

@Composable
fun MyPageScreen(
    onChangeNicknameClick: () -> Unit,
    onBackClick: () -> Unit,
    onLogoutCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
) {
    val container = LocalContext.current.runpamineContainer
    val viewModel: MyPageViewModel =
        viewModel(
            factory =
                MyPageViewModel.Factory(
                    profileRepository = container.profileRepository,
                    authRepository = container.authRepository,
                    clearLocalUserData = container::clearLocalUserData,
                ),
        )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadMyProfile()
    }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            viewModel.onLoggedOutHandled()
            onLogoutCompleted()
        }
    }

    MyPageContent(
        uiState = uiState,
        onChangeNicknameClick = onChangeNicknameClick,
        onLogoutClick = viewModel::logout,
        onDeleteAccountClick = viewModel::deleteAccount,
        onBackClick = onBackClick,
        showBackButton = showBackButton,
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
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteAccountDialog by rememberSaveable { mutableStateOf(false) }
    val privacyPolicyUrl = stringResource(R.string.my_page_privacy_policy_url)
    val termsOfServiceUrl = stringResource(R.string.my_page_terms_of_service_url)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = RunpamineLayout.ScreenHorizontalPadding)
                .verticalScroll(rememberScrollState()),
    ) {
        ScreenTopBar(
            title = stringResource(R.string.my_page_title),
            onBackClick = onBackClick,
            showBackButton = showBackButton,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = RunpamineLayout.NavigationTopPadding),
        )
        if (uiState.isLoading) {
            MyPageSkeletonContent(modifier = Modifier.padding(top = 34.dp))
        } else {
            MyPageProfile(
                name = uiState.nickname,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 34.dp),
            )
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFFDC2626),
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp,
                        ),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
            MyPageSection(
                title = stringResource(R.string.my_page_account_settings),
            ) {
                MyPageMenuRow(
                    iconResId = R.drawable.ic_edit,
                    title = stringResource(R.string.my_page_change_nickname),
                    description = stringResource(R.string.my_page_change_nickname_description),
                    onClick = onChangeNicknameClick,
                )
                MyPageMenuRow(
                    iconResId = R.drawable.ic_logout,
                    title = stringResource(R.string.my_page_logout),
                    description =
                        if (uiState.isLoggingOut) {
                            stringResource(R.string.my_page_logging_out)
                        } else {
                            stringResource(R.string.my_page_logout_description)
                        },
                    titleColor = Color(0xFFDC2626),
                    onClick = {
                        if (!uiState.isLoggingOut) {
                            showLogoutDialog = true
                        }
                    },
                )
                MyPageMenuRow(
                    iconResId = R.drawable.mdi_delete_outline,
                    title = stringResource(R.string.my_page_delete_account),
                    titleColor = Color(0xFFDC2626),
                    description = stringResource(R.string.my_page_delete_account_description),
                    onClick = {
                        if (!uiState.isDeletingAccount) {
                            showDeleteAccountDialog = true
                        }
                    },
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            MyPageSection(
                title = stringResource(R.string.my_page_terms_and_policy),
            ) {
                MyPageMenuRow(
                    iconResId = R.drawable.ic_shield,
                    title = stringResource(R.string.my_page_privacy_policy),
                    description = stringResource(R.string.my_page_privacy_policy_description),
                    onClick = { uriHandler.openUri(privacyPolicyUrl) },
                )
                MyPageMenuRow(
                    iconResId = R.drawable.ic_page,
                    title = stringResource(R.string.my_page_terms_of_service),
                    description = stringResource(R.string.my_page_terms_of_service_description),
                    onClick = { uriHandler.openUri(termsOfServiceUrl) },
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            MyPageSection(
                title = stringResource(R.string.my_page_etc),
            ) {
                MyPageMenuRow(
                    iconResId = R.drawable.ic_infomation,
                    title = stringResource(R.string.my_page_app_info),
                    description = stringResource(R.string.my_page_app_version, BuildConfig.VERSION_NAME),
                    showArrow = false,
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showLogoutDialog) {
        RunpamineConfirmationDialog(
            title = stringResource(R.string.my_page_logout),
            message = stringResource(R.string.logout_confirmation),
            dismissText = stringResource(R.string.cancel),
            confirmText = stringResource(R.string.my_page_logout),
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogoutClick()
            },
            style = ConfirmationDialogStyle.Destructive,
        )
    }

    if (showDeleteAccountDialog) {
        RunpamineConfirmationDialog(
            title = stringResource(R.string.my_page_delete_account),
            message = stringResource(R.string.delete_account_confirmation),
            dismissText = stringResource(R.string.cancel),
            confirmText = stringResource(R.string.my_page_delete_account),
            onDismiss = { showDeleteAccountDialog = false },
            onConfirm = {
                showDeleteAccountDialog = false
                onDeleteAccountClick()
            },
            style = ConfirmationDialogStyle.Destructive,
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
