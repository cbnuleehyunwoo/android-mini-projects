import CoreLocation
import Foundation

struct RunningSession {
    private(set) var routePoints: [RunningCoordinate] = []
    private(set) var latestLocation: CLLocation?
    private(set) var elapsedTime: TimeInterval = 0
    private(set) var distanceMeters: CLLocationDistance = 0

    private var segmentStartedAt: Date?
    private var accumulatedElapsedTime: TimeInterval = 0
    private var startedAt: Date?
    private var shouldMeasureDistanceFromPreviousLocation = false

    var distanceKilometers: Double {
        distanceMeters / 1_000
    }

    var averagePaceSecondsPerKilometer: TimeInterval? {
        guard distanceKilometers > 0.01 else { return nil }
        return elapsedTime / distanceKilometers
    }

    var route: [CLLocationCoordinate2D] {
        routePoints.map(\.coordinate)
    }

    mutating func reset() {
        routePoints.removeAll()
        latestLocation = nil
        elapsedTime = 0
        distanceMeters = 0
        segmentStartedAt = nil
        accumulatedElapsedTime = 0
        startedAt = nil
        shouldMeasureDistanceFromPreviousLocation = false
    }

    mutating func markStartedIfNeeded(at date: Date = Date()) {
        if startedAt == nil {
            startedAt = date
        }
    }

    mutating func startElapsedTimer(at date: Date = Date()) {
        segmentStartedAt = date
        updateElapsedTime(at: date)
    }

    mutating func stopElapsedTimer(at date: Date = Date()) {
        updateElapsedTime(at: date)
        accumulatedElapsedTime = elapsedTime
        segmentStartedAt = nil
    }

    mutating func updateElapsedTime(at date: Date = Date()) {
        guard let segmentStartedAt else {
            elapsedTime = accumulatedElapsedTime
            return
        }

        elapsedTime = accumulatedElapsedTime + date.timeIntervalSince(segmentStartedAt)
    }

    mutating func pauseDistanceMeasurement() {
        shouldMeasureDistanceFromPreviousLocation = false
    }

    mutating func append(_ location: CLLocation) {
        if shouldMeasureDistanceFromPreviousLocation, let previousLocation = latestLocation {
            let delta = location.distance(from: previousLocation)
            if delta.isFinite && delta >= 0 {
                distanceMeters += delta
            }
        }

        latestLocation = location
        routePoints.append(
            RunningCoordinate(
                location.coordinate,
                recordedAt: location.timestamp,
                horizontalAccuracy: location.horizontalAccuracy
            )
        )
        shouldMeasureDistanceFromPreviousLocation = true
    }

    func makeRecord(endedAt: Date) -> RunningRecord? {
        guard elapsedTime > 0 || distanceMeters > 0 || !route.isEmpty else { return nil }

        return RunningRecord(
            startedAt: startedAt ?? endedAt.addingTimeInterval(-elapsedTime),
            endedAt: endedAt,
            elapsedTime: elapsedTime,
            distanceMeters: distanceMeters,
            route: routePoints
        )
    }
}
