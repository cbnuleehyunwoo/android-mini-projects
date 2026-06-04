import SwiftUI

struct MapPlaceholderView: View {
    var body: some View {
        ZStack {
            Color(red: 0.90, green: 0.93, blue: 0.96)

            GeometryReader { proxy in
                let width = proxy.size.width
                let height = proxy.size.height

                ForEach(0..<6) { index in
                    Rectangle()
                        .fill(Color.white)
                        .frame(width: 8)
                        .frame(height: height)
                        .offset(x: CGFloat(index) * width / 5)
                }

                ForEach(0..<7) { index in
                    Rectangle()
                        .fill(Color.white)
                        .frame(width: width)
                        .frame(height: 8)
                        .offset(y: CGFloat(index) * height / 6)
                }

                Path { path in
                    path.move(to: CGPoint(x: 0, y: height * 0.43))
                    path.addCurve(
                        to: CGPoint(x: width, y: height * 0.42),
                        control1: CGPoint(x: width * 0.25, y: height * 0.40),
                        control2: CGPoint(x: width * 0.55, y: height * 0.47)
                    )
                }
                .stroke(Color(red: 0.65, green: 0.80, blue: 0.94), lineWidth: 5)

                RoundedRectangle(cornerRadius: 4)
                    .fill(Color(red: 0.72, green: 0.88, blue: 0.72).opacity(0.7))
                    .frame(width: 62, height: 48)
                    .position(x: width * 0.55, y: height * 0.22)

                MapPinView()
                    .position(x: width * 0.50, y: height * 0.24)
            }
        }
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
    }
}

private struct MapPinView: View {
    var body: some View {
        ZStack {
            Circle()
                .fill(AppTheme.Colors.primary.opacity(0.18))
                .frame(width: 68, height: 68)
            Circle()
                .fill(AppTheme.Colors.primary.opacity(0.22))
                .frame(width: 44, height: 44)
            Image(systemName: "mappin.circle.fill")
                .font(.system(size: 34, weight: .bold))
                .foregroundStyle(AppTheme.Colors.primary)
        }
    }
}
