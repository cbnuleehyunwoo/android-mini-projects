import SwiftUI

struct RootView: View {
    @State private var route: OnboardingRoute = .splash
    @State private var acceptedTerms: [TermsAgreement] = []
    private let authService: AuthServiceProtocol
    private let store: LocalAppStateStore

    init(authService: AuthServiceProtocol, store: LocalAppStateStore) {
        self.authService = authService
        self.store = store
    }

    var body: some View {
        Group {
            switch route {
            case .splash:
                SplashView {
                    withAnimation(.easeInOut(duration: 0.3)) {
                        route = store.hasCompletedSignup ? .main : .login
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
                MainTabView(store: store)
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
