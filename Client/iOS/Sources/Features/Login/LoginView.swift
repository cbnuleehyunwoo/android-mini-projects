import SwiftUI

struct LoginView: View {
    @StateObject private var viewModel: LoginViewModel
    let onLoginCompleted: (AuthSession) -> Void

    init(viewModel: LoginViewModel, onLoginCompleted: @escaping (AuthSession) -> Void) {
        _viewModel = StateObject(wrappedValue: viewModel)
        self.onLoginCompleted = onLoginCompleted
    }

    var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()

            VStack {
                Spacer()

                VStack(spacing: 34) {
                    AppLogoView(size: 88)

                    Text("RUNPAMINE")
                        .font(AppTheme.Typography.font(size: 44, weight: .black))
                        .foregroundStyle(AppTheme.Colors.textPrimary)
                        .minimumScaleFactor(0.72)
                }
                .padding(.bottom, 50)

                GoogleLoginButton(isLoading: viewModel.isLoading) {
                    Task {
                        await viewModel.loginWithGoogle()
                        if let session = viewModel.session {
                            onLoginCompleted(session)
                        }
                    }
                }
                .padding(.horizontal, AppTheme.Layout.horizontalPadding)

                if let message = viewModel.errorMessage {
                    Text(message)
                        .font(AppTheme.Typography.body2)
                        .foregroundStyle(AppTheme.Colors.danger)
                        .padding(.top, 14)
                }

                Spacer()
                    .frame(height: 290)
            }
        }
    }
}

#Preview {
    LoginView(viewModel: LoginViewModel(authService: MockAuthService())) { _ in }
}
