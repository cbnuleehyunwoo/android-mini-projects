import CoreLocation
import Foundation

struct CreatedRun: Equatable {
    let id: String
    let distanceMeters: Int
    let durationSeconds: Int
    let averagePaceSecondsPerKilometer: Int?
    let calories: Int
}

struct RunDaySummary: Identifiable, Equatable {
    let date: Date
    let distanceMeters: Int
    let hasRun: Bool

    var id: Date {
        date
    }
}

struct RunPeriodSummary: Equatable {
    let totalDistanceMeters: Int
    let days: [RunDaySummary]
    let runs: [RunningRecord]
}

extension RunningRecord {
    var createRunPoints: [CreateRunPoint] {
        guard !route.isEmpty else { return [] }

        return route.enumerated().map { index, point in
            let fallbackRecordedAt = fallbackRecordedAt(for: index)
            return CreateRunPoint(
                sequence: index + 1,
                latitude: point.latitude,
                longitude: point.longitude,
                recordedAt: point.recordedAt ?? fallbackRecordedAt
            )
        }
    }

    private func fallbackRecordedAt(for index: Int) -> Date {
        guard route.count > 1, elapsedTime > 0 else {
            return startedAt
        }

        let ratio = Double(index) / Double(route.count - 1)
        return startedAt.addingTimeInterval(elapsedTime * ratio)
    }
}

struct CreateRunPoint: Equatable {
    let sequence: Int
    let latitude: CLLocationDegrees
    let longitude: CLLocationDegrees
    let recordedAt: Date
}
