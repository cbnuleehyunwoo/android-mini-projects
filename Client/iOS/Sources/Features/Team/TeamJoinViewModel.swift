import Foundation

@MainActor
final class TeamJoinViewModel: ObservableObject {
    @Published var inviteCode = ""
    @Published private(set) var isLoading = false
    @Published private(set) var joinedTeam: RunningTeam?
    @Published private(set) var hasError = false

    private let teamService: TeamServiceProtocol

    init(teamService: TeamServiceProtocol) {
        self.teamService = teamService
    }

    var canSubmit: Bool {
        inviteCode.trimmingCharacters(in: .whitespacesAndNewlines).count >= 6
    }

    func joinTeam() async {
        guard canSubmit, !isLoading else { return }

        isLoading = true
        hasError = false
        joinedTeam = nil

        do {
            joinedTeam = try await teamService.joinTeam(inviteCode: inviteCode)
        } catch {
            hasError = true
        }

        isLoading = false
    }
}
