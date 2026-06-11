import AuthenticationServices
import SwiftUI

struct AppleLoginButton: View {
    var isLoading = false
    let onRequest: (ASAuthorizationAppleIDRequest) -> Void
    let onCompletion: (Result<ASAuthorization, Error>) -> Void

    var body: some View {
        ZStack {
            SignInWithAppleButton(.signIn, onRequest: onRequest, onCompletion: onCompletion)
                .signInWithAppleButtonStyle(.black)
                .frame(maxWidth: .infinity)
                .frame(height: LoginButtonLayout.height)
                .cornerRadius(LoginButtonLayout.cornerRadius)

            if isLoading {
                ProgressView()
                    .tint(.white)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.black.opacity(0.72))
            }
        }
        .frame(maxWidth: .infinity)
        .frame(height: LoginButtonLayout.height)
        .clipShape(RoundedRectangle(cornerRadius: LoginButtonLayout.cornerRadius, style: .continuous))
        .disabled(isLoading)
    }
}

#Preview {
    AppleLoginButton(isLoading: false, onRequest: { _ in }, onCompletion: { _ in })
        .padding()
}
