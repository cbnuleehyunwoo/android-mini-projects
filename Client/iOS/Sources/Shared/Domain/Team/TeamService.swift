import Foundation

protocol TeamServiceProtocol {
    func fetchMyTeam() async throws -> RunningTeam?
    func createTeam(name: String) async throws -> RunningTeam
    func joinTeam(inviteCode: String) async throws -> RunningTeam
}

final class MockTeamService: TeamServiceProtocol {
    private let store: LocalAppStateStore
    private var createdTeam: RunningTeam?

    init(store: LocalAppStateStore = LocalAppStateStore()) {
        self.store = store
        createdTeam = store.loadTeam()
    }

    func fetchMyTeam() async throws -> RunningTeam? {
        try await Task.sleep(nanoseconds: 200_000_000)
        return createdTeam ?? store.loadTeam()
    }

    func createTeam(name: String) async throws -> RunningTeam {
        try await Task.sleep(nanoseconds: 350_000_000)

        let trimmed = name.trimmingCharacters(in: .whitespacesAndNewlines)
        guard TeamNameValidator.isValid(trimmed) else {
            throw TeamError.invalidName
        }

        if trimmed == "팀이름팀이름팀이름" {
            throw TeamError.duplicatedName
        }

        let team = RunningTeam(
            id: UUID(),
            name: trimmed,
            distanceKilometers: 0,
            memberCount: 1,
            memberLimit: 1,
            inviteCode: "SRN742"
        )
        createdTeam = team
        store.saveTeam(team)
        return team
    }

    func joinTeam(inviteCode: String) async throws -> RunningTeam {
        try await Task.sleep(nanoseconds: 350_000_000)

        let normalized = inviteCode
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .uppercased()

        guard normalized == "SRN742" else {
            throw TeamError.invalidInviteCode
        }

        let team = RunningTeam(
            id: UUID(),
            name: "팀이름팀이름팀이름",
            distanceKilometers: 324,
            memberCount: 3,
            memberLimit: 4,
            inviteCode: normalized
        )
        createdTeam = team
        store.saveTeam(team)
        return team
    }
}
