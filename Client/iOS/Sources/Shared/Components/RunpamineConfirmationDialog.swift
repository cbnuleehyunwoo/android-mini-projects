import SwiftUI

struct RunpamineConfirmationDialog: View {
    let title: String
    let message: String
    let dismissText: String
    let confirmText: String
    var confirmButtonColor: Color = AppTheme.Colors.primary
    var dismissButtonColor: Color? = nil
    var dismissButtonTextColor: Color? = nil
    var titleColor: Color = AppTheme.Colors.primary
    var messageColor: Color = .black
    var confirmButtonWidthRatio: Double = 0.5
    var verticalPadding: CGFloat = 30
    var messageTopPadding: CGFloat = 20
    var buttonTopPadding: CGFloat = 36
    let onDismiss: () -> Void
    let onConfirm: () -> Void

    var body: some View {
        ZStack {
            Color.black.opacity(0.38)
                .ignoresSafeArea()
                .onTapGesture(perform: onDismiss)

            VStack(spacing: 0) {
                Text(title)
                    .font(AppTheme.Typography.font(size: 20, weight: .semibold))
                    .foregroundStyle(titleColor)

                Text(message)
                    .font(AppTheme.Typography.font(size: 16, weight: .regular))
                    .foregroundStyle(messageColor)
                    .padding(.top, messageTopPadding)
                    .multilineTextAlignment(.center)

                HStack(spacing: 16) {
                    if confirmButtonWidthRatio == 0.5 {
                        dialogButton(title: dismissText, isPrimary: false, action: onDismiss)
                        dialogButton(title: confirmText, isPrimary: true, action: onConfirm)
                    } else {
                        GeometryReader { geometry in
                            let totalWidth = geometry.size.width
                            let spacing: CGFloat = 16
                            let availableWidth = totalWidth - spacing
                            HStack(spacing: spacing) {
                                dialogButton(title: dismissText, isPrimary: false, action: onDismiss)
                                    .frame(width: availableWidth * (1.0 - confirmButtonWidthRatio))
                                dialogButton(title: confirmText, isPrimary: true, action: onConfirm)
                                    .frame(width: availableWidth * confirmButtonWidthRatio)
                            }
                        }
                        .frame(height: 54)
                    }
                }
                .padding(.top, buttonTopPadding)
            }
            .padding(.horizontal, 24)
            .padding(.vertical, verticalPadding)
            .frame(maxWidth: 432)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 21, style: .continuous))
            .padding(.horizontal, 28)
        }
        .transition(.opacity)
        .zIndex(20)
    }

    private func dialogButton(title: String, isPrimary: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            let textColor: Color = {
                if isPrimary {
                    return .white
                } else {
                    return dismissButtonTextColor ?? AppTheme.Colors.primary
                }
            }()
            
            let backgroundColor: Color = {
                if isPrimary {
                    return confirmButtonColor
                } else {
                    return dismissButtonColor ?? Color.white
                }
            }()
            
            let hasBorder = !isPrimary && dismissButtonColor == nil
            
            Text(title)
                .font(AppTheme.Typography.font(size: 16, weight: .semibold))
                .foregroundStyle(textColor)
                .frame(maxWidth: .infinity)
                .frame(height: 54)
                .background(backgroundColor)
                .overlay {
                    if hasBorder {
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .stroke(AppTheme.Colors.primary, lineWidth: 1.5)
                    }
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
