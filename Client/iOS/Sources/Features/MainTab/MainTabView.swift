import SwiftUI

struct MainTabView: View {
    @State private var selectedTab: MainTab = .home
    @State private var presentedAction: HomeAction?
    @State private var team: RunningTeam?

    var body: some View {
        ZStack {
            Group {
                switch selectedTab {
                case .home:
                    HomeView(nickname: "러너", team: team) {
                        presentedAction = .createTeam
                    } onJoinTeam: {
                        presentedAction = .joinTeam
                    } onOpenMyPage: {
                        presentedAction = .myPage
                    } onStartRunning: {
                        presentedAction = .running
                    }
                case .team, .history:
                    Color.white
                }
            }
            .safeAreaInset(edge: .bottom) {
                AppTabBar(selectedTab: $selectedTab)
            }
        }
        .sheet(item: $presentedAction) { action in
            Text(action.title)
                .font(AppTheme.Typography.font(size: 20, weight: .bold))
                .presentationDetents([.medium])
        }
    }
}

enum MainTab {
    case home
    case team
    case history
}

private enum HomeAction: Identifiable {
    case createTeam
    case joinTeam
    case myPage
    case running

    var id: Self { self }

    var title: String {
        switch self {
        case .createTeam:
            return "팀 생성"
        case .joinTeam:
            return "팀 참가"
        case .myPage:
            return "마이페이지"
        case .running:
            return "러닝 시작"
        }
    }
}

#Preview {
    MainTabView()
}
