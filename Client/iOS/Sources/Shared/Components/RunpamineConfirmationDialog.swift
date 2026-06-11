import SwiftUI

struct RunpamineConfirmationDialog: View {
    let title: String
    let message: String
    let dismissText: String
    let confirmText: String
    let onDismiss: () -> Void
    let onConfirm: () -> Void

    var body: some View {
        ZStack {
            Color.black.opacity(0.38)
                .ignoresSafeArea()
                .onTapGesture(perform: onDismiss)

            VStack(spacing: 0) {
                Text(title)
                    .font(AppTheme.Typography.font(size: 24, weight: .semibold))
                    .foregroundStyle(AppTheme.Colors.primary)

                Text(message)
                    .font(AppTheme.Typography.font(size: 20, weight: .regular))
                    .foregroundStyle(.black)
                    .padding(.top, 20)

                HStack(spacing: 16) {
                    dialogButton(title: dismissText, isPrimary: false, action: onDismiss)
                    dialogButton(title: confirmText, isPrimary: true, action: onConfirm)
                }
                .padding(.top, 36)
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 30)
            .frame(maxWidth: 432)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
            .padding(.horizontal, 28)
        }
        .transition(.opacity)
        .zIndex(20)
    }

    private func dialogButton(title: String, isPrimary: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(AppTheme.Typography.font(size: 16, weight: .bold))
                .foregroundStyle(isPrimary ? .white : AppTheme.Colors.primary)
                .frame(maxWidth: .infinity)
                .frame(height: 54)
                .background(isPrimary ? AppTheme.Colors.primary : Color.white)
                .overlay {
                    RoundedRectangle(cornerRadius: 12, style: .continuous)
                        .stroke(AppTheme.Colors.primary, lineWidth: isPrimary ? 0 : 1.5)
                }
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        }
    }
}

#Preview {
    RunpamineConfirmationDialog(
        title: "러닝 시작",
        message: "러닝을 시작하시겠습니까?",
        dismissText: "취소",
        confirmText: "시작",
        onDismiss: {},
        onConfirm: {}
    )
}
