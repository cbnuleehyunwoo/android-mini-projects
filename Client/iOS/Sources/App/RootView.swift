import SwiftUI

struct RootView: View {
    @State private var route: OnboardingRoute = .splash
    @State private var acceptedTerms: [TermsAgreement] = []
    @State private var currentSession: AuthSession?
    private let authService: AuthServiceProtocol
    private let profileService: ProfileServiceProtocol
    private let runService: RunServiceProtocol
    private let teamService: TeamServiceProtocol
    private let rankingService: RankingServiceProtocol
    private let store: LocalAppStateStore

    init(
        authService: AuthServiceProtocol,
        profileService: ProfileServiceProtocol = MockProfileService(),
        runService: RunServiceProtocol = MockRunService(),
        teamService: TeamServiceProtocol = MockTeamService(),
        rankingService: RankingServiceProtocol = MockRankingService(),
        store: LocalAppStateStore
    ) {
        self.authService = authService
        self.profileService = profileService
        self.runService = runService
        self.teamService = teamService
        self.rankingService = rankingService
        self.store = store
    }

    var body: some View {
        Group {
            switch route {
            case .splash:
                SplashView {
                    Task {
                        await handleSplashCompleted()
                    }
                }
            case .login:
                LoginView(viewModel: LoginViewModel(authService: authService)) { session in
                    Task {
                        await handleLoginCompleted(session)
                    }
                }
            case .terms:
                TermsAgreementView(viewModel: TermsAgreementViewModel()) {
                    route = .login
                } onComplete: { agreements in
                    acceptedTerms = agreements
                    route = .nickname
                }
            case .nickname:
                NicknameSetupView(
                    viewModel: NicknameSetupViewModel(
                        authService: authService,
                        profileService: profileService,
                        accessToken: currentSession?.accessToken,
                        agreements: acceptedTerms
                    )
                ) {
                    route = .terms
                } onComplete: {
                    route = .main
                }
            case .main:
                MainTabView(
                    store: store,
                    profileService: profileService,
                    runService: runService,
                    teamService: teamService,
                    rankingService: rankingService,
                    authService: authService,
                    accessToken: currentSession?.accessToken,
                    onLogout: handleLogoutCompleted
                )
            }
        }
        .tint(AppTheme.Colors.primary)
    }

    @MainActor
    private func handleSplashCompleted() async {
        do {
            if let session = try await authService.restoreSession() {
                await handleLoginCompleted(session)
                return
            }
        } catch {
            currentSession = nil
        }

        withAnimation(.easeInOut(duration: 0.3)) {
            route = .login
        }
    }

    @MainActor
    private func handleLoginCompleted(_ session: AuthSession) async {
        currentSession = session

        do {
            let homeState = try await profileService.fetchHomeState(accessToken: session.accessToken)
            apply(homeState)
            route = homeState.profile == nil ? .terms : .main
        } catch {
            route = session.needsSignup ? .terms : .main
        }
    }

    @MainActor
    private func handleLogoutCompleted() {
        currentSession = nil
        acceptedTerms = []
        route = .login
    }

    private func apply(_ homeState: HomeState) {
        if let profile = homeState.profile {
            store.saveNickname(profile.nickname)
        }

        if let team = homeState.team?.runningTeam {
            store.saveTeam(team)
        }
    }
}

private enum OnboardingRoute {
    case splash
    case login
    case terms
    case nickname
    case main
}
