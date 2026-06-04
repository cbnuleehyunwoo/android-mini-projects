import SwiftUI

struct RootView: View {
    @State private var route: OnboardingRoute = .splash
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
                LoginView(viewModel: LoginViewModel(authService: authService)) { _ in }
            }
        }
        .tint(AppTheme.Colors.primary)
    }
}

private enum OnboardingRoute {
    case splash
    case login
}
