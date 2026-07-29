import AVFoundation
import Foundation

enum RunAnnouncement {
    case started
    case paused
    case resumed
    case ended

    var text: String {
        switch self {
        case .started: return "러닝을 시작합니다."
        case .paused: return "러닝을 일시 중지합니다."
        case .resumed: return "러닝을 재개합니다."
        case .ended: return "러닝을 종료합니다."
        }
    }
}

@MainActor
protocol RunAnnouncing {
    func announce(_ announcement: RunAnnouncement)
}

/// 온디바이스 TTS(`AVSpeechSynthesizer`)로 러닝 상태 전이를 한국어 여성 음성으로 안내한다.
@MainActor
final class SpeechRunAnnouncer: NSObject, RunAnnouncing {
    private let synthesizer = AVSpeechSynthesizer()

    override init() {
        super.init()
        synthesizer.delegate = self
        configureAudioSession()
    }

    func announce(_ announcement: RunAnnouncement) {
        let utterance = AVSpeechUtterance(string: announcement.text)
        utterance.voice = AVSpeechSynthesisVoice(language: "ko-KR")
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate
        activateAudioSession()
        synthesizer.speak(utterance)
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

extension SpeechRunAnnouncer: AVSpeechSynthesizerDelegate {
    nonisolated func speechSynthesizer(
        _ synthesizer: AVSpeechSynthesizer,
        didFinish utterance: AVSpeechUtterance
    ) {
        Task { @MainActor in self.deactivateAudioSession() }
    }

    nonisolated func speechSynthesizer(
        _ synthesizer: AVSpeechSynthesizer,
        didCancel utterance: AVSpeechUtterance
    ) {
        Task { @MainActor in self.deactivateAudioSession() }
    }
}
