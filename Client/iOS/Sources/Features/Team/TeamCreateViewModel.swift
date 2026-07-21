import Foundation

@MainActor
final class TeamCreateViewModel: ObservableObject {
    @Published var teamName = ""
    @Published private(set) var isLoading = false
    @Published private(set) var createdTeam: RunningTeam?
    @Published private(set) var error: TeamError?

    private let teamService: TeamServiceProtocol
    private let accessToken: String?

    init(teamService: TeamServiceProtocol, accessToken: String? = nil) {
        self.teamService = teamService
        self.accessToken = accessToken
    }

    var hasValidLength: Bool {
        TeamNameValidator.hasValidLength(trimmedName)
    }

    var containsOnlyAllowedCharacters: Bool {
        !trimmedName.isEmpty && TeamNameValidator.containsOnlyAllowedCharacters(trimmedName)
    }

    var doesNotContainSpecialCharacters: Bool {
        trimmedName.isEmpty || TeamNameValidator.doesNotContainSpecialCharacters(trimmedName)
    }

    var canSubmit: Bool {
        TeamNameValidator.isValid(trimmedName)
    }

    var shouldShowDuplicateError: Bool {
        error == .duplicatedName
    }

    private var trimmedName: String {
        teamName.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    func createTeam() async {
        guard canSubmit, !isLoading else { return }
        isLoading = true
        error = nil
        createdTeam = nil

        do {
            guard let accessToken else {
                throw TeamError.unauthorized
            }

            createdTeam = try await teamService.createTeam(name: trimmedName, accessToken: accessToken)
        } catch let teamError as TeamError {
            error = teamError
        } catch {
            self.error = .unavailable
        }

        isLoading = false
    }
}
