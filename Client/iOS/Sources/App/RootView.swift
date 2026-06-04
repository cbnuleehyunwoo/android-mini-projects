import SwiftUI

struct RootView: View {
    @State private var route: OnboardingRoute = .splash
    @State private var acceptedTerms: [TermsAgreement] = []
    private let authService: AuthServiceProtocol

    init(authService: AuthServiceProtocol) {
        self.authService = authService
    }

    var body: some View {
        Group {
            switch route {
            case .splash:
                SplashView {
                    withAnimation(.easeInOut(duration: 0.3)) {
                        route = .login
                    }
                }
            case .login:
                LoginView(viewModel: LoginViewModel(authService: authService)) { session in
                    route = session.needsSignup ? .terms : .main
                }
            case .terms:
                TermsAgreementView(viewModel: TermsAgreementViewModel()) {
                    route = .login
                } onComplete: { agreements in
                    acceptedTerms = agreements
                    route = .nickname
                }
            case .nickname:
                NicknameSetupView(viewModel: NicknameSetupViewModel(authService: authService, agreements: acceptedTerms)) {
                    route = .terms
                } onComplete: {
                    route = .main
                }
            case .main:
                MainTabView()
            }
        }
        .tint(AppTheme.Colors.primary)
    }
}

private enum OnboardingRoute {
    case splash
    case login
    case terms
    case nickname
    case main
}
