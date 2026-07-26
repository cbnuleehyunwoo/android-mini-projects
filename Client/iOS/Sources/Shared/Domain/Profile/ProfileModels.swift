import Foundation

struct HomeState: Equatable {
    let profile: UserProfile?
    let team: TeamSummary?
}

struct UserProfile: Equatable {
    let id: String
    let nickname: String
    let avatarKey: String?
    let teamId: String?
    let totalDistanceMeters: Int
    let totalDurationSeconds: Int
    let totalRunCount: Int
}

struct TeamSummary: Equatable {
    let id: String
    let name: String
    let joinCode: String?
    let ownerId: String?
    let memberCount: Int
    let isOwner: Bool
    let todayRunMemberCount: Int

    init(
        id: String,
        name: String,
        joinCode: String?,
        ownerId: String?,
        memberCount: Int,
        isOwner: Bool,
        todayRunMemberCount: Int = 0
    ) {
        self.id = id
        self.name = name
        self.joinCode = joinCode
        self.ownerId = ownerId
        self.memberCount = memberCount
        self.isOwner = isOwner
        self.todayRunMemberCount = todayRunMemberCount
    }
}

struct ProfileMutationForm: Equatable {
    var nickname: String
    var avatarKey: String?

    init(nickname: String, avatarKey: String? = "runner_default") {
        self.nickname = nickname.trimmingCharacters(in: .whitespacesAndNewlines)
        self.avatarKey = avatarKey
    }
}

extension TeamSummary {
    var runningTeam: RunningTeam? {
        guard let uuid = UUID(uuidString: id) else { return nil }

        return RunningTeam(
            id: uuid,
            name: name,
            distanceKilometers: 0,
            memberCount: memberCount,
            memberLimit: max(memberCount, 4),
            inviteCode: joinCode ?? ""
        )
    }
}
