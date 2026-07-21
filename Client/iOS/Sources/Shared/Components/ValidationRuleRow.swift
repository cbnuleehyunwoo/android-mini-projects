import SwiftUI

struct ValidationRuleRow: View {
    let text: String
    let isValid: Bool

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: isValid ? "checkmark" : "xmark")
                .font(.system(size: 13, weight: .bold))
                .frame(width: 16)
                .foregroundStyle(isValid ? AppTheme.Colors.success : AppTheme.Colors.danger)

            Text(text)
                .font(AppTheme.Typography.body1)
                .foregroundStyle(isValid ? AppTheme.Colors.success : AppTheme.Colors.danger)
        }
    }
}
