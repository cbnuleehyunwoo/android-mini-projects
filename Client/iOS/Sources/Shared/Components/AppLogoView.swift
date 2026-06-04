import SwiftUI

struct AppLogoView: View {
    var size: CGFloat = 88
    var rotation: Angle = .degrees(-6)

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: size * 0.22, style: .continuous)
                .fill(AppTheme.Colors.primary)
                .shadow(color: .black.opacity(0.14), radius: 14, x: 0, y: 12)

            Image(systemName: "figure.run")
                .font(.system(size: size * 0.45, weight: .bold))
                .foregroundStyle(.white)
        }
        .frame(width: size, height: size)
        .rotationEffect(rotation)
        .accessibilityHidden(true)
    }
}

#Preview {
    AppLogoView()
}
