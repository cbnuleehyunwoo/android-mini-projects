import SwiftUI

struct AgreementRow: View {
    let title: String
    let isRequired: Bool
    let isAccepted: Bool
    var showsDisclosure = true
    let onToggle: () -> Void

    var body: some View {
        HStack(spacing: 14) {
            Button(action: onToggle) {
                Image(systemName: isAccepted ? "checkmark.square.fill" : "square.fill")
                    .font(.system(size: 27, weight: .semibold))
                    .foregroundStyle(isAccepted ? AppTheme.Colors.primary : AppTheme.Colors.border)
            }
            .accessibilityLabel("\(title) 동의")

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(AppTheme.Typography.font(size: 16, weight: .semibold))
                    .foregroundStyle(AppTheme.Colors.textPrimary)

                if isRequired {
                    Text("(필수)")
                        .font(AppTheme.Typography.body2)
                        .foregroundStyle(AppTheme.Colors.danger)
                }
            }

            Spacer()

            if showsDisclosure {
                Image(systemName: "chevron.right")
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundStyle(AppTheme.Colors.textSecondary.opacity(0.65))
            }
        }
    }
}
