import SwiftUI

struct SplashView: View {
    var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()

            RunpamineLogoView()
        }
    }
}

#Preview {
    SplashView()
}
