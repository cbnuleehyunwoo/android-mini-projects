import Foundation

struct RunningTeam: Identifiable, Equatable, Codable {
    let id: UUID
    var name: String
    var distanceKilometers: Double
    var memberCount: Int
    var memberLimit: Int
    var inviteCode: String
}

struct TeamMember: Identifiable, Equatable {
    let id: String
    let nickname: String
    let avatarKey: String?
}

struct TeamDailySummary: Equatable {
    let team: RunningTeam
    let date: Date
    let teamTotalDistanceMeters: Int
    let completedMemberCount: Int
    let totalMemberCount: Int
    let members: [TeamDailyMember]
}

struct TeamDailyMember: Identifiable, Equatable {
    let userID: String
    let nickname: String
    let avatarKey: String?
    let distanceMeters: Int
    let durationSeconds: Int
    let averagePaceSecondsPerKilometer: Int?
    let calories: Int
    let completed: Bool

    var id: String { userID }
}

enum TeamError: LocalizedError, Equatable {
    case invalidName
    case invalidInviteCode
    case duplicatedName
    case invalidResponse
    case missingBaseURL
    case requestFailed(message: String?, statusCode: Int)
    case unauthorized
    case unavailable

    var errorDescription: String? {
        switch self {
        case .invalidName:
            return "팀 이름을 다시 확인해주세요."
        case .invalidInviteCode:
            return "팀 코드가 올바르지 않습니다."
        case .duplicatedName:
            return "중복된 팀 이름입니다."
        case .invalidResponse:
            return "팀 응답을 확인해주세요."
        case .missingBaseURL:
            return "팀 API 주소를 확인해주세요."
        case let .requestFailed(message, statusCode):
            return message ?? "팀 요청에 실패했어요. (\(statusCode))"
        case .unauthorized:
            return "로그인이 필요합니다."
        case .unavailable:
            return "잠시 후 다시 시도해주세요."
        }
    }
}
