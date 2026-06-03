import SwiftUI

struct RunpamineLogoView: View {
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "figure.run")
                .font(.system(size: 72, weight: .bold))
                .foregroundStyle(.blue)
                .frame(width: 90, height: 90)
                .accessibilityLabel("런파민 로고")

            Text("Runpamine")
                .font(AppTheme.Typography.splashTitle)
                .foregroundStyle(AppTheme.Colors.textPrimary)
        }
    }
}

#Preview {
    RunpamineLogoView()
}
