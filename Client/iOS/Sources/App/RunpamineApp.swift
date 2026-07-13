import GoogleSignIn
import Supabase
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
        let supabase = Bundle.main.supabaseConfiguration.map {
            SupabaseClient(
                supabaseURL: $0.url,
                supabaseKey: $0.anonKey,
                options: .init(
                    auth: .init(
                        redirectToURL: SupabaseConfiguration.redirectURL,
                        emitLocalSessionAsInitialSession: true
                    )
                )
            )
        }
        let httpClient = AuthenticatedHTTPClient(supabase: supabase)

        authService = SupabaseAuthService(store: store, supabase: supabase, httpClient: httpClient)
        profileService = (try? ProfileAPIService(httpClient: httpClient)) ?? MockProfileService()
        runService = (try? RunAPIService(httpClient: httpClient)) ?? MockRunService()
        teamService = (try? TeamAPIService(httpClient: httpClient)) ?? MockTeamService(store: store)
        rankingService = (try? RankingAPIService(httpClient: httpClient)) ?? MockRankingService()
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
