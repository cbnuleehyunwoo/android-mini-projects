import SwiftUI

struct TopNavigationBar: View {
    let title: String
    let onBack: () -> Void

    var body: some View {
        HStack {
            Button(action: onBack) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 23, weight: .semibold))
                    .foregroundStyle(AppTheme.Colors.textPrimary)
                    .frame(width: 44, height: 44)
                    .contentShape(Rectangle())
            }
            .accessibilityLabel("뒤로가기")

            Spacer()

            Text(title)
                .font(AppTheme.Typography.font(size: 19, weight: .bold))
                .foregroundStyle(AppTheme.Colors.textPrimary)

            Spacer()

            Color.clear
                .frame(width: 44, height: 44)
        }
        .frame(height: 60)
    }
}
