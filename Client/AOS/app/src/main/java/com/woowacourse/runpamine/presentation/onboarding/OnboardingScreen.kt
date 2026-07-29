package com.woowacourse.runpamine.presentation.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.Pretendard
import com.woowacourse.runpamine.ui.theme.RunpamineTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun OnboardingScreen(
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingContent(
        onStartClick = onStartClick,
        modifier = modifier,
    )
}

@Composable
private fun OnboardingContent(
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pages = onboardingPages
    val pagerState = rememberPagerState(pageCount = { pages.size })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage to pagerState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest { (currentPage, isScrollInProgress) ->
                if (isScrollInProgress) return@collectLatest
                delay(AUTO_SCROLL_INTERVAL_MILLIS)
                pagerState.animateScrollToPage((currentPage + 1) % pages.size)
            }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) { pageIndex ->
            OnboardingPage(
                page = pages[pageIndex],
                modifier = Modifier.fillMaxSize(),
            )
        }

        PageIndicator(
            pageCount = pages.size,
            currentPage = pagerState.currentPage,
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onStartClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5BFF)),
        ) {
            Text(
                text = stringResource(R.string.onboarding_start),
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun OnboardingPage(
    page: OnboardingPageModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(page.imageResId),
            contentDescription = null,
            modifier = Modifier.size(270.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = stringResource(page.titleResId),
            style =
                TextStyle(
                    fontFamily = Pretendard,
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Bold,
                ),
            color = Color(0xFF18202D),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = stringResource(page.descriptionResId),
            style =
                TextStyle(
                    fontFamily = Pretendard,
                    fontSize = 16.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Medium,
                ),
            color = Color(0xFF7B7F87),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .background(
                            color = if (index == currentPage) Color(0xFF0D5BFF) else Color(0xFFD6DAE1),
                            shape = CircleShape,
                        ),
            )
        }
    }
}

private data class OnboardingPageModel(
    @param:DrawableRes val imageResId: Int,
    @param:StringRes val titleResId: Int,
    @param:StringRes val descriptionResId: Int,
)

private val onboardingPages =
    listOf(
        OnboardingPageModel(
            imageResId = R.drawable.img_onboarding_1,
            titleResId = R.string.onboarding_title_1,
            descriptionResId = R.string.onboarding_description_1,
        ),
        OnboardingPageModel(
            imageResId = R.drawable.img_onboarding_2,
            titleResId = R.string.onboarding_title_2,
            descriptionResId = R.string.onboarding_description_2,
        ),
        OnboardingPageModel(
            imageResId = R.drawable.img_onboarding_3,
            titleResId = R.string.onboarding_title_3,
            descriptionResId = R.string.onboarding_description_3,
        ),
        OnboardingPageModel(
            imageResId = R.drawable.img_onboarding_4,
            titleResId = R.string.onboarding_title_4,
            descriptionResId = R.string.onboarding_description_4,
        ),
    )

private const val AUTO_SCROLL_INTERVAL_MILLIS = 2_000L

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun OnboardingScreenPreview() {
    RunpamineTheme {
        OnboardingContent(onStartClick = {})
    }
}
