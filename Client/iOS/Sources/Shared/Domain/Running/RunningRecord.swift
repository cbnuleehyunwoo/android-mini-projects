import CoreLocation
import Foundation

struct RunningRecord: Identifiable, Codable, Hashable {
    let id: UUID
    let startedAt: Date
    let endedAt: Date
    let elapsedTime: TimeInterval
    let distanceMeters: CLLocationDistance
    let route: [RunningCoordinate]

    init(
        id: UUID = UUID(),
        startedAt: Date,
        endedAt: Date,
        elapsedTime: TimeInterval,
        distanceMeters: CLLocationDistance,
        route: [RunningCoordinate]
    ) {
        self.id = id
        self.startedAt = startedAt
        self.endedAt = endedAt
        self.elapsedTime = elapsedTime
        self.distanceMeters = distanceMeters
        self.route = route
    }

    var distanceKilometers: Double {
        distanceMeters / 1_000
    }

    var averagePaceSecondsPerKilometer: TimeInterval? {
        guard distanceKilometers > 0.01 else { return nil }
        return elapsedTime / distanceKilometers
    }

    var estimatedCalories: Int {
        max(0, Int((distanceKilometers * 58).rounded()))
    }

    var routeCoordinates: [CLLocationCoordinate2D] {
        route.map(\.coordinate)
    }
}

struct RunningCoordinate: Codable, Hashable {
    let latitude: CLLocationDegrees
    let longitude: CLLocationDegrees

    init(_ coordinate: CLLocationCoordinate2D) {
        latitude = coordinate.latitude
        longitude = coordinate.longitude
    }

    var coordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
    }
}
