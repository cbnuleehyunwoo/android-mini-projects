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

    init() {
        profileService = (try? ProfileAPIService()) ?? MockProfileService()
        runService = (try? RunAPIService()) ?? MockRunService()
        teamService = (try? TeamAPIService()) ?? MockTeamService(store: store)
        rankingService = (try? RankingAPIService()) ?? MockRankingService()
    }

    var body: some Scene {
        WindowGroup {
            RootView(
                authService: SupabaseAuthService(store: store),
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
