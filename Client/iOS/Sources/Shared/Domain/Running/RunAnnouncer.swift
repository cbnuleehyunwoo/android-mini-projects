import AVFoundation
import Foundation

enum RunAnnouncement {
    case started
    case paused
    case resumed
    case ended

    /// 번들에 포함된 안내 음성 클립의 리소스 이름(확장자 제외).
    var resourceName: String {
        switch self {
        case .started: return "run_start"
        case .paused: return "run_pause"
        case .resumed: return "run_resume"
        case .ended: return "run_stop"
        }
    }
}

@MainActor
protocol RunAnnouncing {
    func announce(_ announcement: RunAnnouncement)
}

/// 앱에 번들된 사전 녹음 오디오 클립(`AVAudioPlayer`)으로 러닝 상태 전이를 안내한다.
/// 시스템 TTS와 달리 모든 기기에서 동일한 목소리로 재생된다.
@MainActor
final class AudioClipRunAnnouncer: NSObject, RunAnnouncing {
    private let bundle: Bundle
    private var player: AVAudioPlayer?

    init(bundle: Bundle = .main) {
        self.bundle = bundle
        super.init()
        configureAudioSession()
    }

    func announce(_ announcement: RunAnnouncement) {
        guard let url = bundle.url(forResource: announcement.resourceName, withExtension: clipFileExtension) else {
            return
        }
        do {
            let player = try AVAudioPlayer(contentsOf: url)
            player.delegate = self
            self.player = player
            activateAudioSession()
            player.play()
        } catch {
            deactivateAudioSession()
        }
    }

    private func configureAudioSession() {
        #if os(iOS)
        try? AVAudioSession.sharedInstance().setCategory(
            .playback,
            mode: .spokenAudio,
            options: [.duckOthers, .mixWithOthers]
        )
        #endif
    }

    private func activateAudioSession() {
        #if os(iOS)
        try? AVAudioSession.sharedInstance().setActive(true)
        #endif
    }

    private func deactivateAudioSession() {
        #if os(iOS)
        try? AVAudioSession.sharedInstance().setActive(false, options: [.notifyOthersOnDeactivation])
        #endif
    }
}

extension AudioClipRunAnnouncer: AVAudioPlayerDelegate {
    nonisolated func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        Task { @MainActor in self.deactivateAudioSession() }
    }

    nonisolated func audioPlayerDecodeErrorDidOccur(_ player: AVAudioPlayer, error: Error?) {
        Task { @MainActor in self.deactivateAudioSession() }
    }
}

private let clipFileExtension = "m4a"
