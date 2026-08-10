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
    let splits: [RunningSplit]

    init(
        id: UUID = UUID(),
        startedAt: Date,
        endedAt: Date,
        elapsedTime: TimeInterval,
        distanceMeters: CLLocationDistance,
        averagePaceSecondsPerKilometer: TimeInterval? = nil,
        calories: Int? = nil,
        route: [RunningCoordinate],
        splits: [RunningSplit] = []
    ) {
        self.id = id
        self.startedAt = startedAt
        self.endedAt = endedAt
        self.elapsedTime = elapsedTime
        self.distanceMeters = distanceMeters
        averagePaceSecondsPerKilometerOverride = averagePaceSecondsPerKilometer
        self.calories = calories
        self.route = route
        self.splits = splits
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

    func replacingSplits(with splits: [RunningSplit]) -> RunningRecord {
        RunningRecord(
            id: id,
            startedAt: startedAt,
            endedAt: endedAt,
            elapsedTime: elapsedTime,
            distanceMeters: distanceMeters,
            averagePaceSecondsPerKilometer: averagePaceSecondsPerKilometerOverride,
            calories: calories,
            route: route,
            splits: splits
        )
    }

    private enum CodingKeys: String, CodingKey {
        case id
        case startedAt
        case endedAt
        case elapsedTime
        case distanceMeters
        case averagePaceSecondsPerKilometerOverride
        case calories
        case route
        case splits
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decode(UUID.self, forKey: .id)
        startedAt = try container.decode(Date.self, forKey: .startedAt)
        endedAt = try container.decode(Date.self, forKey: .endedAt)
        elapsedTime = try container.decode(TimeInterval.self, forKey: .elapsedTime)
        distanceMeters = try container.decode(CLLocationDistance.self, forKey: .distanceMeters)
        averagePaceSecondsPerKilometerOverride = try container.decodeIfPresent(
            TimeInterval.self,
            forKey: .averagePaceSecondsPerKilometerOverride
        )
        calories = try container.decodeIfPresent(Int.self, forKey: .calories)
        route = try container.decode([RunningCoordinate].self, forKey: .route)
        splits = try container.decodeIfPresent([RunningSplit].self, forKey: .splits) ?? []
    }
}

struct RunningSplit: Codable, Hashable, Identifiable {
    let sequence: Int
    let fromDistanceMeters: CLLocationDistance
    let toDistanceMeters: CLLocationDistance
    let distanceMeters: CLLocationDistance
    let durationMillis: Int
    let paceSecondsPerKilometer: TimeInterval

    var id: Int { sequence }
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
