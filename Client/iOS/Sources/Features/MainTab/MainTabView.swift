import SwiftUI

struct MainTabView: View {
    @Environment(\.scenePhase) private var scenePhase
    @EnvironmentObject private var networkMonitor: NetworkMonitor
    @State private var selectedTab: MainTab = .home
    @State private var presentedAction: HomeAction?
    @State private var team: RunningTeam?
    @State private var isShowingMyPage = false
    @State private var isRunning = false
    @State private var isShowingInvite = false
    @State private var runDataRefreshRevision = 0
    @State private var selectedTeamMemberDetail: TeamMemberStatsDetail?
    @State private var selectedHistoryRecord: RunningRecord?
    @State private var homeTeamProgress: HomeTeamProgress?
    @State private var nickname: String
    @State private var currentUserID: String?
    @StateObject private var teamDashboardCache = TeamDashboardCache()
    @StateObject private var rankingCache = RankingCache()
    @StateObject private var historyCache = HistoryCache()
    private let teamService: TeamServiceProtocol
    private let profileService: ProfileServiceProtocol
    private let runService: RunServiceProtocol
    private let runUploadRetrier: RunningUploadRetrier
    private let rankingService: RankingServiceProtocol
    private let authService: AuthServiceProtocol
    private let accessToken: String?
    private let historyStore: RunningHistoryStore
    private let onLogout: () -> Void
    private let store: LocalAppStateStore

    init(
        store: LocalAppStateStore,
        profileService: ProfileServiceProtocol = MockProfileService(),
        runService: RunServiceProtocol = MockRunService(),
        runUploadRetrier: RunningUploadRetrier? = nil,
        teamService: TeamServiceProtocol? = nil,
        rankingService: RankingServiceProtocol = MockRankingService(),
        authService: AuthServiceProtocol = MockAuthService(),
        historyStore: RunningHistoryStore = RunningHistoryStore(),
        accessToken: String? = nil,
        currentUserID: String? = nil,
        onLogout: @escaping () -> Void = {}
    ) {
        self.store = store
        self.profileService = profileService
        self.runService = runService
        self.runUploadRetrier = runUploadRetrier ?? RunningUploadRetrier(store: historyStore, runService: runService)
        self.rankingService = rankingService
        self.authService = authService
        self.historyStore = historyStore
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
                        refreshRevision: runDataRefreshRevision,
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
                        currentUserID: currentUserID,
                        cache: rankingCache,
                        refreshRevision: runDataRefreshRevision
                    )
                case .history:
                    HistoryView(
                        runService: runService,
                        historyStore: historyStore,
                        accessToken: accessToken,
                        currentUserID: currentUserID,
                        cache: historyCache,
                        refreshRevision: runDataRefreshRevision,
                        onRetryPendingRuns: retryPendingRuns,
                        onOpenRecord: { record in
                            selectedHistoryRecord = record
                        }
                    )
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

                TeamMemberStatsDetailSheet(detail: selectedTeamMemberDetail) {
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
                TeamCreateView(
                    viewModel: TeamCreateViewModel(teamService: teamService, accessToken: accessToken),
                    onDismiss: { presentedAction = nil }
                ) { createdTeam in
                    handleTeamUpdated(createdTeam)
                }
                .networkErrorOverlay()
            case .joinTeam:
                TeamJoinView(
                    viewModel: TeamJoinViewModel(teamService: teamService, accessToken: accessToken),
                    onDismiss: { presentedAction = nil }
                ) { joinedTeam in
                    handleTeamUpdated(joinedTeam)
                }
                .networkErrorOverlay()
            }
        }
        .sheet(isPresented: $isShowingMyPage) {
            MyPageView(
                store: store,
                historyStore: historyStore,
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
            RunningView(
                runService: runService,
                runUploadRetrier: runUploadRetrier,
                historyStore: historyStore,
                accessToken: accessToken,
                currentUserID: currentUserID,
                onRunSaved: handleRunSaved,
                onDismiss: { isRunning = false }
            )
        }
        .runpamineFullScreenCover(isPresented: $isShowingInvite) {
            InviteMemberView(inviteCode: team?.inviteCode ?? "", onDismiss: { isShowingInvite = false })
                .networkErrorOverlay()
        }
        .runpamineFullScreenCover(item: $selectedHistoryRecord) { record in
            RunningSummaryView(record: record) {
                selectedHistoryRecord = nil
            }
            .runpamineBackSwipe {
                selectedHistoryRecord = nil
            }
            .networkErrorOverlay()
        }
        .task(id: accessToken) {
            await retryPendingRuns()
            await refreshHomeState()
        }
        .onChange(of: selectedTab) { _, nextTab in
            guard nextTab == .history else { return }
            Task {
                await retryPendingRuns()
            }
        }
        .onChange(of: scenePhase) { _, nextPhase in
            guard nextPhase == .active else { return }
            Task {
                await retryPendingRuns()
            }
        }
        .onChange(of: networkMonitor.isConnected) { _, isConnected in
            guard isConnected else { return }
            Task {
                await retryPendingRuns()
            }
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

    private func handleRunSaved() {
        teamDashboardCache.clearDashboard()
        rankingCache.invalidate()
        runDataRefreshRevision &+= 1

        Task {
            await refreshHomeState()
        }
    }

    private func retryPendingRuns() async {
        guard let accessToken else { return }
        let uploadedIDs = await runUploadRetrier.uploadPending(accessToken: accessToken, currentUserID: currentUserID)
        guard !uploadedIDs.isEmpty else { return }
        await MainActor.run {
            teamDashboardCache.clearDashboard()
            rankingCache.invalidate()
            runDataRefreshRevision &+= 1
        }
        await refreshHomeState()
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

            if let teamSummary = homeState.team, let runningTeam = teamSummary.runningTeam {
                if team?.id != runningTeam.id {
                    teamDashboardCache.clearDashboard()
                }
                team = runningTeam
                homeTeamProgress = HomeTeamProgress(
                    completedMemberCount: teamSummary.todayRunMemberCount,
                    totalMemberCount: teamSummary.memberCount
                )
                store.saveTeam(runningTeam)
            } else {
                team = nil
                homeTeamProgress = nil
                teamDashboardCache.clearDashboard()
                store.clearTeam()
            }
        } catch {
            return
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
        ZStack {
            self
            if isPresented.wrappedValue {
                content()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color(uiColor: .systemBackground).ignoresSafeArea())
                    .transition(.move(edge: .trailing))
                    .zIndex(100)
            }
        }
        .animation(.easeInOut(duration: 0.3), value: isPresented.wrappedValue)
    }

    @ViewBuilder
    func runpamineFullScreenCover<Item: Identifiable, Content: View>(
        item: Binding<Item?>,
        @ViewBuilder content: @escaping (Item) -> Content
    ) -> some View {
        ZStack {
            self
            if let currentItem = item.wrappedValue {
                content(currentItem)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color(uiColor: .systemBackground).ignoresSafeArea())
                    .transition(.move(edge: .trailing))
                    .zIndex(100)
            }
        }
        .animation(.easeInOut(duration: 0.3), value: item.wrappedValue?.id)
    }
}

#Preview {
    MainTabView(store: LocalAppStateStore(defaults: .standard))
        .environmentObject(NetworkMonitor())
}
