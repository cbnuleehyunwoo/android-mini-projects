import Foundation

protocol TeamServiceProtocol {
    func fetchMyTeam(accessToken: String) async throws -> RunningTeam?
    func fetchMyTeamMembers(accessToken: String) async throws -> [TeamMember]
    func fetchDailySummary(date: Date, accessToken: String) async throws -> TeamDailySummary
    func createTeam(name: String, accessToken: String) async throws -> RunningTeam
    func joinTeam(inviteCode: String, accessToken: String) async throws -> RunningTeam
}

final class TeamAPIService: TeamServiceProtocol {
    private let baseURL: URL
    private let session: URLSession
    private let decoder: JSONDecoder
    private let encoder: JSONEncoder

    init(
        baseURL: URL,
        session: URLSession = .shared,
        decoder: JSONDecoder = JSONDecoder(),
        encoder: JSONEncoder = JSONEncoder()
    ) {
        self.baseURL = baseURL.apiBaseURL
        self.session = session
        self.decoder = decoder
        self.encoder = encoder
    }

    convenience init(bundle: Bundle = .main) throws {
        guard let baseURL = bundle.teamAPIBaseURL else {
            throw TeamError.missingBaseURL
        }

        self.init(baseURL: baseURL)
    }

    func fetchMyTeam(accessToken: String) async throws -> RunningTeam? {
        let response: OptionalTeamEnvelope = try await request(
            path: "/teams/me",
            method: "GET",
            accessToken: accessToken
        )
        return response.data?.domain
    }

    func fetchMyTeamMembers(accessToken: String) async throws -> [TeamMember] {
        let response: TeamMembersEnvelope = try await request(
            path: "/teams/me/members",
            method: "GET",
            accessToken: accessToken
        )
        return response.data.members.map(\.domain)
    }

    func fetchDailySummary(date: Date = Date(), accessToken: String) async throws -> TeamDailySummary {
        let response: TeamDailySummaryEnvelope = try await request(
            path: "/teams/me/runs",
            queryItems: [URLQueryItem(name: "date", value: TeamDateCoder.dateString(from: date))],
            method: "GET",
            accessToken: accessToken
        )
        guard let summary = response.data.domain else {
            throw TeamError.invalidResponse
        }

        return summary
    }

    func createTeam(name: String, accessToken: String) async throws -> RunningTeam {
        let response: TeamEnvelope = try await request(
            path: "/teams",
            method: "POST",
            accessToken: accessToken,
            body: CreateTeamPayload(name: name.trimmingCharacters(in: .whitespacesAndNewlines))
        )
        guard let team = response.data.domain else {
            throw TeamError.invalidResponse
        }

        return team
    }

    func joinTeam(inviteCode: String, accessToken: String) async throws -> RunningTeam {
        let response: TeamEnvelope = try await request(
            path: "/teams/join",
            method: "POST",
            accessToken: accessToken,
            body: JoinTeamPayload(joinCode: inviteCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased())
        )
        guard let team = response.data.domain else {
            throw TeamError.invalidResponse
        }

        return team
    }

    private func request<Response: Decodable>(
        path: String,
        queryItems: [URLQueryItem] = [],
        method: String,
        accessToken: String
    ) async throws -> Response {
        try await send(request: makeRequest(path: path, queryItems: queryItems, method: method, accessToken: accessToken))
    }

    private func request<Response: Decodable, Body: Encodable>(
        path: String,
        method: String,
        accessToken: String,
        body: Body
    ) async throws -> Response {
        var request = makeRequest(path: path, method: method, accessToken: accessToken)
        request.httpBody = try encoder.encode(body)
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        return try await send(request: request)
    }

    private func makeRequest(
        path: String,
        queryItems: [URLQueryItem] = [],
        method: String,
        accessToken: String
    ) -> URLRequest {
        let url = baseURL.appendingAPIPath(path).appendingQueryItems(queryItems)
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        return request
    }

    private func send<Response: Decodable>(request: URLRequest) async throws -> Response {
        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw TeamError.invalidResponse
        }

        guard 200..<300 ~= httpResponse.statusCode else {
            throw TeamError.requestFailed(message: decodeErrorMessage(from: data), statusCode: httpResponse.statusCode)
        }

        return try decoder.decode(Response.self, from: data)
    }

    private func decodeErrorMessage(from data: Data) -> String? {
        try? decoder.decode(TeamAPIErrorEnvelope.self, from: data).error.message
    }
}

final class MockTeamService: TeamServiceProtocol {
    private let store: LocalAppStateStore
    private var createdTeam: RunningTeam?

    init(store: LocalAppStateStore = LocalAppStateStore()) {
        self.store = store
        createdTeam = store.loadTeam()
    }

    func fetchMyTeam(accessToken: String) async throws -> RunningTeam? {
        try await Task.sleep(nanoseconds: 200_000_000)
        return createdTeam ?? store.loadTeam()
    }

    func fetchMyTeamMembers(accessToken: String) async throws -> [TeamMember] {
        [
            TeamMember(id: UUID().uuidString, nickname: store.nickname, avatarKey: "runner_default"),
            TeamMember(id: UUID().uuidString, nickname: "버거킹 스마일", avatarKey: "burger_default")
        ]
    }

    func fetchDailySummary(date: Date = Date(), accessToken: String) async throws -> TeamDailySummary {
        let team = createdTeam ?? store.loadTeam() ?? RunningTeam(
            id: UUID(),
            name: "팀이름팀이름팀이름",
            distanceKilometers: 324,
            memberCount: 3,
            memberLimit: 4,
            inviteCode: "SRN742"
        )

        return TeamDailySummary(
            team: team,
            date: date,
            teamTotalDistanceMeters: 324_000,
            completedMemberCount: 3,
            totalMemberCount: 4,
            members: [
                TeamDailyMember(
                    userID: "member-primary",
                    nickname: store.nickname,
                    avatarKey: "runner_default",
                    distanceMeters: 12_000,
                    durationSeconds: 600,
                    averagePaceSecondsPerKilometer: 50,
                    calories: 200,
                    completed: true
                ),
                TeamDailyMember(
                    userID: "member-burger-1",
                    nickname: "버거킹 스마일",
                    avatarKey: "burger_default",
                    distanceMeters: 0,
                    durationSeconds: 0,
                    averagePaceSecondsPerKilometer: nil,
                    calories: 0,
                    completed: false
                )
            ]
        )
    }

    func createTeam(name: String, accessToken: String) async throws -> RunningTeam {
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
            memberLimit: 30,
            inviteCode: "SRN742"
        )
        createdTeam = team
        store.saveTeam(team)
        return team
    }

    func joinTeam(inviteCode: String, accessToken: String) async throws -> RunningTeam {
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
            memberLimit: 30,
            inviteCode: normalized
        )
        createdTeam = team
        store.saveTeam(team)
        return team
    }
}

private struct TeamEnvelope: Decodable {
    let data: TeamPayload
}

private struct OptionalTeamEnvelope: Decodable {
    let data: TeamPayload?
}

private struct TeamMembersEnvelope: Decodable {
    let data: TeamMembersPayload
}

private struct TeamDailySummaryEnvelope: Decodable {
    let data: TeamDailySummaryPayload
}

private struct TeamPayload: Decodable {
    let id: String
    let name: String
    let joinCode: String
    let ownerId: String?
    let memberCount: Int
    let isOwner: Bool?

    var domain: RunningTeam? {
        guard let id = UUID(uuidString: id) else { return nil }

        return RunningTeam(
            id: id,
            name: name,
            distanceKilometers: 0,
            memberCount: memberCount,
            memberLimit: 30,
            inviteCode: joinCode
        )
    }
}

private struct TeamMembersPayload: Decodable {
    let members: [TeamMemberPayload]
}

private struct TeamMemberPayload: Decodable {
    let id: String
    let nickname: String
    let avatarKey: String?

    var domain: TeamMember {
        TeamMember(id: id, nickname: nickname, avatarKey: avatarKey)
    }
}

private struct TeamDailySummaryPayload: Decodable {
    let team: TeamIdentityPayload
    let date: String
    let teamTotalDistanceMeters: Int
    let completedMemberCount: Int
    let totalMemberCount: Int
    let members: [TeamDailyMemberPayload]

    var domain: TeamDailySummary? {
        guard
            let team = team.domain(
                totalDistanceMeters: teamTotalDistanceMeters,
                totalMemberCount: totalMemberCount
            ),
            let date = TeamDateCoder.date(from: date)
        else {
            return nil
        }

        return TeamDailySummary(
            team: team,
            date: date,
            teamTotalDistanceMeters: teamTotalDistanceMeters,
            completedMemberCount: completedMemberCount,
            totalMemberCount: totalMemberCount,
            members: members.map(\.domain)
        )
    }
}

private struct TeamIdentityPayload: Decodable {
    let id: String
    let name: String
    let joinCode: String
    let ownerId: String?

    func domain(totalDistanceMeters: Int, totalMemberCount: Int) -> RunningTeam? {
        guard let id = UUID(uuidString: id) else { return nil }

        return RunningTeam(
            id: id,
            name: name,
            distanceKilometers: Double(totalDistanceMeters) / 1_000,
            memberCount: totalMemberCount,
            memberLimit: max(totalMemberCount, 30),
            inviteCode: joinCode
        )
    }
}

private struct TeamDailyMemberPayload: Decodable {
    let userId: String
    let nickname: String
    let avatarKey: String?
    let distanceMeters: Int
    let durationSeconds: Int
    let averagePaceSecondsPerKm: Int?
    let calories: Int
    let completed: Bool

    var domain: TeamDailyMember {
        TeamDailyMember(
            userID: userId,
            nickname: nickname,
            avatarKey: avatarKey,
            distanceMeters: distanceMeters,
            durationSeconds: durationSeconds,
            averagePaceSecondsPerKilometer: averagePaceSecondsPerKm,
            calories: calories,
            completed: completed
        )
    }
}

private struct CreateTeamPayload: Encodable {
    let name: String
}

private struct JoinTeamPayload: Encodable {
    let joinCode: String
}

private struct TeamAPIErrorEnvelope: Decodable {
    let error: TeamAPIErrorPayload
}

private struct TeamAPIErrorPayload: Decodable {
    let message: String
}

private enum TeamDateCoder {
    private static let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = TimeZone(identifier: "Asia/Seoul")
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()

    static func dateString(from date: Date) -> String {
        dateFormatter.string(from: date)
    }

    static func date(from string: String) -> Date? {
        dateFormatter.date(from: string)
    }
}

private extension Bundle {
    var teamAPIBaseURL: URL? {
        guard
            let baseURLString = (
                object(forInfoDictionaryKey: "TeamAPIBaseURL")
                ?? object(forInfoDictionaryKey: "ProfileAPIBaseURL")
            ) as? String,
            !baseURLString.isEmpty,
            !baseURLString.hasPrefix("$(")
        else {
            return nil
        }

        return URL(string: baseURLString)
    }
}

private extension URL {
    var apiBaseURL: URL {
        let normalizedURL = absoluteString.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        guard host?.hasSuffix(".supabase.co") == true, path.isEmpty || path == "/" else {
            return URL(string: normalizedURL) ?? self
        }

        return URL(string: "\(normalizedURL)/functions/v1/api") ?? self
    }

    func appendingAPIPath(_ path: String) -> URL {
        var url = self
        path
            .split(separator: "/")
            .forEach { url.appendPathComponent(String($0)) }
        return url
    }

    func appendingQueryItems(_ queryItems: [URLQueryItem]) -> URL {
        guard !queryItems.isEmpty else { return self }

        var components = URLComponents(url: self, resolvingAgainstBaseURL: false)
        components?.queryItems = queryItems
        return components?.url ?? self
    }
}
