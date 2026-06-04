import SwiftUI

struct AppTabBar: View {
    @Binding var selectedTab: MainTab

    var body: some View {
        HStack {
            tabButton(.home, title: "홈", icon: "house")
            Spacer()
            tabButton(.team, title: "팀", icon: "person.3")
            Spacer()
            tabButton(.history, title: "기록", icon: "clock.arrow.circlepath")
        }
        .padding(.horizontal, 30)
        .padding(.top, 10)
        .padding(.bottom, 8)
        .background(Color.white)
    }

    private func tabButton(_ tab: MainTab, title: String, icon: String) -> some View {
        Button {
            selectedTab = tab
        } label: {
            VStack(spacing: 3) {
                Image(systemName: icon)
                    .font(.system(size: 24, weight: .semibold))
                Text(title)
                    .font(AppTheme.Typography.font(size: 11, weight: .medium))
            }
            .foregroundStyle(selectedTab == tab ? AppTheme.Colors.primary : Color.gray)
            .frame(width: 56, height: 46)
        }
    }
}
