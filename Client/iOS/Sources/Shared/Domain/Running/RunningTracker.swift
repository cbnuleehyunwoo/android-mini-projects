import CoreLocation
import Foundation

@MainActor
final class RunningTracker: NSObject, ObservableObject {
    enum TrackingState {
        case idle
        case requestingPermission
        case tracking
        case paused
        case ended
        case denied
    }

    private enum PauseReason {
        case manual
        case inactivity
    }

    @Published private(set) var trackingState: TrackingState = .idle
    @Published private(set) var authorizationStatus: CLAuthorizationStatus
    @Published private(set) var lastError: String?
    @Published private(set) var lastRecord: RunningRecord?
    @Published private var session = RunningSession()

    private let manager = CLLocationManager()
    private var timer: Timer?
    private var shouldStartAfterAuthorization = false
    private var shouldValidateJumpFromPreviousLocation = false
    private var stationaryReferenceLocation: CLLocation?
    private var lastLocationMovementDate: Date?
    private var pauseReason: PauseReason?
    private var automaticResumeMonitor: AutomaticResumeLocationMonitor?
    private var automaticResumeTimer: Timer?
    private var automaticResumeBackgroundSession: CLBackgroundActivitySession?
    private var isRequestingFullAccuracy = false
    private let announcer: RunAnnouncing

    override convenience init() {
        self.init(announcer: AudioClipRunAnnouncer())
    }

    init(announcer: RunAnnouncing) {
        self.announcer = announcer
        authorizationStatus = manager.authorizationStatus
        super.init()

        manager.delegate = self
        manager.activityType = .fitness
        manager.desiredAccuracy = kCLLocationAccuracyBest
        manager.distanceFilter = 1
        manager.pausesLocationUpdatesAutomatically = false
        #if os(iOS)
        manager.showsBackgroundLocationIndicator = true
        #endif
    }

    var route: [CLLocationCoordinate2D] {
        session.route
    }

    var latestLocation: CLLocation? {
        session.latestLocation
    }

    var elapsedTime: TimeInterval {
        session.elapsedTime
    }

    var distanceMeters: CLLocationDistance {
        session.distanceMeters
    }

    var averagePaceSecondsPerKilometer: TimeInterval? {
        session.averagePaceSecondsPerKilometer
    }

    var estimatedCalories: Int {
        max(0, Int((session.distanceKilometers * 58).rounded()))
    }

    func start() {
        lastError = nil
        lastRecord = nil
        session.reset()
        shouldStartAfterAuthorization = true
        shouldValidateJumpFromPreviousLocation = false
        pauseReason = nil
        automaticResumeMonitor = nil
        stopAutomaticResumeMonitoring()
        resetStationaryLocationTracking()

        switch authorizationStatus {
        case .notDetermined:
            trackingState = .requestingPermission
            manager.requestWhenInUseAuthorization()
        case .authorizedWhenInUse:
            manager.requestAlwaysAuthorization()
            beginPreciseLocationUpdates()
        case .authorizedAlways:
            beginPreciseLocationUpdates()
        case .denied, .restricted:
            trackingState = .denied
            lastError = "위치 권한이 필요합니다. 설정에서 위치 권한을 허용해주세요."
        @unknown default:
            trackingState = .denied
            lastError = "지원하지 않는 위치 권한 상태입니다."
        }
    }

    func pause() {
        pause(reason: .manual)
    }

    private func pause(reason: PauseReason) {
        guard trackingState == .tracking else { return }

        let automaticResumeReferenceLocation = stationaryReferenceLocation ?? latestLocation
        manager.stopUpdatingLocation()
        if reason == .manual {
            manager.allowsBackgroundLocationUpdates = false
        } else {
            automaticResumeMonitor = AutomaticResumeLocationMonitor(
                referenceLocation: automaticResumeReferenceLocation
            )
            startAutomaticResumeMonitoring()
        }

        stopElapsedTimer()
        session.pauseDistanceMeasurement()
        shouldValidateJumpFromPreviousLocation = false
        resetStationaryLocationTracking()
        pauseReason = reason
        trackingState = .paused
        announcer.announce(.paused)
    }

    func resume() {
        guard trackingState == .paused else { return }
        shouldStartAfterAuthorization = true
        pauseReason = nil
        automaticResumeMonitor = nil
        stopAutomaticResumeMonitoring()
        resetStationaryLocationTracking()
        #if os(iOS)
        if authorizationStatus == .authorizedWhenInUse {
            manager.requestAlwaysAuthorization()
        }
        #endif
        beginPreciseLocationUpdates()
    }

    @discardableResult
    func end() -> RunningRecord? {
        let endedAt = Date()
        shouldStartAfterAuthorization = false
        pauseReason = nil
        automaticResumeMonitor = nil
        stopAutomaticResumeMonitoring()
        resetStationaryLocationTracking()
        manager.stopUpdatingLocation()
        manager.allowsBackgroundLocationUpdates = false
        stopElapsedTimer()

        let record = session.makeRecord(endedAt: endedAt)
        lastRecord = record
        trackingState = .ended
        announcer.announce(.ended)
        return record
    }

    private func beginPreciseLocationUpdates() {
        guard manager.accuracyAuthorization == .fullAccuracy else {
            requestTemporaryFullAccuracy()
            return
        }

        beginLocationUpdates()
    }

    private func requestTemporaryFullAccuracy() {
        guard !isRequestingFullAccuracy else { return }
        isRequestingFullAccuracy = true

        manager.requestTemporaryFullAccuracyAuthorization(withPurposeKey: fullAccuracyPurposeKey) { [weak self] _ in
            Task { @MainActor in
                guard let self else { return }
                self.isRequestingFullAccuracy = false
                self.beginLocationUpdates()
            }
        }
    }

    private func beginLocationUpdates() {
        guard CLLocationManager.locationServicesEnabled() else {
            trackingState = .denied
            lastError = "위치 서비스가 꺼져 있습니다."
            return
        }

        manager.allowsBackgroundLocationUpdates = true
        manager.startUpdatingLocation()
        session.markStartedIfNeeded()
        resetStationaryLocationTracking(at: Date())
        startElapsedTimer()
        let wasPaused = trackingState == .paused
        trackingState = .tracking
        shouldStartAfterAuthorization = false
        announcer.announce(wasPaused ? .resumed : .started)
    }

    private func startElapsedTimer() {
        session.startElapsedTimer()
        timer?.invalidate()
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] _ in
            Task { @MainActor in
                guard let self else { return }
                let now = Date()
                self.session.updateElapsedTime(at: now)
                self.pauseIfLocationHasNotChanged(at: now)
            }
        }
    }

    private func stopElapsedTimer() {
        session.stopElapsedTimer()
        timer?.invalidate()
        timer = nil
    }
}

extension RunningTracker: CLLocationManagerDelegate {
    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            authorizationStatus = manager.authorizationStatus

            if authorizationStatus == .denied || authorizationStatus == .restricted {
                trackingState = .denied
                shouldStartAfterAuthorization = false
                pauseReason = nil
                automaticResumeMonitor = nil
                stopAutomaticResumeMonitoring()
                manager.stopUpdatingLocation()
                manager.allowsBackgroundLocationUpdates = false
                stopElapsedTimer()
                session.pauseDistanceMeasurement()
                lastError = "위치 권한이 거부되었습니다."
                return
            }

            if shouldStartAfterAuthorization,
               isRunnableAuthorizationStatus(authorizationStatus) {
                #if os(iOS)
                if authorizationStatus == .authorizedWhenInUse {
                    manager.requestAlwaysAuthorization()
                }
                #endif
                beginPreciseLocationUpdates()
            }
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        Task { @MainActor in
            switch trackingState {
            case .tracking:
                for location in locations where isAcceptable(location) {
                    updateStationaryLocationTracking(with: location, at: Date())
                    session.append(location)
                    shouldValidateJumpFromPreviousLocation = true
                }
            case .paused where pauseReason == .inactivity:
                guard let location = locations.last(where: isAccurate) else { return }
                resumeAutomaticallyIfMovementDetected(with: location)
            default:
                return
            }
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Task { @MainActor in
            lastError = error.localizedDescription
        }
    }
}

private extension RunningTracker {
    func isAcceptable(_ location: CLLocation) -> Bool {
        guard isAccurate(location) else { return false }

        guard shouldValidateJumpFromPreviousLocation,
              let previousLocation = latestLocation
        else {
            return true
        }

        let elapsedSeconds = abs(location.timestamp.timeIntervalSince(previousLocation.timestamp))
        guard elapsedSeconds > 0 else { return true }

        let allowedDistance = elapsedSeconds * maxRunningMetersPerSecond + jumpToleranceMeters
        return location.distance(from: previousLocation) <= allowedDistance
    }

    func isAccurate(_ location: CLLocation) -> Bool {
        location.horizontalAccuracy >= 0 &&
            location.horizontalAccuracy <= maxHorizontalAccuracyMeters
    }

    func updateStationaryLocationTracking(with location: CLLocation, at date: Date) {
        guard let referenceLocation = stationaryReferenceLocation else {
            stationaryReferenceLocation = location
            lastLocationMovementDate = date
            return
        }

        if location.distance(from: referenceLocation) > stationaryLocationMovementThresholdMeters {
            stationaryReferenceLocation = location
            lastLocationMovementDate = date
        }
    }

    func pauseIfLocationHasNotChanged(at date: Date) {
        guard trackingState == .tracking,
              let lastLocationMovementDate,
              date.timeIntervalSince(lastLocationMovementDate) >= stationaryLocationPauseInterval
        else {
            return
        }

        pause(reason: .inactivity)
    }

    func startAutomaticResumeMonitoring() {
        automaticResumeBackgroundSession = CLBackgroundActivitySession()
        automaticResumeTimer = Timer.scheduledTimer(
            withTimeInterval: automaticResumeProbeInterval,
            repeats: true
        ) { [weak self] _ in
            Task { @MainActor in
                guard let self,
                      self.trackingState == .paused,
                      self.pauseReason == .inactivity
                else {
                    return
                }
                self.manager.requestLocation()
            }
        }
    }

    func stopAutomaticResumeMonitoring() {
        automaticResumeTimer?.invalidate()
        automaticResumeTimer = nil
        automaticResumeBackgroundSession?.invalidate()
        automaticResumeBackgroundSession = nil
    }

    func resumeAutomaticallyIfMovementDetected(with location: CLLocation) {
        guard var automaticResumeMonitor else { return }

        let shouldResume = automaticResumeMonitor.detectsMovement(with: location)
        self.automaticResumeMonitor = automaticResumeMonitor

        if shouldResume {
            resume()
        }
    }

    func resetStationaryLocationTracking(at date: Date? = nil) {
        stationaryReferenceLocation = nil
        lastLocationMovementDate = date
    }
}

private struct AutomaticResumeLocationMonitor {
    private var referenceLocation: CLLocation?
    private var movementCandidateLocation: CLLocation?

    init(referenceLocation: CLLocation?) {
        self.referenceLocation = referenceLocation
    }

    mutating func detectsMovement(with location: CLLocation) -> Bool {
        guard let referenceLocation else {
            self.referenceLocation = location
            return false
        }

        guard location.distance(from: referenceLocation) > stationaryLocationMovementThresholdMeters else {
            movementCandidateLocation = nil
            return false
        }

        defer {
            self.movementCandidateLocation = location
        }
        guard let movementCandidateLocation else {
            return false
        }
        return location.distance(from: movementCandidateLocation) > stationaryLocationMovementThresholdMeters
    }
}

private let maxHorizontalAccuracyMeters: CLLocationAccuracy = 30
private let maxRunningMetersPerSecond: CLLocationDistance = 7
private let jumpToleranceMeters: CLLocationDistance = 6
private let stationaryLocationMovementThresholdMeters: CLLocationDistance = 1
private let stationaryLocationPauseInterval: TimeInterval = 10
private let automaticResumeProbeInterval: TimeInterval = 3
private let fullAccuracyPurposeKey = "RunningRoute"

private func isRunnableAuthorizationStatus(_ status: CLAuthorizationStatus) -> Bool {
    #if os(iOS)
    return status == .authorizedWhenInUse || status == .authorizedAlways
    #else
    return status == .authorizedAlways
    #endif
}
