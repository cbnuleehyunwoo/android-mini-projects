package com.woowacourse.runpamine.shared.ui.screens.onboarding

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.shared.generated.resources.Res
import com.woowacourse.runpamine.shared.generated.resources.onboarding_feedback
import com.woowacourse.runpamine.shared.generated.resources.onboarding_persistence
import com.woowacourse.runpamine.shared.generated.resources.onboarding_running
import com.woowacourse.runpamine.shared.generated.resources.onboarding_team
import com.woowacourse.runpamine.shared.ui.components.PrimaryButton
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTypography
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun OnboardingScreen(
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    autoAdvance: Boolean = true,
) {
    val pagerState = rememberPagerState(pageCount = { OnboardingPages.size })

    LaunchedEffect(pagerState, autoAdvance) {
        if (!autoAdvance) return@LaunchedEffect

        while (true) {
            delay(2_000L)
            val nextPage = (pagerState.currentPage + 1) % OnboardingPages.size
            pagerState.animateScrollToPage(
                page = nextPage,
                animationSpec = tween(durationMillis = 280),
            )
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White)
                .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) { pageIndex ->
            OnboardingPageContent(page = OnboardingPages[pageIndex])
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OnboardingPages.indices.forEach { index ->
                Box(
                    modifier =
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) {
                                    RunpamineColors.Primary
                                } else {
                                    Color(0xFFD6DBE0)
                                },
                            ),
                )
            }
        }

        PrimaryButton(
            title = "지금 바로 시작하기!",
            onClick = onStart,
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 14.dp),
        )
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(page.image),
            contentDescription = null,
            modifier = Modifier.size(270.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(22.dp))
        Text(
            text = page.title,
            style = RunpamineTypography.Header1.copy(fontWeight = FontWeight.Bold, lineHeight = 36.sp),
            color = RunpamineColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        Text(
            text = page.description,
            style = RunpamineTypography.Body1.copy(lineHeight = 27.sp),
            color = Color(0xFF7A8087),
            textAlign = TextAlign.Center,
        )
    }
}

private data class OnboardingPage(
    val image: DrawableResource,
    val title: String,
    val description: String,
)

private val OnboardingPages =
    listOf(
        OnboardingPage(
            image = Res.drawable.onboarding_persistence,
            title = "런파민에서 꾸준히 달려보세요.",
            description = "런파민은 팀원들과 러닝 기록을 공유하고,\n서로의 꾸준함을 응원하는 러닝 서비스예요.",
        ),
        OnboardingPage(
            image = Res.drawable.onboarding_team,
            title = "먼저 팀에 합류해볼까요?",
            description = "팀을 만들거나 초대 코드로 팀에 참가하고,\n내 캐릭터와 러닝 기록을 팀원들과 공유해보세요.",
        ),
        OnboardingPage(
            image = Res.drawable.onboarding_running,
            title = "뛰는 만큼 캐릭터도 달라져요",
            description = "빠르게, 천천히, 꾸준히, 혹은 쉬엄쉬엄\n오늘의 러닝에 따라 팀 대시보드 캐릭터가 달라져요.",
        ),
        OnboardingPage(
            image = Res.drawable.onboarding_feedback,
            title = "런파민은 아직 성장 중이에요",
            description = "불편한 점이나 갖고 싶은 기능이 있다면 편하게 말해주세요.\n좋은 의견은 정말 빠르게 반영할게요.",
        ),
    )

@Preview
@Composable
private fun OnboardingScreenPreview() {
    RunpamineTheme {
        OnboardingScreen(onStart = {}, autoAdvance = false)
    }
}
