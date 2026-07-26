import Foundation

protocol RankingServiceProtocol {
    func fetchTeamRankings(metric: UserRankingMetric, accessToken: String) async throws -> TeamRankingBoard
    func fetchUserRankings(metric: UserRankingMetric, accessToken: String) async throws -> UserRankingBoard
    func fetchMyRankingSummary(accessToken: String) async throws -> MyRankingSummary
}

struct RankingPeriod: Equatable {
    let type: String
    let startsAt: Date?
    let endsAt: Date?
    let elapsedDays: Int
}

struct TeamRankingBoard: Equatable {
    let period: RankingPeriod
    let requiredDistanceMeters: Int
    let eligibleCount: Int
    let rankings: [TeamRankingEntry]
}

struct TeamRankingEntry: Identifiable, Equatable {
    var id: String { teamID }

    let rank: Int
    let topPercent: Int
    let eligibleCount: Int
    let teamID: String
    let teamName: String
    let distanceMeters: Int
    let durationSeconds: Int
    let averagePaceSecondsPerKilometer: Int?
    let runCount: Int
    let totalActiveDays: Int
    let averageActiveDays: Double
}

enum UserRankingMetric: String, CaseIterable, Equatable {
    case distance
    case pace
    case consistency
}

struct UserRankingBoard: Equatable {
    let period: RankingPeriod
    let metric: UserRankingMetric
    let requiredDistanceMeters: Int
    let eligibleCount: Int
    let rankings: [UserRankingEntry]
}

struct UserRankingEntry: Identifiable, Equatable {
    var id: String { userID }

    let rank: Int
    let topPercent: Int
    let eligibleCount: Int
    let userID: String
    let nickname: String
    let avatarKey: String?
    let teamID: String?
    let teamName: String?
    let distanceMeters: Int
    let durationSeconds: Int
    let averagePaceSecondsPerKilometer: Int?
    let runCount: Int
    let activeDays: Int
    let elapsedDays: Int
    let consistencyRate: Int
}

struct MyRankingSummary: Equatable {
    let period: RankingPeriod
    let eligible: Bool
    let requiredDistanceMeters: Int
    let distanceMeters: Int
    let remainingDistanceMeters: Int
    let durationSeconds: Int
    let averagePaceSecondsPerKilometer: Int?
    let runCount: Int
    let activeDays: Int
    let consistencyRate: Int
    let distanceRank: Int?
    let distanceTopPercent: Int?
    let distanceEligibleCount: Int
    let paceRank: Int?
    let paceTopPercent: Int?
    let paceEligibleCount: Int
    let consistencyRank: Int?
    let consistencyTopPercent: Int?
    let consistencyEligibleCount: Int
}

final class RankingAPIService: RankingServiceProtocol {
    private let baseURL: URL
    private let httpClient: AuthenticatedHTTPClient
    private let decoder: JSONDecoder

    init(
        baseURL: URL,
        session: URLSession = .shared,
        httpClient: AuthenticatedHTTPClient? = nil,
        decoder: JSONDecoder = JSONDecoder()
    ) {
        self.baseURL = baseURL
        self.httpClient = httpClient ?? AuthenticatedHTTPClient(session: session)
        self.decoder = decoder
    }

    convenience init(bundle: Bundle = .main, httpClient: AuthenticatedHTTPClient? = nil) throws {
        guard let baseURL = bundle.rankingAPIBaseURL else {
            throw RankingAPIError.missingBaseURL
        }

        self.init(baseURL: baseURL, httpClient: httpClient)
    }

    func fetchTeamRankings(
        metric: UserRankingMetric,
        accessToken: String
    ) async throws -> TeamRankingBoard {
        let response: TeamRankingEnvelope = try await request(
            path: metric.teamRankingPath,
            queryItems: [URLQueryItem(name: "period", value: RankingPeriodType.all)],
            accessToken: accessToken
        )
        return response.data.domain
    }

    func fetchUserRankings(
        metric: UserRankingMetric,
        accessToken: String
    ) async throws -> UserRankingBoard {
        let response: UserRankingEnvelope = try await request(
            path: metric.rankingPath,
            queryItems: [URLQueryItem(name: "period", value: RankingPeriodType.all)],
            accessToken: accessToken
        )
        return response.data.domain
    }

    func fetchMyRankingSummary(accessToken: String) async throws -> MyRankingSummary {
        let response: MyRankingSummaryEnvelope = try await request(
            path: "/rankings/me",
            queryItems: [URLQueryItem(name: "period", value: RankingPeriodType.all)],
            accessToken: accessToken
        )
        return response.data.domain
    }

    private func request<Response: Decodable>(
        path: String,
        queryItems: [URLQueryItem] = [],
        accessToken: String
    ) async throws -> Response {
        let url = baseURL.appendingAPIPath(path).appendingQueryItems(queryItems)
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        return try await send(request: request)
    }

    private func send<Response: Decodable>(request: URLRequest) async throws -> Response {
        let (data, response) = try await httpClient.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw RankingAPIError.invalidResponse
        }

        guard 200..<300 ~= httpResponse.statusCode else {
            throw RankingAPIError.requestFailed(message: decodeErrorMessage(from: data), statusCode: httpResponse.statusCode)
        }

        return try decoder.decode(Response.self, from: data)
    }

    private func decodeErrorMessage(from data: Data) -> String? {
        try? decoder.decode(RankingAPIErrorEnvelope.self, from: data).error.message
    }
}

final class MockRankingService: RankingServiceProtocol {
    func fetchTeamRankings(
        metric: UserRankingMetric,
        accessToken: String
    ) async throws -> TeamRankingBoard {
        TeamRankingBoard(
            period: Self.samplePeriod,
            requiredDistanceMeters: 10_000,
            eligibleCount: Self.sampleTeamRankings.count,
            rankings: Self.sampleTeams(for: metric)
        )
    }

    func fetchUserRankings(
        metric: UserRankingMetric,
        accessToken: String
    ) async throws -> UserRankingBoard {
        UserRankingBoard(
            period: Self.samplePeriod,
            metric: metric,
            requiredDistanceMeters: 10_000,
            eligibleCount: Self.sampleUsers.count,
            rankings: Self.sampleUsers(for: metric)
        )
    }

    func fetchMyRankingSummary(accessToken: String) async throws -> MyRankingSummary {
        MyRankingSummary(
            period: Self.samplePeriod,
            eligible: true,
            requiredDistanceMeters: 10_000,
            distanceMeters: 253_100,
            remainingDistanceMeters: 0,
            durationSeconds: 77_563,
            averagePaceSecondsPerKilometer: 307,
            runCount: 158,
            activeDays: 9,
            consistencyRate: 90,
            distanceRank: 2,
            distanceTopPercent: 1,
            distanceEligibleCount: 25,
            paceRank: 2,
            paceTopPercent: 1,
            paceEligibleCount: 25,
            consistencyRank: 2,
            consistencyTopPercent: 1,
            consistencyEligibleCount: 25
        )
    }

    private static let samplePeriod = RankingPeriod(
        type: RankingPeriodType.all,
        startsAt: nil,
        endsAt: nil,
        elapsedDays: 10
    )

    private static let sampleTeamRankings: [TeamRankingEntry] = [
        TeamRankingEntry(rank: 1, topPercent: 0, eligibleCount: 12, teamID: "team-1", teamName: "롯23데", distanceMeters: 298_300, durationSeconds: 88_000, averagePaceSecondsPerKilometer: 295, runCount: 200, totalActiveDays: 10, averageActiveDays: 2.4),
        TeamRankingEntry(rank: 2, topPercent: 1, eligibleCount: 12, teamID: "team-2", teamName: "김영희", distanceMeters: 253_100, durationSeconds: 77_563, averagePaceSecondsPerKilometer: 307, runCount: 158, totalActiveDays: 9, averageActiveDays: 2.0),
        TeamRankingEntry(rank: 3, topPercent: 8, eligibleCount: 12, teamID: "team-3", teamName: "롯데55", distanceMeters: 241_800, durationSeconds: 76_000, averagePaceSecondsPerKilometer: 318, runCount: 140, totalActiveDays: 8, averageActiveDays: 1.6),
        TeamRankingEntry(rank: 4, topPercent: 10, eligibleCount: 12, teamID: "team-4", teamName: "롯데", distanceMeters: 241_800, durationSeconds: 76_000, averagePaceSecondsPerKilometer: 330, runCount: 140, totalActiveDays: 8, averageActiveDays: 1.5),
        TeamRankingEntry(rank: 5, topPercent: 15, eligibleCount: 12, teamID: "team-5", teamName: "롯데", distanceMeters: 241_800, durationSeconds: 76_000, averagePaceSecondsPerKilometer: 342, runCount: 140, totalActiveDays: 8, averageActiveDays: 1.4),
        TeamRankingEntry(rank: 6, topPercent: 20, eligibleCount: 12, teamID: "team-6", teamName: "롯데", distanceMeters: 241_800, durationSeconds: 76_000, averagePaceSecondsPerKilometer: 354, runCount: 140, totalActiveDays: 8, averageActiveDays: 1.3),
        TeamRankingEntry(rank: 7, topPercent: 25, eligibleCount: 12, teamID: "team-7", teamName: "롯데", distanceMeters: 241_800, durationSeconds: 76_000, averagePaceSecondsPerKilometer: 366, runCount: 140, totalActiveDays: 8, averageActiveDays: 1.2),
        TeamRankingEntry(rank: 8, topPercent: 30, eligibleCount: 12, teamID: "team-8", teamName: "롯데", distanceMeters: 241_800, durationSeconds: 76_000, averagePaceSecondsPerKilometer: 378, runCount: 140, totalActiveDays: 8, averageActiveDays: 1.1),
        TeamRankingEntry(rank: 9, topPercent: 35, eligibleCount: 12, teamID: "team-9", teamName: "롯데", distanceMeters: 241_800, durationSeconds: 76_000, averagePaceSecondsPerKilometer: 390, runCount: 140, totalActiveDays: 8, averageActiveDays: 1.0),
        TeamRankingEntry(rank: 10, topPercent: 40, eligibleCount: 12, teamID: "team-10", teamName: "롯데", distanceMeters: 241_800, durationSeconds: 76_000, averagePaceSecondsPerKilometer: 402, runCount: 140, totalActiveDays: 8, averageActiveDays: 0.9),
        TeamRankingEntry(rank: 11, topPercent: 45, eligibleCount: 12, teamID: "team-11", teamName: "롯데", distanceMeters: 241_800, durationSeconds: 76_000, averagePaceSecondsPerKilometer: 414, runCount: 140, totalActiveDays: 8, averageActiveDays: 0.8)
    ]

    private static func sampleTeams(for metric: UserRankingMetric) -> [TeamRankingEntry] {
        switch metric {
        case .distance:
            return sampleTeamRankings
        case .pace:
            return reranked(sampleTeamRankings.sorted { ($0.averagePaceSecondsPerKilometer ?? Int.max) < ($1.averagePaceSecondsPerKilometer ?? Int.max) })
        case .consistency:
            return reranked(sampleTeamRankings.sorted { $0.averageActiveDays > $1.averageActiveDays })
        }
    }

    private static func reranked(_ entries: [TeamRankingEntry]) -> [TeamRankingEntry] {
        entries.enumerated().map { index, entry in
            TeamRankingEntry(
                rank: index + 1,
                topPercent: entry.topPercent,
                eligibleCount: entry.eligibleCount,
                teamID: entry.teamID,
                teamName: entry.teamName,
                distanceMeters: entry.distanceMeters,
                durationSeconds: entry.durationSeconds,
                averagePaceSecondsPerKilometer: entry.averagePaceSecondsPerKilometer,
                runCount: entry.runCount,
                totalActiveDays: entry.totalActiveDays,
                averageActiveDays: entry.averageActiveDays
            )
        }
    }

    private static let sampleUsers: [UserRankingEntry] = [
        UserRankingEntry(rank: 1, topPercent: 0, eligibleCount: 25, userID: "user-1", nickname: "롯23데", avatarKey: nil, teamID: "team-1", teamName: "롯23데", distanceMeters: 298_300, durationSeconds: 87_016, averagePaceSecondsPerKilometer: 292, runCount: 200, activeDays: 10, elapsedDays: 10, consistencyRate: 100),
        UserRankingEntry(rank: 2, topPercent: 1, eligibleCount: 25, userID: "user-2", nickname: "김영희", avatarKey: nil, teamID: "team-2", teamName: "김영희", distanceMeters: 253_100, durationSeconds: 77_563, averagePaceSecondsPerKilometer: 307, runCount: 158, activeDays: 9, elapsedDays: 10, consistencyRate: 90),
        UserRankingEntry(rank: 3, topPercent: 8, eligibleCount: 25, userID: "user-3", nickname: "롯데55", avatarKey: nil, teamID: "team-3", teamName: "롯데55", distanceMeters: 241_800, durationSeconds: 76_884, averagePaceSecondsPerKilometer: 318, runCount: 140, activeDays: 8, elapsedDays: 10, consistencyRate: 80)
    ] + (4...10).map { index in
        UserRankingEntry(rank: index, topPercent: index * 5, eligibleCount: 25, userID: "user-\(index)", nickname: "롯데", avatarKey: nil, teamID: nil, teamName: nil, distanceMeters: 241_800, durationSeconds: 80_000 + index * 1_000, averagePaceSecondsPerKilometer: 318 + (index - 3) * 12, runCount: 140, activeDays: 8, elapsedDays: 10, consistencyRate: 80)
    }

    private static func sampleUsers(for metric: UserRankingMetric) -> [UserRankingEntry] {
        switch metric {
        case .distance:
            return sampleUsers
        case .pace:
            return sampleUsers.sorted { ($0.averagePaceSecondsPerKilometer ?? Int.max) < ($1.averagePaceSecondsPerKilometer ?? Int.max) }
                .enumerated()
                .map { index, entry in
                    UserRankingEntry(rank: index + 1, topPercent: entry.topPercent, eligibleCount: entry.eligibleCount, userID: entry.userID, nickname: entry.nickname, avatarKey: entry.avatarKey, teamID: entry.teamID, teamName: entry.teamName, distanceMeters: entry.distanceMeters, durationSeconds: entry.durationSeconds, averagePaceSecondsPerKilometer: entry.averagePaceSecondsPerKilometer, runCount: entry.runCount, activeDays: entry.activeDays, elapsedDays: entry.elapsedDays, consistencyRate: entry.consistencyRate)
                }
        case .consistency:
            return sampleUsers.sorted { $0.activeDays > $1.activeDays }
                .enumerated()
                .map { index, entry in
                    UserRankingEntry(rank: index + 1, topPercent: entry.topPercent, eligibleCount: entry.eligibleCount, userID: entry.userID, nickname: entry.nickname, avatarKey: entry.avatarKey, teamID: entry.teamID, teamName: entry.teamName, distanceMeters: entry.distanceMeters, durationSeconds: entry.durationSeconds, averagePaceSecondsPerKilometer: entry.averagePaceSecondsPerKilometer, runCount: entry.runCount, activeDays: entry.activeDays, elapsedDays: entry.elapsedDays, consistencyRate: entry.consistencyRate)
                }
        }
    }
}

enum RankingAPIError: LocalizedError, Equatable {
    case invalidResponse
    case missingBaseURL
    case requestFailed(message: String?, statusCode: Int)

    var errorDescription: String? {
        switch self {
        case .invalidResponse:
            return "랭킹 응답을 확인해주세요."
        case .missingBaseURL:
            return "랭킹 API 주소를 확인해주세요."
        case let .requestFailed(message, statusCode):
            return message ?? "랭킹 요청에 실패했어요. (\(statusCode))"
        }
    }
}

private struct TeamRankingEnvelope: Decodable {
    let data: TeamRankingBoardPayload
}

private struct UserRankingEnvelope: Decodable {
    let data: UserRankingBoardPayload
}

private struct MyRankingSummaryEnvelope: Decodable {
    let data: MyRankingSummaryPayload
}

private enum RankingPeriodType {
    static let all = "all"
}

private struct PeriodPayload: Decodable {
    let type: String
    let startsAt: Date?
    let endsAt: Date?
    let elapsedDays: Int

    private enum CodingKeys: String, CodingKey {
        case type
        case startsAt
        case endsAt
        case elapsedDays
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        type = try container.decode(String.self, forKey: .type)
        startsAt = try container.decodeRankingDateIfPresent(forKey: .startsAt)
        endsAt = try container.decodeRankingDateIfPresent(forKey: .endsAt)
        elapsedDays = try container.decodeLossyInt(forKey: .elapsedDays)
    }

    var domain: RankingPeriod {
        RankingPeriod(
            type: type,
            startsAt: startsAt,
            endsAt: endsAt,
            elapsedDays: elapsedDays
        )
    }
}

private struct TeamRankingBoardPayload: Decodable {
    let period: PeriodPayload
    let requiredDistanceMeters: Int
    let eligibleCount: Int
    let rankings: [TeamRankingEntryPayload]

    private enum CodingKeys: String, CodingKey {
        case period
        case requiredDistanceMeters
        case eligibleCount
        case rankings
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        period = try container.decode(PeriodPayload.self, forKey: .period)
        requiredDistanceMeters = try container.decodeLossyInt(forKey: .requiredDistanceMeters)
        eligibleCount = try container.decodeLossyInt(forKey: .eligibleCount)
        rankings = try container.decode([TeamRankingEntryPayload].self, forKey: .rankings)
    }

    var domain: TeamRankingBoard {
        TeamRankingBoard(
            period: period.domain,
            requiredDistanceMeters: requiredDistanceMeters,
            eligibleCount: eligibleCount,
            rankings: rankings.map(\.domain)
        )
    }
}

private struct TeamRankingEntryPayload: Decodable {
    let rank: Int
    let topPercent: Int
    let eligibleCount: Int
    let teamId: String
    let teamName: String
    let distanceMeters: Int
    let durationSeconds: Int
    let averagePaceSecondsPerKm: Int?
    let runCount: Int
    let totalActiveDays: Int
    let averageActiveDays: Double

    private enum CodingKeys: String, CodingKey {
        case rank
        case topPercent
        case eligibleCount
        case teamId
        case teamName
        case distanceMeters
        case durationSeconds
        case averagePaceSecondsPerKm
        case runCount
        case totalActiveDays
        case averageActiveDays
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        rank = try container.decodeLossyInt(forKey: .rank)
        topPercent = try container.decodeLossyInt(forKey: .topPercent)
        eligibleCount = try container.decodeLossyInt(forKey: .eligibleCount)
        teamName = try container.decode(String.self, forKey: .teamName)
        teamId = try container.decode(String.self, forKey: .teamId)
        distanceMeters = try container.decodeLossyInt(forKey: .distanceMeters)
        durationSeconds = try container.decodeLossyInt(forKey: .durationSeconds)
        averagePaceSecondsPerKm = try container.decodeLossyIntIfPresent(forKey: .averagePaceSecondsPerKm)
        runCount = try container.decodeLossyInt(forKey: .runCount)
        totalActiveDays = try container.decodeLossyInt(forKey: .totalActiveDays)
        averageActiveDays = try container.decodeLossyDouble(forKey: .averageActiveDays)
    }

    var domain: TeamRankingEntry {
        TeamRankingEntry(
            rank: rank,
            topPercent: topPercent,
            eligibleCount: eligibleCount,
            teamID: teamId,
            teamName: teamName,
            distanceMeters: distanceMeters,
            durationSeconds: durationSeconds,
            averagePaceSecondsPerKilometer: averagePaceSecondsPerKm,
            runCount: runCount,
            totalActiveDays: totalActiveDays,
            averageActiveDays: averageActiveDays
        )
    }
}

private struct UserRankingBoardPayload: Decodable {
    let period: PeriodPayload
    let metric: UserRankingMetric
    let requiredDistanceMeters: Int
    let eligibleCount: Int
    let rankings: [UserRankingEntryPayload]

    private enum CodingKeys: String, CodingKey {
        case period
        case metric
        case requiredDistanceMeters
        case eligibleCount
        case rankings
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        period = try container.decode(PeriodPayload.self, forKey: .period)
        metric = try UserRankingMetric(apiValue: container.decode(String.self, forKey: .metric), codingPath: container.codingPath + [CodingKeys.metric])
        requiredDistanceMeters = try container.decodeLossyInt(forKey: .requiredDistanceMeters)
        eligibleCount = try container.decodeLossyInt(forKey: .eligibleCount)
        rankings = try container.decode([UserRankingEntryPayload].self, forKey: .rankings)
    }

    var domain: UserRankingBoard {
        UserRankingBoard(
            period: period.domain,
            metric: metric,
            requiredDistanceMeters: requiredDistanceMeters,
            eligibleCount: eligibleCount,
            rankings: rankings.map(\.domain)
        )
    }
}

private struct UserRankingEntryPayload: Decodable {
    let rank: Int
    let topPercent: Int
    let eligibleCount: Int
    let userId: String
    let nickname: String
    let avatarKey: String?
    let teamId: String?
    let teamName: String?
    let distanceMeters: Int
    let durationSeconds: Int
    let averagePaceSecondsPerKm: Int?
    let runCount: Int
    let activeDays: Int
    let elapsedDays: Int
    let consistencyRate: Int

    private enum CodingKeys: String, CodingKey {
        case rank
        case topPercent
        case eligibleCount
        case userId
        case nickname
        case avatarKey
        case teamId
        case teamName
        case distanceMeters
        case durationSeconds
        case averagePaceSecondsPerKm
        case runCount
        case activeDays
        case elapsedDays
        case consistencyRate
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        rank = try container.decodeLossyInt(forKey: .rank)
        topPercent = try container.decodeLossyInt(forKey: .topPercent)
        eligibleCount = try container.decodeLossyInt(forKey: .eligibleCount)
        userId = try container.decode(String.self, forKey: .userId)
        nickname = try container.decode(String.self, forKey: .nickname)
        avatarKey = try container.decodeStringIfPresent(forKey: .avatarKey)
        teamId = try container.decodeStringIfPresent(forKey: .teamId)
        teamName = try container.decodeStringIfPresent(forKey: .teamName)
        distanceMeters = try container.decodeLossyInt(forKey: .distanceMeters)
        durationSeconds = try container.decodeLossyInt(forKey: .durationSeconds)
        averagePaceSecondsPerKm = try container.decodeLossyIntIfPresent(forKey: .averagePaceSecondsPerKm)
        runCount = try container.decodeLossyInt(forKey: .runCount)
        activeDays = try container.decodeLossyInt(forKey: .activeDays)
        elapsedDays = try container.decodeLossyInt(forKey: .elapsedDays)
        consistencyRate = try container.decodeLossyInt(forKey: .consistencyRate)
    }

    var domain: UserRankingEntry {
        UserRankingEntry(
            rank: rank,
            topPercent: topPercent,
            eligibleCount: eligibleCount,
            userID: userId,
            nickname: nickname,
            avatarKey: avatarKey,
            teamID: teamId,
            teamName: teamName,
            distanceMeters: distanceMeters,
            durationSeconds: durationSeconds,
            averagePaceSecondsPerKilometer: averagePaceSecondsPerKm,
            runCount: runCount,
            activeDays: activeDays,
            elapsedDays: elapsedDays,
            consistencyRate: consistencyRate
        )
    }
}

private struct MyRankingSummaryPayload: Decodable {
    let period: PeriodPayload
    let eligible: Bool
    let requiredDistanceMeters: Int
    let distanceMeters: Int
    let remainingDistanceMeters: Int
    let durationSeconds: Int
    let averagePaceSecondsPerKm: Int?
    let runCount: Int
    let activeDays: Int
    let consistencyRate: Int
    let distanceRank: Int?
    let distanceTopPercent: Int?
    let distanceEligibleCount: Int
    let paceRank: Int?
    let paceTopPercent: Int?
    let paceEligibleCount: Int
    let consistencyRank: Int?
    let consistencyTopPercent: Int?
    let consistencyEligibleCount: Int

    private enum CodingKeys: String, CodingKey {
        case period
        case eligible
        case requiredDistanceMeters
        case distanceMeters
        case remainingDistanceMeters
        case durationSeconds
        case averagePaceSecondsPerKm
        case runCount
        case activeDays
        case consistencyRate
        case distanceRank
        case distanceTopPercent
        case distanceEligibleCount
        case paceRank
        case paceTopPercent
        case paceEligibleCount
        case consistencyRank
        case consistencyTopPercent
        case consistencyEligibleCount
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        period = try container.decode(PeriodPayload.self, forKey: .period)
        eligible = try container.decode(Bool.self, forKey: .eligible)
        requiredDistanceMeters = try container.decodeLossyInt(forKey: .requiredDistanceMeters)
        distanceMeters = try container.decodeLossyInt(forKey: .distanceMeters)
        remainingDistanceMeters = try container.decodeLossyInt(forKey: .remainingDistanceMeters)
        durationSeconds = try container.decodeLossyInt(forKey: .durationSeconds)
        averagePaceSecondsPerKm = try container.decodeLossyIntIfPresent(forKey: .averagePaceSecondsPerKm)
        runCount = try container.decodeLossyInt(forKey: .runCount)
        activeDays = try container.decodeLossyInt(forKey: .activeDays)
        consistencyRate = try container.decodeLossyInt(forKey: .consistencyRate)
        distanceRank = try container.decodeLossyIntIfPresent(forKey: .distanceRank)
        distanceTopPercent = try container.decodeLossyIntIfPresent(forKey: .distanceTopPercent)
        distanceEligibleCount = try container.decodeLossyInt(forKey: .distanceEligibleCount)
        paceRank = try container.decodeLossyIntIfPresent(forKey: .paceRank)
        paceTopPercent = try container.decodeLossyIntIfPresent(forKey: .paceTopPercent)
        paceEligibleCount = try container.decodeLossyInt(forKey: .paceEligibleCount)
        consistencyRank = try container.decodeLossyIntIfPresent(forKey: .consistencyRank)
        consistencyTopPercent = try container.decodeLossyIntIfPresent(forKey: .consistencyTopPercent)
        consistencyEligibleCount = try container.decodeLossyInt(forKey: .consistencyEligibleCount)
    }

    var domain: MyRankingSummary {
        MyRankingSummary(
            period: period.domain,
            eligible: eligible,
            requiredDistanceMeters: requiredDistanceMeters,
            distanceMeters: distanceMeters,
            remainingDistanceMeters: remainingDistanceMeters,
            durationSeconds: durationSeconds,
            averagePaceSecondsPerKilometer: averagePaceSecondsPerKm,
            runCount: runCount,
            activeDays: activeDays,
            consistencyRate: consistencyRate,
            distanceRank: distanceRank,
            distanceTopPercent: distanceTopPercent,
            distanceEligibleCount: distanceEligibleCount,
            paceRank: paceRank,
            paceTopPercent: paceTopPercent,
            paceEligibleCount: paceEligibleCount,
            consistencyRank: consistencyRank,
            consistencyTopPercent: consistencyTopPercent,
            consistencyEligibleCount: consistencyEligibleCount
        )
    }
}

private struct RankingAPIErrorEnvelope: Decodable {
    let error: RankingAPIErrorPayload
}

private struct RankingAPIErrorPayload: Decodable {
    let message: String
}

extension KeyedDecodingContainer {
    func decodeLossyInt(forKey key: Key) throws -> Int {
        if let value = try? decode(Int.self, forKey: key) {
            return value
        }

        if let value = try? decode(Double.self, forKey: key) {
            return Int(value.rounded())
        }

        if let value = try? decode(String.self, forKey: key), let number = Double(value) {
            return Int(number.rounded())
        }

        throw DecodingError.typeMismatch(
            Int.self,
            DecodingError.Context(
                codingPath: codingPath + [key],
                debugDescription: "Expected Int-compatible value."
            )
        )
    }

    func decodeLossyIntIfPresent(forKey key: Key) throws -> Int? {
        guard contains(key), try !decodeNil(forKey: key) else { return nil }

        if let value = try? decode(Int.self, forKey: key) {
            return value
        }

        if let value = try? decode(Double.self, forKey: key) {
            return Int(value.rounded())
        }

        if let value = try? decode(String.self, forKey: key), let number = Double(value) {
            return Int(number.rounded())
        }

        throw DecodingError.typeMismatch(
            Int.self,
            DecodingError.Context(
                codingPath: codingPath + [key],
                debugDescription: "Expected Int-compatible value."
            )
        )
    }

    func decodeLossyDouble(forKey key: Key) throws -> Double {
        if let value = try? decode(Double.self, forKey: key) {
            return value
        }

        if let value = try? decode(Int.self, forKey: key) {
            return Double(value)
        }

        if let value = try? decode(String.self, forKey: key), let number = Double(value) {
            return number
        }

        throw DecodingError.typeMismatch(
            Double.self,
            DecodingError.Context(
                codingPath: codingPath + [key],
                debugDescription: "Expected Double-compatible value."
            )
        )
    }

    func decodeLossyDoubleIfPresent(forKey key: Key) throws -> Double? {
        guard contains(key), try !decodeNil(forKey: key) else { return nil }

        if let value = try? decode(Double.self, forKey: key) {
            return value
        }

        if let value = try? decode(Int.self, forKey: key) {
            return Double(value)
        }

        if let value = try? decode(String.self, forKey: key), let number = Double(value) {
            return number
        }

        throw DecodingError.typeMismatch(
            Double.self,
            DecodingError.Context(
                codingPath: codingPath + [key],
                debugDescription: "Expected Double-compatible value."
            )
        )
    }

    func decodeStringIfPresent(forKey key: Key) throws -> String? {
        guard contains(key), try !decodeNil(forKey: key) else { return nil }

        if let value = try? decode(String.self, forKey: key) {
            return value.isEmpty ? nil : value
        }

        throw DecodingError.typeMismatch(
            String.self,
            DecodingError.Context(
                codingPath: codingPath + [key],
                debugDescription: "Expected String value."
            )
        )
    }

    func decodeRankingDateIfPresent(forKey key: Key) throws -> Date? {
        guard contains(key), try !decodeNil(forKey: key) else { return nil }

        let value = try decode(String.self, forKey: key)
        if let date = RankingDateCoder.dateTime(from: value) {
            return date
        }

        throw DecodingError.dataCorrupted(
            DecodingError.Context(
                codingPath: codingPath + [key],
                debugDescription: "Expected ISO8601 ranking date."
            )
        )
    }
}

private extension UserRankingMetric {
    init(apiValue: String, codingPath: [CodingKey]) throws {
        switch apiValue {
        case "distance":
            self = .distance
        case "pace":
            self = .pace
        case "consistency", "count":
            self = .consistency
        default:
            throw DecodingError.dataCorrupted(
                DecodingError.Context(
                    codingPath: codingPath,
                    debugDescription: "Unsupported ranking metric value: \(apiValue)"
                )
            )
        }
    }

    var teamRankingPath: String {
        switch self {
        case .distance:
            return "/rankings/teams/distance"
        case .pace:
            return "/rankings/teams/pace"
        case .consistency:
            return "/rankings/teams/count"
        }
    }

    var rankingPath: String {
        switch self {
        case .distance:
            return "/rankings/users/distance"
        case .pace:
            return "/rankings/users/pace"
        case .consistency:
            return "/rankings/users/count"
        }
    }
}

private enum RankingDateCoder {
    private static var productCalendar: Calendar {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(identifier: "Asia/Seoul") ?? .current
        return calendar
    }

    private static let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = TimeZone(identifier: "Asia/Seoul")
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()

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

    static func dateString(from date: Date) -> String {
        dateFormatter.string(from: date)
    }

    static func dateTime(from string: String) -> Date? {
        dateTimeFormatter.date(from: string) ?? fallbackDateTimeFormatter.date(from: string)
    }

    static func monthInterval(year: Int, month: Int) -> DateInterval {
        let calendar = productCalendar
        let fallbackStart = Date(timeIntervalSince1970: 0)
        let start = calendar.date(from: DateComponents(year: year, month: month, day: 1)) ?? fallbackStart
        let end = calendar.date(byAdding: .month, value: 1, to: start) ?? start
        return DateInterval(start: start, end: end)
    }
}

private extension Bundle {
    var rankingAPIBaseURL: URL? {
        guard
            let baseURLString = (
                object(forInfoDictionaryKey: "RankingAPIBaseURL")
                ?? object(forInfoDictionaryKey: "APIBaseURL")
            ) as? String,
            !baseURLString.isEmpty,
            !baseURLString.hasPrefix("$(")
        else {
            return nil
        }

        return URL(string: baseURLString)?.runpamineAPIBaseURL
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
