import AVFoundation
import Foundation

enum RunningVoiceCue: String {
    case start = "running_start"
    case pause = "running_pause"
    case resume = "running_resume"
    case stop = "running_stop"
}

@MainActor
protocol RunningVoiceCuePlaying: AnyObject {
    func play(_ cue: RunningVoiceCue)
}

@MainActor
final class RunningVoiceCuePlayer: RunningVoiceCuePlaying {
    static let shared = RunningVoiceCuePlayer()

    private var player: AVAudioPlayer?
    private var restoreTimer: Timer?

    private init() {}

    func play(_ cue: RunningVoiceCue) {
        restoreTimer?.invalidate()
        restoreTimer = nil

        guard let url = Bundle.main.url(forResource: cue.rawValue, withExtension: "mp3") else {
            return
        }

        do {
            let audioSession = AVAudioSession.sharedInstance()
            try audioSession.setCategory(.playback, mode: .spokenAudio, options: [.duckOthers])
            try audioSession.setActive(true)

            let cuePlayer = try AVAudioPlayer(contentsOf: url)
            player?.stop()
            player = cuePlayer
            cuePlayer.prepareToPlay()
            cuePlayer.play()
            scheduleAudioSessionRestore(after: cuePlayer.duration)
        } catch {
            restoreAudioSession()
        }
    }

    private func scheduleAudioSessionRestore(after duration: TimeInterval) {
        restoreTimer = Timer.scheduledTimer(withTimeInterval: duration + 0.25, repeats: false) { [weak self] _ in
            Task { @MainActor in
                self?.restoreAudioSession()
            }
        }
    }

    private func restoreAudioSession() {
        restoreTimer?.invalidate()
        restoreTimer = nil
        player = nil
        try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
    }
}
