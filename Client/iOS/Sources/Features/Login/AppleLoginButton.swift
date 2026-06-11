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

            if isLoading {
                ProgressView()
                    .tint(.white)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.black.opacity(0.72))
            }
        }
        .frame(maxWidth: .infinity)
        .frame(height: 56)
        .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
        .disabled(isLoading)
    }
}

#Preview {
    AppleLoginButton(isLoading: false, onRequest: { _ in }, onCompletion: { _ in })
        .padding()
}
