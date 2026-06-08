import SwiftUI

struct AppTabBar: View {
    static let height: CGFloat = 104

    @Binding var selectedTab: MainTab

    var body: some View {
        HStack {
            tabButton(.home, title: "홈", assetIcon: "icon_home")
            Spacer()
            tabButton(.team, title: "팀", assetIcon: "icon_team")
            Spacer()
            tabButton(.history, title: "기록", assetIcon: "icon_history")
        }
        .padding(.horizontal, 30)
        .padding(.top, 10)
        .padding(.bottom, 8)
        .background(Color.white.ignoresSafeArea(.container, edges: .bottom))
        .frame(height: Self.height)
    }

    private func tabButton(_ tab: MainTab, title: String, systemIcon: String? = nil, assetIcon: String? = nil) -> some View {
        Button {
            selectedTab = tab
        } label: {
            VStack(spacing: 12) {
                if let assetIcon {
                    Image(assetIcon)
                        .resizable()
                        .renderingMode(.template)
                        .scaledToFit()
                        .frame(width: 36, height: 36)
                } else if let systemIcon {
                    Image(systemName: systemIcon)
                        .font(.system(size: 36, weight: .semibold))
                }
                Text(title)
                    .font(AppTheme.Typography.font(size: 16, weight: .medium))
            }
            .foregroundStyle(selectedTab == tab ? AppTheme.Colors.primary : Color.gray)
            .frame(width: 74, height: 86)
        }
    }
}
