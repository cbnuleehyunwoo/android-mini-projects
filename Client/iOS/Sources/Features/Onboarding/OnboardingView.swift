import SwiftUI

struct OnboardingView: View {
    let onStart: () -> Void
    @State private var selectedPage = 0

    private let pages = OnboardingPage.pages

    var body: some View {
        VStack(spacing: 0) {
            TabView(selection: $selectedPage) {
                ForEach(pages.indices, id: \.self) { index in
                    OnboardingPageView(page: pages[index])
                        .tag(index)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))

            pageIndicator
                .padding(.bottom, 28)

            PrimaryButton(title: "지금 바로 시작하기!") {
                onStart()
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 14)
        }
        .background(Color.white)
        .task(id: selectedPage) {
            await advanceAfterDelay()
        }
    }

    private var pageIndicator: some View {
        HStack(spacing: 8) {
            ForEach(pages.indices, id: \.self) { index in
                Circle()
                    .fill(index == selectedPage ? AppTheme.Colors.primary : Color(red: 0.84, green: 0.86, blue: 0.88))
                    .frame(width: 8, height: 8)
            }
        }
    }

    private func advanceAfterDelay() async {
        try? await Task.sleep(nanoseconds: 2_000_000_000)
        guard !Task.isCancelled else { return }

        await MainActor.run {
            withAnimation(.easeInOut(duration: 0.28)) {
                selectedPage = (selectedPage + 1) % pages.count
            }
        }
    }
}

private struct OnboardingPageView: View {
    let page: OnboardingPage

    var body: some View {
        VStack(spacing: 0) {
            Image(page.imageName)
                .resizable()
                .scaledToFit()
                .frame(width: 270, height: 270)
                .accessibilityHidden(true)

            Text(page.title)
                .font(AppTheme.Typography.font(size: 28, weight: .bold))
                .foregroundStyle(AppTheme.Colors.textPrimary)
                .multilineTextAlignment(.center)
                .lineSpacing(4)
                .padding(.top, 22)

            Text(page.description)
                .font(AppTheme.Typography.font(size: 16, weight: .medium))
                .foregroundStyle(Color(red: 0.48, green: 0.50, blue: 0.53))
                .multilineTextAlignment(.center)
                .lineSpacing(8)
                .padding(.top, 28)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
        .padding(.horizontal, 24)
    }
}

private struct OnboardingPage {
    let imageName: String
    let title: String
    let description: String

    static let pages = [
        OnboardingPage(
            imageName: "onboarding_persistence",
            title: "런파민에서 꾸준히 달려보세요.",
            description: "런파민은 팀원들과 러닝 기록을 공유하고,\n서로의 꾸준함을 응원하는 러닝 서비스예요."
        ),
        OnboardingPage(
            imageName: "onboarding_team",
            title: "먼저 팀에 합류해볼까요?",
            description: "팀을 만들거나 초대 코드로 팀에 참가하고,\n내 캐릭터와 러닝 기록을 팀원들과 공유해보세요."
        ),
        OnboardingPage(
            imageName: "onboarding_running",
            title: "뛰는 만큼 캐릭터도 달라져요",
            description: "빠르게, 천천히, 꾸준히, 혹은 쉬엄쉬엄\n오늘의 러닝에 따라 팀 대시보드 캐릭터가 달라져요."
        ),
        OnboardingPage(
            imageName: "onboarding_feedback",
            title: "런파민은 아직 성장 중이에요",
            description: "불편한 점이나 갖고 싶은 기능이 있다면 편하게 말해주세요.\n좋은 의견은 정말 빠르게 반영할게요."
        )
    ]
}

#Preview {
    OnboardingView {}
}
