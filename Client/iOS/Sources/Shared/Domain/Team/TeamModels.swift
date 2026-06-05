import Foundation

struct RunningTeam: Identifiable, Equatable, Codable {
    let id: UUID
    var name: String
    var distanceKilometers: Double
    var memberCount: Int
    var memberLimit: Int
    var inviteCode: String
}

enum TeamError: LocalizedError, Equatable {
    case invalidName
    case invalidInviteCode
    case duplicatedName
    case unavailable

    var errorDescription: String? {
        switch self {
        case .invalidName:
            return "팀 이름을 다시 확인해주세요."
        case .invalidInviteCode:
            return "팀 코드가 올바르지 않습니다."
        case .duplicatedName:
            return "중복된 팀 이름입니다."
        case .unavailable:
            return "잠시 후 다시 시도해주세요."
        }
    }
}
