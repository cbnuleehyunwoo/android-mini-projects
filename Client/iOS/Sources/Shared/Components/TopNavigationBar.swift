import SwiftUI

struct TopNavigationBar: View {
    let title: String
    let onBack: () -> Void

    var body: some View {
        HStack {
            Button(action: onBack) {
                Image("icon_back")
                    .resizable()
                    .renderingMode(.template)
                    .scaledToFit()
                    .foregroundStyle(AppTheme.Colors.textPrimary)
                    .frame(width: 24, height: 24)
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

extension View {
    @ViewBuilder
    func runpamineBackSwipe(onBack: @escaping () -> Void) -> some View {
        #if os(iOS)
        overlay(alignment: .leading) {
            Color.clear
                .frame(width: 32)
                .contentShape(Rectangle())
                .gesture(
                    DragGesture(minimumDistance: 20, coordinateSpace: .global)
                        .onEnded { value in
                            guard value.translation.width > 70,
                                  abs(value.translation.height) < 80 else { return }
                            onBack()
                        }
                )
        }
        #else
        self
        #endif
    }
}
