import Foundation

@MainActor
final class TeamJoinViewModel: ObservableObject {
    @Published var inviteCode = ""
    @Published private(set) var isLoading = false
    @Published private(set) var joinedTeam: RunningTeam?
    @Published private(set) var hasError = false

    private let teamService: TeamServiceProtocol
    private let accessToken: String?

    init(teamService: TeamServiceProtocol, accessToken: String? = nil) {
        self.teamService = teamService
        self.accessToken = accessToken
    }

    var canSubmit: Bool {
        normalizedInviteCode.range(of: "^[A-Z0-9]{6}$", options: .regularExpression) != nil
    }

    private var normalizedInviteCode: String {
        inviteCode
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .uppercased()
    }

    func joinTeam() async {
        guard canSubmit, !isLoading else { return }

        isLoading = true
        hasError = false
        joinedTeam = nil

        do {
            guard let accessToken else {
                throw TeamError.unauthorized
            }

            joinedTeam = try await teamService.joinTeam(inviteCode: normalizedInviteCode, accessToken: accessToken)
        } catch {
            hasError = true
        }

        isLoading = false
    }
}
