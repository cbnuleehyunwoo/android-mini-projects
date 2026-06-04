import SwiftUI

struct KakaoLoginButton: View {
    var isLoading = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 12) {
                if isLoading {
                    ProgressView()
                        .tint(AppTheme.Colors.kakaoText)
                } else {
                    Image(systemName: "message.fill")
                        .font(.system(size: 22, weight: .semibold))
                    Text("카카오로 시작하기")
                        .font(AppTheme.Typography.font(size: 16, weight: .bold))
                }
            }
            .foregroundStyle(AppTheme.Colors.kakaoText)
            .frame(maxWidth: .infinity)
            .frame(height: AppTheme.Layout.buttonHeight)
            .background(AppTheme.Colors.kakao)
            .clipShape(RoundedRectangle(cornerRadius: AppTheme.Layout.cornerRadius, style: .continuous))
        }
        .disabled(isLoading)
    }
}
