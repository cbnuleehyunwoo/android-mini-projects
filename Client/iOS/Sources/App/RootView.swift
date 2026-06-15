import Network
import SwiftUI
import UIKit

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
                    routeToMainOrOnboarding()
                }
            case .onboarding:
                OnboardingView {
                    store.markOnboardingCompleted()
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
            route = homeState.profile == nil ? .terms : mainOrOnboardingRoute()
        } catch {
            route = session.needsSignup ? .terms : mainOrOnboardingRoute()
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

    private func routeToMainOrOnboarding() {
        route = mainOrOnboardingRoute()
    }

    private func mainOrOnboardingRoute() -> OnboardingRoute {
        store.hasCompletedOnboarding ? .main : .onboarding
    }
}

private enum OnboardingRoute {
    case splash
    case login
    case terms
    case nickname
    case onboarding
    case main
}

final class NetworkMonitor: ObservableObject {
    @Published private(set) var isConnected = false
    @Published private(set) var isStatusKnown = false
    @Published private(set) var hasConnectedOnce = false

    private let monitor = NWPathMonitor()
    private let queue = DispatchQueue(label: "runpamine.network-monitor")

    init() {
        monitor.pathUpdateHandler = { [weak self] path in
            DispatchQueue.main.async {
                guard let self else { return }
                self.isConnected = path.status == .satisfied
                self.isStatusKnown = true
                if self.isConnected {
                    self.hasConnectedOnce = true
                }
            }
        }
        monitor.start(queue: queue)
    }

    deinit {
        monitor.cancel()
    }
}

struct NetworkErrorView: View {
    var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()

            VStack(spacing: 8) {
                errorImage
                    .resizable()
                    .scaledToFit()
                    .frame(width: 220, height: 220)

                Text("네트워크 연결을 확인해주세요.")
                    .font(AppTheme.Typography.body1)
                    .foregroundStyle(Color.gray)
            }
        }
    }

    private var errorImage: Image {
        guard let imageURL = Bundle.main.url(forResource: "error", withExtension: "png"),
              let image = UIImage(contentsOfFile: imageURL.path)
        else {
            return Image(systemName: "wifi.slash")
        }
        return Image(uiImage: image)
    }
}

private struct NetworkErrorOverlayModifier: ViewModifier {
    @EnvironmentObject private var networkMonitor: NetworkMonitor

    func body(content: Content) -> some View {
        ZStack {
            if networkMonitor.hasConnectedOnce {
                content
            }

            if networkMonitor.isStatusKnown && !networkMonitor.isConnected {
                NetworkErrorView()
                    .zIndex(1)
            }
        }
    }
}

extension View {
    func networkErrorOverlay() -> some View {
        modifier(NetworkErrorOverlayModifier())
    }
}
