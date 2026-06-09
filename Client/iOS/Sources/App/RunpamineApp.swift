import GoogleSignIn
import SwiftUI

@main
struct RunpamineApp: App {
    private let store = LocalAppStateStore()
    private let profileService: ProfileServiceProtocol
    private let runService: RunServiceProtocol
    private let teamService: TeamServiceProtocol

    init() {
        profileService = (try? ProfileAPIService()) ?? MockProfileService()
        runService = (try? RunAPIService()) ?? MockRunService()
        teamService = (try? TeamAPIService()) ?? MockTeamService(store: store)
    }

    var body: some Scene {
        WindowGroup {
            RootView(
                authService: SupabaseAuthService(store: store),
                profileService: profileService,
                runService: runService,
                teamService: teamService,
                store: store
            )
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
