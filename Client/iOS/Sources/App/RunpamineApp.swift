import SwiftUI

@main
struct RunpamineApp: App {
    var body: some Scene {
        WindowGroup {
            RootView(authService: MockAuthService())
        }
    }
}
