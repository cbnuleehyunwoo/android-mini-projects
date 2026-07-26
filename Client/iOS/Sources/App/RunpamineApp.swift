import GoogleSignIn
import SwiftUI

@main
struct RunpamineApp: App {
    @StateObject private var networkMonitor = NetworkMonitor()
    private let store = LocalAppStateStore()
    private let profileService: ProfileServiceProtocol
    private let runService: RunServiceProtocol
    private let teamService: TeamServiceProtocol
    private let rankingService: RankingServiceProtocol
    private let authService: AuthServiceProtocol

    init() {
        let sessionStore = AuthSessionStore.shared
        let httpClient = AuthenticatedHTTPClient(sessionStore: sessionStore)

        authService = WASAuthService(store: store, sessionStore: sessionStore, httpClient: httpClient)
        profileService = Self.makeProfileService(httpClient: httpClient)
        runService = Self.makeRunService(httpClient: httpClient)
        teamService = Self.makeTeamService(httpClient: httpClient, store: store)
        rankingService = Self.makeRankingService(httpClient: httpClient)
    }

    var body: some Scene {
        WindowGroup {
            RootView(
                authService: authService,
                profileService: profileService,
                runService: runService,
                teamService: teamService,
                rankingService: rankingService,
                store: store
            )
                .networkErrorOverlay()
                .environmentObject(networkMonitor)
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}

private extension RunpamineApp {
    static func makeProfileService(httpClient: AuthenticatedHTTPClient) -> ProfileServiceProtocol {
        #if DEBUG
        return (try? ProfileAPIService(httpClient: httpClient)) ?? MockProfileService()
        #else
        do {
            return try ProfileAPIService(httpClient: httpClient)
        } catch {
            fatalError("ProfileAPIService initialization failed: \(error.localizedDescription)")
        }
        #endif
    }

    static func makeRunService(httpClient: AuthenticatedHTTPClient) -> RunServiceProtocol {
        #if DEBUG
        return (try? RunAPIService(httpClient: httpClient)) ?? MockRunService()
        #else
        do {
            return try RunAPIService(httpClient: httpClient)
        } catch {
            fatalError("RunAPIService initialization failed: \(error.localizedDescription)")
        }
        #endif
    }

    static func makeTeamService(
        httpClient: AuthenticatedHTTPClient,
        store: LocalAppStateStore
    ) -> TeamServiceProtocol {
        #if DEBUG
        return (try? TeamAPIService(httpClient: httpClient)) ?? MockTeamService(store: store)
        #else
        do {
            return try TeamAPIService(httpClient: httpClient)
        } catch {
            fatalError("TeamAPIService initialization failed: \(error.localizedDescription)")
        }
        #endif
    }

    static func makeRankingService(httpClient: AuthenticatedHTTPClient) -> RankingServiceProtocol {
        #if DEBUG
        return (try? RankingAPIService(httpClient: httpClient)) ?? MockRankingService()
        #else
        do {
            return try RankingAPIService(httpClient: httpClient)
        } catch {
            fatalError("RankingAPIService initialization failed: \(error.localizedDescription)")
        }
        #endif
    }
}
