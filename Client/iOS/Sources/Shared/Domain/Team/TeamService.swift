import Foundation

protocol TeamServiceProtocol {
    func fetchMyTeam(accessToken: String) async throws -> RunningTeam?
    func fetchMyTeamMembers(accessToken: String) async throws -> [TeamMember]
    func fetchDailySummary(date: Date, accessToken: String) async throws -> TeamDailySummary
    func fetchMyTeamSeasonStats(seasonID: String?, accessToken: String) async throws -> TeamSeasonStats
    func createTeam(name: String, accessToken: String) async throws -> RunningTeam
    func joinTeam(inviteCode: String, accessToken: String) async throws -> RunningTeam
    func leaveTeam(accessToken: String) async throws -> TeamLeaveResult
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
        self.baseURL = baseURL
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

    func fetchMyTeamSeasonStats(seasonID: String? = nil, accessToken: String) async throws -> TeamSeasonStats {
        var queryItems: [URLQueryItem] = []
        if let seasonID, !seasonID.isEmpty {
            queryItems.append(URLQueryItem(name: "seasonId", value: seasonID))
        }

        let response: TeamSeasonStatsEnvelope = try await request(
            path: "/teams/me/season-stats",
            queryItems: queryItems,
            method: "GET",
            accessToken: accessToken
        )

        guard let stats = response.data.domain else {
            throw TeamError.invalidResponse
        }

        return stats
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

    func leaveTeam(accessToken: String) async throws -> TeamLeaveResult {
        let response: TeamLeaveEnvelope = try await request(
            path: "/teams/me",
            method: "DELETE",
            accessToken: accessToken
        )
        return response.data.domain
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

    func fetchMyTeamSeasonStats(seasonID: String? = nil, accessToken: String) async throws -> TeamSeasonStats {
        let team = createdTeam ?? store.loadTeam() ?? RunningTeam(
            id: UUID(),
            name: "팀이름팀이름팀이름",
            distanceKilometers: 324,
            memberCount: 3,
            memberLimit: 4,
            inviteCode: "SRN742"
        )
        let now = Date()

        return TeamSeasonStats(
            season: TeamSeason(
                id: seasonID ?? "mock-season",
                name: "2026-06",
                year: 2026,
                month: 6,
                startsAt: now,
                endsAt: now,
                elapsedDays: 10
            ),
            team: TeamSeasonTeam(
                id: team.id.uuidString,
                name: team.name,
                ownerID: "member-primary"
            ),
            members: [
                TeamSeasonMember(
                    id: "member-primary",
                    nickname: store.nickname,
                    avatarKey: "runner_default",
                    seasonDistanceMeters: 42_800,
                    seasonDurationSeconds: 13_200,
                    seasonCalories: 1_200,
                    seasonRunCount: 8,
                    seasonActiveDays: 6,
                    averagePaceSecondsPerKilometer: 308,
                    consecutiveRunDays: 5
                ),
                TeamSeasonMember(
                    id: "member-burger-1",
                    nickname: "버거킹 스마일",
                    avatarKey: "burger_default",
                    seasonDistanceMeters: 3_800,
                    seasonDurationSeconds: 1_400,
                    seasonCalories: 120,
                    seasonRunCount: 2,
                    seasonActiveDays: 2,
                    averagePaceSecondsPerKilometer: 368,
                    consecutiveRunDays: -5
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

    func leaveTeam(accessToken: String) async throws -> TeamLeaveResult {
        try await Task.sleep(nanoseconds: 250_000_000)
        createdTeam = nil
        store.clearTeam()
        return TeamLeaveResult(left: true, teamDeleted: false, newOwnerID: nil)
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

private struct TeamSeasonStatsEnvelope: Decodable {
    let data: TeamSeasonStatsPayload
}

private struct TeamLeaveEnvelope: Decodable {
    let data: TeamLeavePayload
}

private struct TeamLeavePayload: Decodable {
    let left: Bool
    let teamDeleted: Bool
    let newOwnerId: String?

    var domain: TeamLeaveResult {
        TeamLeaveResult(left: left, teamDeleted: teamDeleted, newOwnerID: newOwnerId)
    }
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

private struct TeamSeasonStatsPayload: Decodable {
    let season: TeamSeasonPayload
    let team: TeamSeasonTeamPayload
    let members: [TeamSeasonMemberPayload]

    var domain: TeamSeasonStats? {
        guard let season = season.domain else { return nil }

        return TeamSeasonStats(
            season: season,
            team: team.domain,
            members: members.map(\.domain)
        )
    }
}

private struct TeamSeasonPayload: Decodable {
    let id: String
    let name: String
    let year: Int
    let month: Int
    let startsAt: String
    let endsAt: String
    let elapsedDays: Int

    var domain: TeamSeason? {
        guard
            let startsAt = TeamDateCoder.dateTime(from: startsAt),
            let endsAt = TeamDateCoder.dateTime(from: endsAt)
        else {
            return nil
        }

        return TeamSeason(
            id: id,
            name: name,
            year: year,
            month: month,
            startsAt: startsAt,
            endsAt: endsAt,
            elapsedDays: elapsedDays
        )
    }
}

private struct TeamSeasonTeamPayload: Decodable {
    let id: String
    let name: String
    let ownerId: String

    var domain: TeamSeasonTeam {
        TeamSeasonTeam(id: id, name: name, ownerID: ownerId)
    }
}

private struct TeamSeasonMemberPayload: Decodable {
    let id: String
    let nickname: String
    let avatarKey: String?
    let seasonDistanceMeters: Int
    let seasonDurationSeconds: Int
    let seasonCalories: Int
    let seasonRunCount: Int
    let seasonActiveDays: Int
    let averagePaceSecondsPerKm: Int?
    let consecutiveRunDays: Int

    var domain: TeamSeasonMember {
        TeamSeasonMember(
            id: id,
            nickname: nickname,
            avatarKey: avatarKey,
            seasonDistanceMeters: seasonDistanceMeters,
            seasonDurationSeconds: seasonDurationSeconds,
            seasonCalories: seasonCalories,
            seasonRunCount: seasonRunCount,
            seasonActiveDays: seasonActiveDays,
            averagePaceSecondsPerKilometer: averagePaceSecondsPerKm,
            consecutiveRunDays: consecutiveRunDays
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
    private static let dateTimeFormatter: ISO8601DateFormatter = {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        return formatter
    }()

    private static let fallbackDateTimeFormatter: ISO8601DateFormatter = {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime]
        return formatter
    }()

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

    static func dateTime(from string: String) -> Date? {
        dateTimeFormatter.date(from: string) ?? fallbackDateTimeFormatter.date(from: string)
    }
}

private extension Bundle {
    var teamAPIBaseURL: URL? {
        guard
            let baseURLString = (
                object(forInfoDictionaryKey: "TeamAPIBaseURL")
                ?? object(forInfoDictionaryKey: "APIBaseURL")
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
