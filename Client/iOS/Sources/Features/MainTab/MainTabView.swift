import SwiftUI

struct MainTabView: View {
    @State private var selectedTab: MainTab = .home
    @State private var presentedAction: HomeAction?
    @State private var team: RunningTeam?
    @State private var isShowingMyPage = false
    @State private var isRunning = false
    @State private var nickname: String
    private let store: LocalAppStateStore

    init(store: LocalAppStateStore) {
        self.store = store
        _nickname = State(initialValue: store.nickname)
    }

    var body: some View {
        ZStack {
            Group {
                switch selectedTab {
                case .home:
                    HomeView(nickname: nickname, team: team) {
                        presentedAction = .createTeam
                    } onJoinTeam: {
                        presentedAction = .joinTeam
                    } onOpenMyPage: {
                        isShowingMyPage = true
                    } onStartRunning: {
                        isRunning = true
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
        .sheet(isPresented: $isShowingMyPage) {
            MyPageView(store: store) { updatedNickname in
                nickname = updatedNickname
            }
        }
        .runpamineFullScreenCover(isPresented: $isRunning) {
            RunningView()
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

    var id: Self { self }

    var title: String {
        switch self {
        case .createTeam:
            return "팀 생성"
        case .joinTeam:
            return "팀 참가"
        }
    }
}

private extension View {
    @ViewBuilder
    func runpamineFullScreenCover<Content: View>(
        isPresented: Binding<Bool>,
        @ViewBuilder content: @escaping () -> Content
    ) -> some View {
        #if os(iOS)
        fullScreenCover(isPresented: isPresented, content: content)
        #else
        sheet(isPresented: isPresented, content: content)
        #endif
    }
}

#Preview {
    MainTabView(store: LocalAppStateStore(defaults: .standard))
}
