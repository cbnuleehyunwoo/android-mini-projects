import SwiftUI

struct SplashView: View {
    var onFinished: () -> Void = {}

    var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()

            RunpamineLogoView()
        }
        .task {
            try? await Task.sleep(nanoseconds: 1_200_000_000)
            onFinished()
        }
    }
}

#Preview {
    SplashView()
}
