import SwiftUI

struct RunpamineLogoView: View {
    var body: some View {
        VStack(spacing: 34) {
            Image("AppLogo")
                .resizable()
                .scaledToFit()
                .frame(width: 109, height: 109)
                .accessibilityLabel("런파민 로고")

            Text("RUNPAMINE")
                .font(AppTheme.Typography.splashTitle)
                .foregroundStyle(AppTheme.Colors.textPrimary)
                .minimumScaleFactor(0.7)
        }
    }
}

#Preview {
    RunpamineLogoView()
}
