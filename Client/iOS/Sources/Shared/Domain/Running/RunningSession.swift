import CoreLocation
import Foundation

struct RunningSession {
    private(set) var routePoints: [RunningCoordinate] = []
    private(set) var latestLocation: CLLocation?
    private(set) var elapsedTime: TimeInterval = 0
    private(set) var distanceMeters: CLLocationDistance = 0
    private(set) var averagePaceSecondsPerKilometer: TimeInterval?
    private(set) var completedSplits: [RunningSplit] = []

    private var segmentStartedAt: Date?
    private var accumulatedElapsedTime: TimeInterval = 0
    private var startedAt: Date?
    private var shouldMeasureDistanceFromPreviousLocation = false
    private var previousLocationElapsedTime: TimeInterval?
    private var lastSplitBoundaryDistanceMeters: CLLocationDistance = 0
    private var lastSplitBoundaryElapsedTime: TimeInterval = 0

    var distanceKilometers: Double {
        distanceMeters / 1_000
    }

    var route: [CLLocationCoordinate2D] {
        routePoints.map(\.coordinate)
    }

    mutating func reset() {
        routePoints.removeAll()
        latestLocation = nil
        elapsedTime = 0
        distanceMeters = 0
        averagePaceSecondsPerKilometer = nil
        completedSplits.removeAll()
        segmentStartedAt = nil
        accumulatedElapsedTime = 0
        startedAt = nil
        shouldMeasureDistanceFromPreviousLocation = false
        previousLocationElapsedTime = nil
        lastSplitBoundaryDistanceMeters = 0
        lastSplitBoundaryElapsedTime = 0
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

    mutating func append(_ location: CLLocation, at date: Date = Date()) {
        updateElapsedTime(at: date)

        let distanceBeforeLocation = distanceMeters
        let elapsedBeforeLocation = previousLocationElapsedTime ?? elapsedTime

        if shouldMeasureDistanceFromPreviousLocation, let previousLocation = latestLocation {
            let delta = location.distance(from: previousLocation)
            if delta.isFinite && delta >= 0 {
                distanceMeters += delta
                appendCompletedSplits(
                    fromDistanceMeters: distanceBeforeLocation,
                    toDistanceMeters: distanceMeters,
                    fromElapsedTime: elapsedBeforeLocation,
                    toElapsedTime: elapsedTime
                )
            }
        }

        updateAveragePace()
        latestLocation = location
        routePoints.append(
            RunningCoordinate(
                location.coordinate,
                recordedAt: location.timestamp,
                horizontalAccuracy: location.horizontalAccuracy
            )
        )
        shouldMeasureDistanceFromPreviousLocation = true
        previousLocationElapsedTime = elapsedTime
    }

    func makeRecord(endedAt: Date) -> RunningRecord? {
        guard elapsedTime > 0 || distanceMeters > 0 || !route.isEmpty else { return nil }

        return RunningRecord(
            startedAt: startedAt ?? endedAt.addingTimeInterval(-elapsedTime),
            endedAt: endedAt,
            elapsedTime: elapsedTime,
            distanceMeters: distanceMeters,
            averagePaceSecondsPerKilometer: averagePaceSecondsPerKilometer,
            route: routePoints,
            splits: finalizedSplits()
        )
    }

    private mutating func appendCompletedSplits(
        fromDistanceMeters: CLLocationDistance,
        toDistanceMeters: CLLocationDistance,
        fromElapsedTime: TimeInterval,
        toElapsedTime: TimeInterval
    ) {
        guard toDistanceMeters > fromDistanceMeters else { return }

        var nextBoundary = (Double(completedSplits.count) + 1) * 1_000
        while nextBoundary <= toDistanceMeters {
            let progress = (nextBoundary - fromDistanceMeters) / (toDistanceMeters - fromDistanceMeters)
            let boundaryElapsedTime = fromElapsedTime + (toElapsedTime - fromElapsedTime) * progress
            let durationMillis = max(1, Int(((boundaryElapsedTime - lastSplitBoundaryElapsedTime) * 1_000).rounded()))
            completedSplits.append(
                RunningSplit(
                    sequence: completedSplits.count + 1,
                    fromDistanceMeters: lastSplitBoundaryDistanceMeters,
                    toDistanceMeters: nextBoundary,
                    distanceMeters: nextBoundary - lastSplitBoundaryDistanceMeters,
                    durationMillis: durationMillis,
                    paceSecondsPerKilometer: Double(durationMillis) / 1_000
                )
            )
            lastSplitBoundaryDistanceMeters = nextBoundary
            lastSplitBoundaryElapsedTime = boundaryElapsedTime
            nextBoundary += 1_000
        }
    }

    private func finalizedSplits() -> [RunningSplit] {
        let remainingDistanceMeters = distanceMeters - lastSplitBoundaryDistanceMeters
        guard remainingDistanceMeters > 0 else { return completedSplits }

        let durationMillis = max(1, Int(((elapsedTime - lastSplitBoundaryElapsedTime) * 1_000).rounded()))
        let paceSecondsPerKilometer = Double(durationMillis) / remainingDistanceMeters
        return completedSplits + [
            RunningSplit(
                sequence: completedSplits.count + 1,
                fromDistanceMeters: lastSplitBoundaryDistanceMeters,
                toDistanceMeters: distanceMeters,
                distanceMeters: remainingDistanceMeters,
                durationMillis: durationMillis,
                paceSecondsPerKilometer: paceSecondsPerKilometer
            )
        ]
    }

    private mutating func updateAveragePace() {
        guard distanceKilometers > 0.01 else {
            averagePaceSecondsPerKilometer = nil
            return
        }

        averagePaceSecondsPerKilometer = elapsedTime / distanceKilometers
    }
}
