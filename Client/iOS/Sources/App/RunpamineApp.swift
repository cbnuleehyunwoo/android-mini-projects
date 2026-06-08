import GoogleSignIn
import SwiftUI

@main
struct RunpamineApp: App {
    private let store = LocalAppStateStore()

    var body: some Scene {
        WindowGroup {
            RootView(authService: SupabaseAuthService(store: store), store: store)
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
