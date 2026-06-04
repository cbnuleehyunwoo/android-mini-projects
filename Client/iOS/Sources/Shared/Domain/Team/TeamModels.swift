import Foundation

struct RunningTeam: Identifiable, Equatable, Codable {
    let id: UUID
    var name: String
    var distanceKilometers: Double
    var memberCount: Int
    var memberLimit: Int
    var inviteCode: String
}
