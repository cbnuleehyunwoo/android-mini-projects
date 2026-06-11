import SwiftUI

struct AppTabBar: View {
    static let height: CGFloat = 75

    @Binding var selectedTab: MainTab

    var body: some View {
        HStack {
            tabButton(.home, title: "홈", assetIcon: "icon_home")
            Spacer()
            tabButton(.team, title: "팀", assetIcon: "icon_team")
            Spacer()
            tabButton(.ranking, title: "랭킹", assetIcon: "icon_rank")
            Spacer()
            tabButton(.history, title: "기록", assetIcon: "icon_history")
        }
        .padding(.horizontal, 30)
        .padding(.top, 10)
        .padding(.bottom, 8)
        .background(Color.white.ignoresSafeArea(.container, edges: .bottom))
        .frame(height: Self.height)
        .overlay(alignment: .top) {
            Rectangle()
                .fill(AppTheme.Colors.border.opacity(0.6))
                .frame(height: 1)
        }
    }

    private func tabButton(_ tab: MainTab, title: String, systemIcon: String? = nil, assetIcon: String? = nil) -> some View {
        Button {
            selectedTab = tab
        } label: {
            VStack(spacing: 8) {
                if let assetIcon {
                    Image(assetIcon)
                        .resizable()
                        .renderingMode(.template)
                        .scaledToFit()
                        .frame(width: 24, height: 24)
                } else if let systemIcon {
                    Image(systemName: systemIcon)
                        .font(.system(size: 24, weight: .semibold))
                }
                Text(title)
                    .font(AppTheme.Typography.font(size: 16, weight: .medium))
            }
            .foregroundStyle(selectedTab == tab ? AppTheme.Colors.primary : Color.gray)
            .frame(width: 74, height: 30)
        }
    }
}
