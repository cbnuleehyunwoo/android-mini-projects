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

            VStack(spacing: 0) {
                VStack(spacing: 34) {
                    Image("character_no_bg")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 200, height: 200)
                        .accessibilityLabel("런파민 캐릭터")

                    Text("RUNPAMINE")
                        .font(AppTheme.Typography.loginTitle)
                        .foregroundStyle(AppTheme.Colors.textPrimary)
                        .minimumScaleFactor(0.72)
                }
                .padding(.bottom, 50)

                VStack(spacing: 12) {
                    GoogleLoginButton(isLoading: viewModel.isLoading) {
                        Task {
                            await viewModel.loginWithGoogle()
                            completeLoginIfNeeded()
                        }
                    }

                    AppleLoginButton(
                        isLoading: viewModel.isLoading,
                        onRequest: viewModel.configureAppleLoginRequest,
                        onCompletion: { result in
                            Task {
                                await viewModel.loginWithApple(result: result)
                                completeLoginIfNeeded()
                            }
                        }
                    )
                }
                .padding(.horizontal, AppTheme.Layout.horizontalPadding)

                if let message = viewModel.errorMessage {
                    Text(message)
                        .font(AppTheme.Typography.body2)
                        .foregroundStyle(AppTheme.Colors.danger)
                        .padding(.top, 14)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
        }
    }

    private func completeLoginIfNeeded() {
        if let session = viewModel.session {
            onLoginCompleted(session)
        }
    }
}

#Preview {
    LoginView(viewModel: LoginViewModel(authService: MockAuthService())) { _ in }
}
