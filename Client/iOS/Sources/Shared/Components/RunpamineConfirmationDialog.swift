import SwiftUI

enum RunpamineConfirmationDialogAppearance {
    case `default`
    case android
}

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
    var appearance: RunpamineConfirmationDialogAppearance = .default
    let onDismiss: () -> Void
    let onConfirm: () -> Void

    var body: some View {
        ZStack {
            Color.black.opacity(layout.dimOpacity)
                .ignoresSafeArea()
                .onTapGesture(perform: onDismiss)

            VStack(spacing: 0) {
                Text(title)
                    .font(AppTheme.Typography.font(size: 20, weight: .semibold))
                    .foregroundStyle(titleColor)

                Text(message)
                    .font(AppTheme.Typography.font(size: 16, weight: layout.messageWeight))
                    .foregroundStyle(messageColor)
                    .padding(.top, layout.messageTopPadding)
                    .multilineTextAlignment(layout.messageAlignment)

                HStack(spacing: layout.buttonSpacing) {
                    if confirmButtonWidthRatio == 0.5 {
                        dialogButton(title: dismissText, isPrimary: false, action: onDismiss)
                        dialogButton(title: confirmText, isPrimary: true, action: onConfirm)
                    } else {
                        GeometryReader { geometry in
                            let totalWidth = geometry.size.width
                            let spacing = layout.buttonSpacing
                            let availableWidth = totalWidth - spacing
                            HStack(spacing: spacing) {
                                dialogButton(title: dismissText, isPrimary: false, action: onDismiss)
                                    .frame(width: availableWidth * (1.0 - confirmButtonWidthRatio))
                                dialogButton(title: confirmText, isPrimary: true, action: onConfirm)
                                    .frame(width: availableWidth * confirmButtonWidthRatio)
                            }
                        }
                        .frame(height: layout.buttonHeight)
                    }
                }
                .padding(.top, layout.buttonTopPadding)
            }
            .padding(.horizontal, 24)
            .padding(.vertical, layout.verticalPadding)
            .frame(maxWidth: 432)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: layout.cornerRadius, style: .continuous))
            .shadow(color: .black.opacity(layout.shadowOpacity), radius: layout.shadowRadius, y: layout.shadowOffsetY)
            .padding(.horizontal, layout.horizontalPadding)
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
                .frame(height: layout.buttonHeight)
                .background(backgroundColor)
                .overlay {
                    if hasBorder {
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .stroke(AppTheme.Colors.primary, lineWidth: layout.dismissButtonBorderWidth)
                    }
                }
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        }
    }

    private var layout: Layout {
        switch appearance {
        case .default:
            Layout(
                dimOpacity: 0.38,
                verticalPadding: verticalPadding,
                messageTopPadding: messageTopPadding,
                buttonTopPadding: buttonTopPadding,
                horizontalPadding: 28,
                cornerRadius: 21,
                messageWeight: .regular,
                messageAlignment: .center,
                buttonHeight: 54,
                buttonSpacing: 16,
                dismissButtonBorderWidth: 1.5,
                shadowOpacity: 0,
                shadowRadius: 0,
                shadowOffsetY: 0
            )
        case .android:
            Layout(
                dimOpacity: 0.45,
                verticalPadding: 24,
                messageTopPadding: 12,
                buttonTopPadding: 24,
                horizontalPadding: 36,
                cornerRadius: 20,
                messageWeight: .medium,
                messageAlignment: .leading,
                buttonHeight: 40,
                buttonSpacing: 12,
                dismissButtonBorderWidth: 1,
                shadowOpacity: 0.24,
                shadowRadius: 6,
                shadowOffsetY: 3
            )
        }
    }

    private struct Layout {
        let dimOpacity: Double
        let verticalPadding: CGFloat
        let messageTopPadding: CGFloat
        let buttonTopPadding: CGFloat
        let horizontalPadding: CGFloat
        let cornerRadius: CGFloat
        let messageWeight: AppTheme.Typography.Weight
        let messageAlignment: TextAlignment
        let buttonHeight: CGFloat
        let buttonSpacing: CGFloat
        let dismissButtonBorderWidth: CGFloat
        let shadowOpacity: Double
        let shadowRadius: CGFloat
        let shadowOffsetY: CGFloat
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
