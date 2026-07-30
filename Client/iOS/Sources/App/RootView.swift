import Network
import SwiftUI
import UIKit

struct RootView: View {
    @Environment(\.openURL) private var openURL
    @Environment(\.scenePhase) private var scenePhase
    @EnvironmentObject private var networkMonitor: NetworkMonitor
    @State private var route: OnboardingRoute = .splash
    @State private var acceptedTerms: [TermsAgreement] = []
    @State private var currentSession: AuthSession?
    @State private var currentProfileID: String?
    @State private var isResolvingProfile = false
    @State private var availableUpdate: AppUpdate?
    @State private var dismissedUpdateVersion: String?
    @State private var isCheckingForUpdate = false
    private let authService: AuthServiceProtocol
    private let profileService: ProfileServiceProtocol
    private let runService: RunServiceProtocol
    private let runUploadRetrier: RunningUploadRetrier
    private let teamService: TeamServiceProtocol
    private let rankingService: RankingServiceProtocol
    private let store: LocalAppStateStore
    private let runningHistoryStore: RunningHistoryStore
    private let appUpdateChecker: AppUpdateChecking

    init(
        authService: AuthServiceProtocol,
        profileService: ProfileServiceProtocol = MockProfileService(),
        runService: RunServiceProtocol = MockRunService(),
        runUploadRetrier: RunningUploadRetrier? = nil,
        teamService: TeamServiceProtocol = MockTeamService(),
        rankingService: RankingServiceProtocol = MockRankingService(),
        store: LocalAppStateStore,
        runningHistoryStore: RunningHistoryStore = RunningHistoryStore(),
        appUpdateChecker: AppUpdateChecking = AppStoreUpdateChecker()
    ) {
        self.authService = authService
        self.profileService = profileService
        self.runService = runService
        self.runUploadRetrier = runUploadRetrier ?? RunningUploadRetrier(store: runningHistoryStore, runService: runService)
        self.teamService = teamService
        self.rankingService = rankingService
        self.store = store
        self.runningHistoryStore = runningHistoryStore
        self.appUpdateChecker = appUpdateChecker
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
                    withAnimation(.easeInOut(duration: 0.3)) { route = .login }
                } onComplete: { agreements in
                    acceptedTerms = agreements
                    withAnimation(.easeInOut(duration: 0.3)) { route = .nickname }
                }
                .transition(.move(edge: .trailing))
            case .nickname:
                NicknameSetupView(
                    viewModel: NicknameSetupViewModel(
                        authService: authService,
                        profileService: profileService,
                        accessToken: currentSession?.accessToken,
                        agreements: acceptedTerms
                    )
                ) {
                    withAnimation(.easeInOut(duration: 0.3)) { route = .terms }
                } onComplete: {
                    withAnimation(.easeInOut(duration: 0.3)) { routeToMainOrOnboarding() }
                }
                .transition(.move(edge: .trailing))
            case .profileLoadFailed:
                ProfileLoadErrorView(isRetrying: isResolvingProfile) {
                    guard let currentSession else {
                        route = .login
                        return
                    }
                    Task {
                        await handleLoginCompleted(currentSession)
                    }
                }
            case .onboarding:
                OnboardingView {
                    store.markOnboardingCompleted()
                    withAnimation(.easeInOut(duration: 0.3)) { route = .main }
                }
                .transition(.move(edge: .trailing))
            case .main:
                MainTabView(
                    store: store,
                    profileService: profileService,
                    runService: runService,
                    runUploadRetrier: runUploadRetrier,
                    teamService: teamService,
                    rankingService: rankingService,
                    authService: authService,
                    historyStore: runningHistoryStore,
                    accessToken: currentSession?.accessToken,
                    currentUserID: currentProfileID ?? currentSession?.userID,
                    onLogout: handleLogoutCompleted
                )
            }
        }
        .tint(AppTheme.Colors.primary)
        .overlay {
            if availableUpdate != nil {
                RunpamineConfirmationDialog(
                    title: "업데이트가 필요해요",
                    message: "새로운 버전이 출시되었습니다. 원활한 이용을 위해 업데이트해주세요.",
                    dismissText: "나중에",
                    confirmText: "업데이트",
                    appearance: .android,
                    onDismiss: dismissAvailableUpdate,
                    onConfirm: openAppStore
                )
            }
        }
        .task {
            await checkForUpdateIfNeeded()
        }
        .onChange(of: scenePhase) { _, nextPhase in
            guard nextPhase == .active else { return }
            Task {
                await checkForUpdateIfNeeded()
                await refreshRestoredSession()
                await retryPendingRunsIfPossible()
            }
        }
        .onChange(of: networkMonitor.isConnected) { _, isConnected in
            guard isConnected else { return }
            Task {
                await checkForUpdateIfNeeded()
                await retryPendingRunsIfPossible()
            }
        }
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
        guard !isResolvingProfile else { return }

        isResolvingProfile = true
        defer { isResolvingProfile = false }
        currentSession = session

        do {
            let homeState = try await profileService.fetchHomeState(accessToken: session.accessToken)
            apply(homeState)
            await retryPendingRunsIfPossible()
            withAnimation(.easeInOut(duration: 0.3)) {
                route = homeState.profile == nil ? .terms : mainOrOnboardingRoute()
            }
        } catch {
            do {
                guard let restoredSession = try await authService.restoreSession() else {
                    handleLogoutCompleted()
                    return
                }
                currentSession = restoredSession
            } catch {
                // 세션 저장소를 확인하지 못한 경우에도 가입 상태를 추측하지 않는다.
            }
            withAnimation(.easeInOut(duration: 0.3)) {
                route = .profileLoadFailed
            }
        }
    }

    @MainActor
    private func handleLogoutCompleted() {
        do {
            try runningHistoryStore.removeAll()
        } catch {
            return
        }
        Task {
            await authService.clearLocalSession()
        }
        currentSession = nil
        currentProfileID = nil
        acceptedTerms = []
        isResolvingProfile = false
        route = .login
    }

    @MainActor
    private func refreshRestoredSession() async {
        guard currentSession != nil else { return }

        do {
            if let session = try await authService.restoreSession() {
                currentSession = session
            } else {
                handleLogoutCompleted()
            }
        } catch {
            return
        }
    }

    private func apply(_ homeState: HomeState) {
        if let profile = homeState.profile {
            currentProfileID = profile.id
            store.saveNickname(profile.nickname)
        }

        if let team = homeState.team?.runningTeam {
            store.saveTeam(team)
        }
    }

    private func routeToMainOrOnboarding() {
        route = mainOrOnboardingRoute()
    }

    private func retryPendingRunsIfPossible() async {
        if case .main = route { return }
        guard networkMonitor.isConnected, let accessToken = currentSession?.accessToken else { return }
        _ = await runUploadRetrier.uploadPending(
            accessToken: accessToken,
            currentUserID: currentProfileID ?? currentSession?.userID
        )
    }

    @MainActor
    private func checkForUpdateIfNeeded() async {
        guard networkMonitor.isConnected, !isCheckingForUpdate else { return }

        isCheckingForUpdate = true
        defer { isCheckingForUpdate = false }

        do {
            let update = try await appUpdateChecker.availableUpdate()
            guard update?.version != dismissedUpdateVersion else { return }
            availableUpdate = update
        } catch {
            return
        }
    }

    @MainActor
    private func dismissAvailableUpdate() {
        dismissedUpdateVersion = availableUpdate?.version
        availableUpdate = nil
    }

    @MainActor
    private func openAppStore() {
        guard let storeURL = availableUpdate?.storeURL else { return }
        dismissAvailableUpdate()
        openURL(storeURL)
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
    case profileLoadFailed
    case onboarding
    case main
}

private struct ProfileLoadErrorView: View {
    let isRetrying: Bool
    let onRetry: () -> Void

    var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()

            VStack(spacing: 20) {
                errorImage
                    .resizable()
                    .scaledToFit()
                    .frame(width: 220, height: 220)

                Text("회원 정보를 불러오지 못했어요.\n잠시 후 다시 시도해주세요.")
                    .font(AppTheme.Typography.body1)
                    .foregroundStyle(AppTheme.Colors.textSecondary)
                    .multilineTextAlignment(.center)

                PrimaryButton(
                    title: "다시 시도",
                    isLoading: isRetrying,
                    isDisabled: isRetrying,
                    action: onRetry
                )
                .padding(.horizontal, AppTheme.Layout.horizontalPadding)
            }
        }
    }

    private var errorImage: Image {
        guard let imageURL = Bundle.main.url(forResource: "error", withExtension: "png"),
              let image = UIImage(contentsOfFile: imageURL.path)
        else {
            return Image(systemName: "exclamationmark.triangle.fill")
        }
        return Image(uiImage: image)
    }
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
