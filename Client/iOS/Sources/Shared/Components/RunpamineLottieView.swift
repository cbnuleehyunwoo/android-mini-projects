import Lottie
import SwiftUI

struct RunpamineLottieView: View {
    let animation: RunpamineLottieAnimation
    var isPlaying = true
    var loopMode: LottieLoopMode = .loop

    var body: some View {
        if isPlaying, let lottieAnimation = animation.lottieAnimation {
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

enum RunpamineLottieAnimation: String, CaseIterable, Equatable {
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
        Self.lottieAnimations[rawValue]
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

    private static let lottieAnimations: [String: LottieAnimation] = {
        Dictionary(uniqueKeysWithValues: Self.allCases.compactMap { animation in
            guard let url = Bundle.main.url(forResource: animation.rawValue, withExtension: "json", subdirectory: "lottie"),
                  let lottieAnimation = LottieAnimation.filepath(url.path)
            else {
                return nil
            }

            return (animation.rawValue, lottieAnimation)
        })
    }()
}
