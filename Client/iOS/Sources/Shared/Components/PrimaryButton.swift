import SwiftUI

struct PrimaryButton: View {
    let title: String
    var isLoading = false
    var isDisabled = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            ZStack {
                if isLoading {
                    ProgressView()
                        .tint(.white)
                } else {
                    Text(title)
                        .font(AppTheme.Typography.button)
                }
            }
            .foregroundStyle(.white)
            .frame(maxWidth: .infinity)
            .frame(height: AppTheme.Layout.buttonHeight)
            .background(isDisabled ? AppTheme.Colors.primary.opacity(0.45) : AppTheme.Colors.primary)
            .clipShape(RoundedRectangle(cornerRadius: AppTheme.Layout.cornerRadius, style: .continuous))
        }
        .disabled(isDisabled || isLoading)
    }
}
