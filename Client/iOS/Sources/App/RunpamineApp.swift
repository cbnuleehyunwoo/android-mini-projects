import GoogleSignIn
import SwiftUI

@main
struct RunpamineApp: App {
    private let store = LocalAppStateStore()
    private let profileService: ProfileServiceProtocol
    private let runService: RunServiceProtocol

    init() {
        profileService = (try? ProfileAPIService()) ?? MockProfileService()
        runService = (try? RunAPIService()) ?? MockRunService()
    }

    var body: some Scene {
        WindowGroup {
            RootView(
                authService: SupabaseAuthService(store: store),
                profileService: profileService,
                runService: runService,
                store: store
            )
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
