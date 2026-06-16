import Lottie
import SwiftUI

struct RunpamineLottieView: View {
    let animation: RunpamineLottieAnimation
    var loopMode: LottieLoopMode = .loop

    var body: some View {
        if let lottieAnimation = animation.lottieAnimation {
            LottieView(animation: lottieAnimation)
                .playing(loopMode: loopMode)
                .animationSpeed(animation.speed)
        } else {
            Image(animation.fallbackImageName)
                .resizable()
                .scaledToFit()
        }
    }
}

enum RunpamineLottieAnimation: String, Equatable {
    case hamburger = "hamburger10"
    case idle = "idle10"
    case running = "enchoRunning10"
    case reverse = "reverse10"
    case cheetah = "cheeta10"

    var fallbackImageName: String {
        switch self {
        case .hamburger, .idle:
            return "bk"
        case .running, .reverse, .cheetah:
            return "encho"
        }
    }

    var lottieAnimation: LottieAnimation? {
        guard let url = Bundle.main.url(forResource: rawValue, withExtension: "json", subdirectory: "lottie") else {
            return nil
        }

        return LottieAnimation.filepath(url.path)
    }

    var speed: Double {
        switch self {
        case .cheetah:
            return 1.5
        case .reverse:
            return 0.75
        case .hamburger, .idle, .running:
            return 1
        }
    }

    static func teamMember(consecutiveRunDays: Int?) -> RunpamineLottieAnimation {
        guard let consecutiveRunDays else { return .hamburger }

        if consecutiveRunDays >= 5 {
            return .cheetah
        }

        if consecutiveRunDays == 4 {
            return .reverse
        }

        if consecutiveRunDays == 3 {
            return .running
        }

        if consecutiveRunDays == 2 {
            return .idle
        }

        return .hamburger
    }
}
