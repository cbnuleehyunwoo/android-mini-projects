import Foundation

final class LocalAppStateStore {
    private let defaults: UserDefaults
    private let stateKey = "runpamine.local-state.v1"

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    var state: PersistedAppState {
        get {
            guard let data = defaults.data(forKey: stateKey),
                  let state = try? JSONDecoder().decode(PersistedAppState.self, from: data) else {
                return PersistedAppState()
            }
            return state
        }
        set {
            guard let data = try? JSONEncoder().encode(newValue) else { return }
            defaults.set(data, forKey: stateKey)
        }
    }

    var hasCompletedSignup: Bool {
        state.nickname?.isEmpty == false && !state.acceptedRequiredTerms.isEmpty
    }

    var hasCompletedOnboarding: Bool {
        state.hasCompletedOnboarding
    }

    var nickname: String {
        state.nickname ?? "호이"
    }

    func saveSignup(profile: SignupProfile) {
        var nextState = state
        nextState.nickname = profile.nickname
        nextState.acceptedRequiredTerms = profile.agreedTerms
            .filter { $0.isRequired && $0.isAccepted }
            .map { $0.id.rawValue }
        state = nextState
    }

    func saveNickname(_ nickname: String) {
        var nextState = state
        nextState.nickname = nickname
        state = nextState
    }

    func markOnboardingCompleted() {
        var nextState = state
        nextState.hasCompletedOnboarding = true
        state = nextState
    }

    func logout() {
        state = PersistedAppState(hasCompletedOnboarding: state.hasCompletedOnboarding)
    }

    func saveTeam(_ team: RunningTeam) {
        var nextState = state
        nextState.team = PersistedTeam(team)
        state = nextState
    }

    func loadTeam() -> RunningTeam? {
        state.team?.runningTeam
    }
}

struct PersistedAppState: Codable {
    var nickname: String?
    var acceptedRequiredTerms: [String] = []
    var team: PersistedTeam?
    var hasCompletedOnboarding = false

    init(
        nickname: String? = nil,
        acceptedRequiredTerms: [String] = [],
        team: PersistedTeam? = nil,
        hasCompletedOnboarding: Bool = false
    ) {
        self.nickname = nickname
        self.acceptedRequiredTerms = acceptedRequiredTerms
        self.team = team
        self.hasCompletedOnboarding = hasCompletedOnboarding
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        nickname = try container.decodeIfPresent(String.self, forKey: .nickname)
        acceptedRequiredTerms = try container.decodeIfPresent([String].self, forKey: .acceptedRequiredTerms) ?? []
        team = try container.decodeIfPresent(PersistedTeam.self, forKey: .team)
        hasCompletedOnboarding = try container.decodeIfPresent(Bool.self, forKey: .hasCompletedOnboarding) ?? false
    }
}

struct PersistedTeam: Codable {
    let id: UUID
    let name: String
    let distanceKilometers: Double
    let memberCount: Int
    let memberLimit: Int
    let inviteCode: String

    init(_ team: RunningTeam) {
        id = team.id
        name = team.name
        distanceKilometers = team.distanceKilometers
        memberCount = team.memberCount
        memberLimit = team.memberLimit
        inviteCode = team.inviteCode
    }

    var runningTeam: RunningTeam {
        RunningTeam(
            id: id,
            name: name,
            distanceKilometers: distanceKilometers,
            memberCount: memberCount,
            memberLimit: memberLimit,
            inviteCode: inviteCode
        )
    }
}
