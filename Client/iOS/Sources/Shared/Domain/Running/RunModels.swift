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
        let uploadRoute = route.simplifiedForUpload()
        guard !uploadRoute.isEmpty else { return [] }

        return uploadRoute.enumerated().map { index, point in
            let fallbackRecordedAt = fallbackRecordedAt(for: index)
            return CreateRunPoint(
                sequence: index + 1,
                latitude: point.latitude,
                longitude: point.longitude,
                horizontalAccuracyMeters: point.horizontalAccuracy,
                recordedAt: point.recordedAt ?? fallbackRecordedAt
            )
        }
    }

    private func fallbackRecordedAt(for index: Int) -> Date {
        let uploadPointCount = route.simplifiedForUpload().count
        guard uploadPointCount > 1, elapsedTime > 0 else {
            return startedAt
        }

        let ratio = Double(index) / Double(uploadPointCount - 1)
        return startedAt.addingTimeInterval(elapsedTime * ratio)
    }
}

struct CreateRunPoint: Equatable {
    let sequence: Int
    let latitude: CLLocationDegrees
    let longitude: CLLocationDegrees
    let horizontalAccuracyMeters: CLLocationAccuracy?
    let recordedAt: Date
}

private extension Array where Element == RunningCoordinate {
    func simplifiedForUpload() -> [RunningCoordinate] {
        guard count > 2 else { return self }

        var keep = Swift.Array(repeating: false, count: count)
        keep[0] = true
        keep[count - 1] = true
        simplifyRange(keep: &keep, startIndex: 0, endIndex: count - 1)
        return enumerated().compactMap { index, point in
            keep[index] ? point : nil
        }
    }

    func simplifyRange(keep: inout [Bool], startIndex: Int, endIndex: Int) {
        guard endIndex > startIndex + 1 else { return }

        var maxDistance: CLLocationDistance = 0
        var maxIndex = startIndex
        for index in (startIndex + 1)..<endIndex {
            let distance = self[index].distanceFromSegment(start: self[startIndex], end: self[endIndex])
            if distance > maxDistance {
                maxDistance = distance
                maxIndex = index
            }
        }

        if maxDistance > uploadSimplificationEpsilonMeters {
            keep[maxIndex] = true
            simplifyRange(keep: &keep, startIndex: startIndex, endIndex: maxIndex)
            simplifyRange(keep: &keep, startIndex: maxIndex, endIndex: endIndex)
        }
    }
}

private extension RunningCoordinate {
    func distanceFromSegment(start: RunningCoordinate, end: RunningCoordinate) -> CLLocationDistance {
        let endX = start.eastWestDistance(to: end)
        let endY = start.northSouthDistance(to: end)
        let denominator = endX * endX + endY * endY
        guard denominator > 0 else {
            return location.distance(from: start.location)
        }

        let x = start.eastWestDistance(to: self)
        let y = start.northSouthDistance(to: self)
        let progress = max(0, min(1, (x * endX + y * endY) / denominator))
        let projectedX = endX * progress
        let projectedY = endY * progress
        return hypot(x - projectedX, y - projectedY)
    }

    func eastWestDistance(to other: RunningCoordinate) -> CLLocationDistance {
        let projected = CLLocation(latitude: latitude, longitude: other.longitude)
        let distance = location.distance(from: projected)
        return other.longitude >= longitude ? distance : -distance
    }

    func northSouthDistance(to other: RunningCoordinate) -> CLLocationDistance {
        let projected = CLLocation(latitude: other.latitude, longitude: longitude)
        let distance = location.distance(from: projected)
        return other.latitude >= latitude ? distance : -distance
    }

    var location: CLLocation {
        CLLocation(latitude: latitude, longitude: longitude)
    }
}

private let uploadSimplificationEpsilonMeters: CLLocationDistance = 8
