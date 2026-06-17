import SwiftUI

struct MainTabView: View {
@State private var selectedTab: MainTab = .home
    @State private var presentedAction: HomeAction?
    @State private var team: RunningTeam?
    @State private var isShowingMyPage = false
    @State private var isRunning = false
    @State private var isShowingInvite = false
    @State private var selectedTeamMemberDetail: TeamMemberSeasonDetail?
    @State private var homeTeamProgress: HomeTeamProgress?
    @State private var nickname: String
    @State private var currentUserID: String?
    @StateObject private var teamDashboardCache = TeamDashboardCache()
    @StateObject private var rankingCache = RankingCache()
    @StateObject private var historyCache = HistoryCache()
    private let teamService: TeamServiceProtocol
    private let profileService: ProfileServiceProtocol
    private let runService: RunServiceProtocol
    private let rankingService: RankingServiceProtocol
    private let authService: AuthServiceProtocol
    private let accessToken: String?
    private let onLogout: () -> Void
    private let store: LocalAppStateStore

    init(
        store: LocalAppStateStore,
        profileService: ProfileServiceProtocol = MockProfileService(),
        runService: RunServiceProtocol = MockRunService(),
        teamService: TeamServiceProtocol? = nil,
        rankingService: RankingServiceProtocol = MockRankingService(),
        authService: AuthServiceProtocol = MockAuthService(),
        accessToken: String? = nil,
        currentUserID: String? = nil,
        onLogout: @escaping () -> Void = {}
    ) {
        self.store = store
        self.profileService = profileService
        self.runService = runService
        self.rankingService = rankingService
        self.authService = authService
        self.accessToken = accessToken
        self.onLogout = onLogout
        self.teamService = teamService ?? MockTeamService(store: store)
        _nickname = State(initialValue: store.nickname)
        _team = State(initialValue: store.loadTeam())
        _currentUserID = State(initialValue: currentUserID)
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            Group {
                switch selectedTab {
                case .home:
                    HomeView(nickname: nickname, team: team, teamProgress: homeTeamProgress) {
                        presentedAction = .createTeam
                    } onJoinTeam: {
                        presentedAction = .joinTeam
                    } onOpenTeam: {
                        selectedTab = .team
                    } onOpenMyPage: {
                        isShowingMyPage = true
                    } onStartRunning: {
                        isRunning = true
                    }
                case .team:
                    TeamDashboardView(
                        team: team,
                        nickname: nickname,
                        teamService: teamService,
                        accessToken: accessToken,
                        currentUserID: currentUserID,
                        cache: teamDashboardCache,
                        onCreateTeam: {
                            presentedAction = .createTeam
                        },
                        onJoinTeam: {
                            presentedAction = .joinTeam
                        },
                        onInvite: {
                            isShowingInvite = true
                        },
                        onLeaveTeam: {
                            handleTeamLeft()
                        },
                        onSelectMember: { detail in
                            withAnimation(.easeOut(duration: 0.22)) {
                                selectedTeamMemberDetail = detail
                            }
                        }
                    )
                case .ranking:
                    RankingView(
                        rankingService: rankingService,
                        accessToken: accessToken,
                        team: team,
                        nickname: nickname,
                        cache: rankingCache
                    )
                case .history:
                    HistoryView(runService: runService, accessToken: accessToken, cache: historyCache)
                }
            }
            .padding(.bottom, AppTabBar.height)

            AppTabBar(selectedTab: $selectedTab)
                .ignoresSafeArea(.container, edges: .bottom)

            if let selectedTeamMemberDetail {
                Color.black.opacity(0.001)
                    .ignoresSafeArea()
                    .onTapGesture {
                        dismissTeamMemberDetail()
                    }
                    .zIndex(1)

                TeamMemberSeasonDetailSheet(detail: selectedTeamMemberDetail) {
                    dismissTeamMemberDetail()
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
                .zIndex(2)
            }
        }
        .ignoresSafeArea(.container, edges: .bottom)
        .runpamineFullScreenCover(item: $presentedAction) { action in
            switch action {
            case .createTeam:
                TeamCreateView(viewModel: TeamCreateViewModel(teamService: teamService, accessToken: accessToken)) { createdTeam in
                    handleTeamUpdated(createdTeam)
                }
                .networkErrorOverlay()
            case .joinTeam:
                TeamJoinView(viewModel: TeamJoinViewModel(teamService: teamService, accessToken: accessToken)) { joinedTeam in
                    handleTeamUpdated(joinedTeam)
                }
                .networkErrorOverlay()
            }
        }
        .sheet(isPresented: $isShowingMyPage) {
            MyPageView(
                store: store,
                profileService: profileService,
                authService: authService,
                accessToken: accessToken,
                onLogoutCompleted: onLogout
            ) { updatedNickname in
                nickname = updatedNickname
            }
            .presentationDetents([.large])
            .networkErrorOverlay()
        }
        .runpamineFullScreenCover(isPresented: $isRunning) {
            RunningView(runService: runService, accessToken: accessToken)
        }
        .runpamineFullScreenCover(isPresented: $isShowingInvite) {
            InviteMemberView(inviteCode: team?.inviteCode ?? "")
                .networkErrorOverlay()
        }
        .task(id: accessToken) {
            await refreshHomeState()
        }
    }

    private func handleTeamUpdated(_ updatedTeam: RunningTeam) {
        team = updatedTeam
        store.saveTeam(updatedTeam)
        homeTeamProgress = nil
        teamDashboardCache.clearDashboard()
        selectedTab = .team
        presentedAction = nil
    }

    private func handleTeamLeft() {
        team = nil
        homeTeamProgress = nil
        teamDashboardCache.clearDashboard()
        store.clearTeam()
        selectedTab = .team
        selectedTeamMemberDetail = nil
    }

    private func dismissTeamMemberDetail() {
        withAnimation(.easeInOut(duration: 0.18)) {
            selectedTeamMemberDetail = nil
        }
    }

    @MainActor
    private func refreshHomeState() async {
        guard let accessToken else { return }

        do {
            let homeState = try await profileService.fetchHomeState(accessToken: accessToken)

            if let profile = homeState.profile {
                currentUserID = profile.id
                nickname = profile.nickname
                store.saveNickname(profile.nickname)
            }

            if let runningTeam = homeState.team?.runningTeam {
                if team?.id != runningTeam.id {
                    teamDashboardCache.clearDashboard()
                }
                team = runningTeam
                store.saveTeam(runningTeam)
                await refreshHomeTeamProgress(for: runningTeam)
            }
        } catch {
            return
        }
    }

    @MainActor
    private func refreshHomeTeamProgress(for runningTeam: RunningTeam) async {
        guard let accessToken else {
            homeTeamProgress = nil
            return
        }

        do {
            let summary = try await teamService.fetchDailySummary(date: Date(), accessToken: accessToken)
            guard team?.id == runningTeam.id else { return }
            homeTeamProgress =
                HomeTeamProgress(
                    completedMemberCount: summary.completedMemberCount,
                    totalMemberCount: summary.totalMemberCount
                )
        } catch {
            homeTeamProgress = nil
        }
    }
}

enum MainTab {
    case home
    case team
    case ranking
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
