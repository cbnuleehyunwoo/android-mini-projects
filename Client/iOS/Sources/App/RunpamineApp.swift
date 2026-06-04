import SwiftUI

@main
struct RunpamineApp: App {
    private let store = LocalAppStateStore()

    var body: some Scene {
        WindowGroup {
            RootView(authService: MockAuthService(store: store), store: store)
        }
    }
}
