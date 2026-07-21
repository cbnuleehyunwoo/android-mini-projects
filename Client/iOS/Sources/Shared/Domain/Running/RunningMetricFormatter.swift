import CoreLocation
import Foundation

enum RunningMetricFormatter {
    static func duration(_ elapsedTime: TimeInterval) -> String {
        let totalSeconds = max(0, Int(elapsedTime.rounded()))
        let hours = totalSeconds / 3_600
        let minutes = (totalSeconds % 3_600) / 60
        let seconds = totalSeconds % 60

        if hours > 0 {
            return String(format: "%d:%02d:%02d", hours, minutes, seconds)
        }

        return String(format: "%d:%02d", minutes, seconds)
    }

    static func distanceKilometers(_ distanceMeters: CLLocationDistance) -> String {
        (distanceMeters / 1_000).formatted(.number.precision(.fractionLength(2)))
    }

    static func pace(_ pace: TimeInterval?) -> String {
        guard let pace else { return "--'--\"" }
        let totalSeconds = max(0, Int(pace.rounded()))
        return String(format: "%d'%02d\"", totalSeconds / 60, totalSeconds % 60)
    }
}
