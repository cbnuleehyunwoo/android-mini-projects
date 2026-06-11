import SwiftUI

struct GoogleLoginButton: View {
    var isLoading = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            ZStack {
                Image("icon_google_login")
                    .resizable()
                    .scaledToFit()

                if isLoading {
                    ProgressView()
                        .tint(AppTheme.Colors.textPrimary)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .background(Color.white.opacity(0.72))
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: LoginButtonLayout.height)
        }
        .frame(maxWidth: .infinity)
        .frame(height: LoginButtonLayout.height)
        .disabled(isLoading)
    }
}

enum LoginButtonLayout {
    static let height: CGFloat = 56
    static let cornerRadius: CGFloat = 8
}
