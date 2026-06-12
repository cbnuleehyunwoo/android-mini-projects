import CoreLocation
import Foundation

struct RunningRecord: Identifiable, Codable, Hashable {
    let id: UUID
    let startedAt: Date
    let endedAt: Date
    let elapsedTime: TimeInterval
    let distanceMeters: CLLocationDistance
    let averagePaceSecondsPerKilometerOverride: TimeInterval?
    let calories: Int?
    let route: [RunningCoordinate]

    init(
        id: UUID = UUID(),
        startedAt: Date,
        endedAt: Date,
        elapsedTime: TimeInterval,
        distanceMeters: CLLocationDistance,
        averagePaceSecondsPerKilometer: TimeInterval? = nil,
        calories: Int? = nil,
        route: [RunningCoordinate]
    ) {
        self.id = id
        self.startedAt = startedAt
        self.endedAt = endedAt
        self.elapsedTime = elapsedTime
        self.distanceMeters = distanceMeters
        averagePaceSecondsPerKilometerOverride = averagePaceSecondsPerKilometer
        self.calories = calories
        self.route = route
    }

    var distanceKilometers: Double {
        distanceMeters / 1_000
    }

    var averagePaceSecondsPerKilometer: TimeInterval? {
        if let averagePaceSecondsPerKilometerOverride {
            return averagePaceSecondsPerKilometerOverride
        }

        guard distanceKilometers > 0.01 else { return nil }
        return elapsedTime / distanceKilometers
    }

    var estimatedCalories: Int {
        if let calories {
            return calories
        }

        return max(0, Int((distanceKilometers * 58).rounded()))
    }

    var routeCoordinates: [CLLocationCoordinate2D] {
        route.map(\.coordinate)
    }
}

struct RunningCoordinate: Codable, Hashable {
    let latitude: CLLocationDegrees
    let longitude: CLLocationDegrees
    let recordedAt: Date?
    let horizontalAccuracy: CLLocationAccuracy?

    init(_ coordinate: CLLocationCoordinate2D, recordedAt: Date? = nil, horizontalAccuracy: CLLocationAccuracy? = nil) {
        latitude = coordinate.latitude
        longitude = coordinate.longitude
        self.recordedAt = recordedAt
        self.horizontalAccuracy = horizontalAccuracy
    }

    var coordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
    }
}
