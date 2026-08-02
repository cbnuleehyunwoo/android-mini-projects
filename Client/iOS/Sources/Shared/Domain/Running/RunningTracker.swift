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

    @Published private(set) var trackingState: TrackingState = .idle
    @Published private(set) var authorizationStatus: CLAuthorizationStatus
    @Published private(set) var lastError: String?
    @Published private(set) var lastRecord: RunningRecord?
    @Published private var session = RunningSession()

    private let manager = CLLocationManager()
    private var timer: Timer?
    private var shouldStartAfterAuthorization = false
    private var shouldValidateJumpFromPreviousLocation = false
    private var isRequestingFullAccuracy = false

    override init() {
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
        guard trackingState == .tracking else { return }

        manager.stopUpdatingLocation()
        manager.allowsBackgroundLocationUpdates = false
        stopElapsedTimer()
        session.pauseDistanceMeasurement()
        shouldValidateJumpFromPreviousLocation = false
        trackingState = .paused
    }

    func resume() {
        guard trackingState == .paused else { return }
        shouldStartAfterAuthorization = true
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
        manager.stopUpdatingLocation()
        manager.allowsBackgroundLocationUpdates = false
        stopElapsedTimer()

        let record = session.makeRecord(endedAt: endedAt)
        lastRecord = record
        trackingState = .ended
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
        startElapsedTimer()
        trackingState = .tracking
        shouldStartAfterAuthorization = false
    }

    private func startElapsedTimer() {
        session.startElapsedTimer()
        timer?.invalidate()
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] _ in
            Task { @MainActor in
                guard let self else { return }
                self.session.updateElapsedTime(at: Date())
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
            guard trackingState == .tracking else { return }

            lastError = nil
            for location in locations where isAcceptable(location) {
                session.append(location)
                shouldValidateJumpFromPreviousLocation = true
            }
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Task { @MainActor in
            guard let locationError = error as? CLError else {
                lastError = "위치 정보를 가져오지 못했습니다. 잠시 후 다시 시도해 주세요."
                return
            }

            switch locationError.code {
            case .locationUnknown:
                // Core Location keeps retrying after this temporary loss of signal.
                break
            case .denied:
                lastError = "위치 권한이 거부되었습니다."
            case .network:
                lastError = "네트워크 문제로 위치 정보를 가져오지 못했습니다."
            default:
                lastError = "위치 정보를 가져오지 못했습니다. 잠시 후 다시 시도해 주세요."
            }
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
}

private let maxHorizontalAccuracyMeters: CLLocationAccuracy = 30
private let maxRunningMetersPerSecond: CLLocationDistance = 7
private let jumpToleranceMeters: CLLocationDistance = 6
private let fullAccuracyPurposeKey = "RunningRoute"

private func isRunnableAuthorizationStatus(_ status: CLAuthorizationStatus) -> Bool {
    #if os(iOS)
    return status == .authorizedWhenInUse || status == .authorizedAlways
    #else
    return status == .authorizedAlways
    #endif
}
