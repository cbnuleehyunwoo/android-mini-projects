import SwiftUI

struct MainTabView: View {
    @State private var selectedTab: MainTab = .home
    @State private var presentedAction: HomeAction?
    @State private var team: RunningTeam?
    @State private var isShowingMyPage = false
    @State private var isRunning = false
    @State private var isShowingInvite = false
    @State private var nickname: String
    private let teamService: TeamServiceProtocol
    private let profileService: ProfileServiceProtocol
    private let accessToken: String?
    private let store: LocalAppStateStore

    init(
        store: LocalAppStateStore,
        profileService: ProfileServiceProtocol = MockProfileService(),
        accessToken: String? = nil
    ) {
        self.store = store
        self.profileService = profileService
        self.accessToken = accessToken
        teamService = MockTeamService(store: store)
        _nickname = State(initialValue: store.nickname)
        _team = State(initialValue: store.loadTeam())
    }

    var body: some View {
        ZStack(alignment: .bottom) {
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
                case .team:
                    TeamDashboardView(
                        team: team,
                        nickname: nickname,
                        onCreateTeam: {
                            presentedAction = .createTeam
                        },
                        onJoinTeam: {
                            presentedAction = .joinTeam
                        },
                        onInvite: {
                            isShowingInvite = true
                        }
                    )
                case .history:
                    HistoryView()
                }
            }
            .padding(.bottom, AppTabBar.height)

            AppTabBar(selectedTab: $selectedTab)
                .ignoresSafeArea(.container, edges: .bottom)
        }
        .ignoresSafeArea(.container, edges: .bottom)
        .runpamineFullScreenCover(item: $presentedAction) { action in
            switch action {
            case .createTeam:
                TeamCreateView(viewModel: TeamCreateViewModel(teamService: teamService)) { createdTeam in
                    handleTeamUpdated(createdTeam)
                }
            case .joinTeam:
                TeamJoinView(viewModel: TeamJoinViewModel(teamService: teamService)) { joinedTeam in
                    handleTeamUpdated(joinedTeam)
                }
            }
        }
        .sheet(isPresented: $isShowingMyPage) {
            MyPageView(store: store, profileService: profileService, accessToken: accessToken) { updatedNickname in
                nickname = updatedNickname
            }
        }
        .runpamineFullScreenCover(isPresented: $isRunning) {
            RunningView()
        }
        .runpamineFullScreenCover(isPresented: $isShowingInvite) {
            InviteMemberView(inviteCode: team?.inviteCode ?? "")
        }
        .task {
            await refreshHomeState()
        }
    }

    private func handleTeamUpdated(_ updatedTeam: RunningTeam) {
        team = updatedTeam
        selectedTab = .team
        presentedAction = nil
    }

    @MainActor
    private func refreshHomeState() async {
        guard let accessToken else { return }

        do {
            let homeState = try await profileService.fetchHomeState(accessToken: accessToken)

            if let profile = homeState.profile {
                nickname = profile.nickname
                store.saveNickname(profile.nickname)
            }

            if let runningTeam = homeState.team?.runningTeam {
                team = runningTeam
                store.saveTeam(runningTeam)
            }
        } catch {
            return
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

    @ViewBuilder
    func runpamineFullScreenCover<Item: Identifiable, Content: View>(
        item: Binding<Item?>,
        @ViewBuilder content: @escaping (Item) -> Content
    ) -> some View {
        #if os(iOS)
        fullScreenCover(item: item, content: content)
        #else
        sheet(item: item, content: content)
        #endif
    }
}

#Preview {
    MainTabView(store: LocalAppStateStore(defaults: .standard))
}
