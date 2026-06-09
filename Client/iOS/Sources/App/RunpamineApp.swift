import GoogleSignIn
import SwiftUI

@main
struct RunpamineApp: App {
    private let store = LocalAppStateStore()
    private let profileService: ProfileServiceProtocol

    init() {
        profileService = (try? ProfileAPIService()) ?? MockProfileService()
    }

    var body: some Scene {
        WindowGroup {
            RootView(authService: SupabaseAuthService(store: store), profileService: profileService, store: store)
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
