import Foundation

protocol RankingServiceProtocol {
    func fetchCurrentSeason(date: Date?, accessToken: String) async throws -> RankingSeason
    func fetchTeamRankings(metric: UserRankingMetric, seasonID: String?, accessToken: String) async throws -> TeamRankingBoard
    func fetchUserRankings(metric: UserRankingMetric, seasonID: String?, accessToken: String) async throws -> UserRankingBoard
    func fetchMyRankingSummary(seasonID: String?, accessToken: String) async throws -> MyRankingSummary
}

struct RankingSeason: Equatable {
    let id: String
    let name: String
    let year: Int
    let month: Int
    let startsAt: Date
    let endsAt: Date
    let elapsedDays: Int?
    let requiredDistanceMeters: Int?
}

struct TeamRankingBoard: Equatable {
    let season: RankingSeason
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
    let season: RankingSeason
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
    let season: RankingSeason
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
    private let session: URLSession
    private let decoder: JSONDecoder

    init(
        baseURL: URL,
        session: URLSession = .shared,
        decoder: JSONDecoder = JSONDecoder()
    ) {
        self.baseURL = baseURL
        self.session = session
        self.decoder = decoder
    }

    convenience init(bundle: Bundle = .main) throws {
        guard let baseURL = bundle.rankingAPIBaseURL else {
            throw RankingAPIError.missingBaseURL
        }

        self.init(baseURL: baseURL)
    }

    func fetchCurrentSeason(date: Date? = nil, accessToken: String) async throws -> RankingSeason {
        let response: CurrentSeasonEnvelope = try await request(
            path: "/seasons/current",
            queryItems: date.map { [URLQueryItem(name: "date", value: RankingDateCoder.dateString(from: $0))] } ?? [],
            accessToken: accessToken
        )
        return response.data.domain
    }

    func fetchTeamRankings(
        metric: UserRankingMetric,
        seasonID: String? = nil,
        accessToken: String
    ) async throws -> TeamRankingBoard {
        let response: TeamRankingEnvelope = try await request(
            path: metric.teamRankingPath,
            queryItems: seasonID.map { [URLQueryItem(name: "seasonId", value: $0)] } ?? [],
            accessToken: accessToken
        )
        return response.data.domain
    }

    func fetchUserRankings(
        metric: UserRankingMetric,
        seasonID: String? = nil,
        accessToken: String
    ) async throws -> UserRankingBoard {
        let response: UserRankingEnvelope = try await request(
            path: metric.rankingPath,
            queryItems: seasonID.map { [URLQueryItem(name: "seasonId", value: $0)] } ?? [],
            accessToken: accessToken
        )
        return response.data.domain
    }

    func fetchMyRankingSummary(seasonID: String? = nil, accessToken: String) async throws -> MyRankingSummary {
        let response: MyRankingSummaryEnvelope = try await request(
            path: "/rankings/me",
            queryItems: seasonID.map { [URLQueryItem(name: "seasonId", value: $0)] } ?? [],
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
        let (data, response) = try await session.data(for: request)
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
    func fetchCurrentSeason(date: Date? = nil, accessToken: String) async throws -> RankingSeason {
        Self.sampleSeason
    }

    func fetchTeamRankings(
        metric: UserRankingMetric,
        seasonID: String? = nil,
        accessToken: String
    ) async throws -> TeamRankingBoard {
        TeamRankingBoard(
            season: Self.sampleSeason,
            requiredDistanceMeters: 10_000,
            eligibleCount: Self.sampleTeamRankings.count,
            rankings: Self.sampleTeams(for: metric)
        )
    }

    func fetchUserRankings(
        metric: UserRankingMetric,
        seasonID: String? = nil,
        accessToken: String
    ) async throws -> UserRankingBoard {
        UserRankingBoard(
            season: Self.sampleSeason,
            metric: metric,
            requiredDistanceMeters: 10_000,
            eligibleCount: Self.sampleUsers.count,
            rankings: Self.sampleUsers(for: metric)
        )
    }

    func fetchMyRankingSummary(seasonID: String? = nil, accessToken: String) async throws -> MyRankingSummary {
        MyRankingSummary(
            season: Self.sampleSeason,
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

    private static let sampleSeason = RankingSeason(
        id: "season-uuid",
        name: "2026-06",
        year: 2026,
        month: 6,
        startsAt: Date(timeIntervalSince1970: 1_780_272_000),
        endsAt: Date(timeIntervalSince1970: 1_782_864_000),
        elapsedDays: 10,
        requiredDistanceMeters: 10_000
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

private struct CurrentSeasonEnvelope: Decodable {
    let data: SeasonPayload
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

private struct SeasonPayload: Decodable {
    let id: String
    let name: String
    let year: Int
    let month: Int
    let startsAt: String
    let endsAt: String
    let elapsedDays: Int?
    let requiredDistanceMeters: Int?

    var domain: RankingSeason {
        RankingSeason(
            id: id,
            name: name,
            year: year,
            month: month,
            startsAt: RankingDateCoder.dateTime(from: startsAt) ?? Date(),
            endsAt: RankingDateCoder.dateTime(from: endsAt) ?? Date(),
            elapsedDays: elapsedDays,
            requiredDistanceMeters: requiredDistanceMeters
        )
    }
}

private struct TeamRankingBoardPayload: Decodable {
    let season: SeasonPayload
    let requiredDistanceMeters: Int
    let eligibleCount: Int
    let rankings: [TeamRankingEntryPayload]

    private enum CodingKeys: String, CodingKey {
        case season
        case requiredDistanceMeters
        case eligibleCount
        case rankings
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        season = try container.decode(SeasonPayload.self, forKey: .season)
        requiredDistanceMeters = try container.decodeLossyIntIfPresent(forKey: .requiredDistanceMeters) ?? 0
        eligibleCount = try container.decodeLossyIntIfPresent(forKey: .eligibleCount) ?? 0
        rankings = (try? container.decode([TeamRankingEntryPayload].self, forKey: .rankings)) ?? []
    }

    var domain: TeamRankingBoard {
        TeamRankingBoard(
            season: season.domain,
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
    let averageActiveDays: Double?

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
        topPercent = try container.decodeLossyIntIfPresent(forKey: .topPercent) ?? 0
        eligibleCount = try container.decodeLossyIntIfPresent(forKey: .eligibleCount) ?? 0
        teamName = (try container.decodeStringIfPresent(forKey: .teamName)) ?? ""
        teamId = (try container.decodeStringIfPresent(forKey: .teamId)) ?? "team-\(rank)-\(teamName)"
        distanceMeters = try container.decodeLossyIntIfPresent(forKey: .distanceMeters) ?? 0
        durationSeconds = try container.decodeLossyIntIfPresent(forKey: .durationSeconds) ?? 0
        averagePaceSecondsPerKm = try container.decodeLossyIntIfPresent(forKey: .averagePaceSecondsPerKm)
        runCount = try container.decodeLossyIntIfPresent(forKey: .runCount) ?? 0
        totalActiveDays = try container.decodeLossyIntIfPresent(forKey: .totalActiveDays) ?? 0
        averageActiveDays = try container.decodeLossyDoubleIfPresent(forKey: .averageActiveDays)
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
            averageActiveDays: averageActiveDays ?? 0
        )
    }
}

private struct UserRankingBoardPayload: Decodable {
    let season: SeasonPayload
    let metric: String
    let requiredDistanceMeters: Int
    let eligibleCount: Int
    let rankings: [UserRankingEntryPayload]

    var domain: UserRankingBoard {
        UserRankingBoard(
            season: season.domain,
            metric: UserRankingMetric(rawValue: metric) ?? .distance,
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
        topPercent = try container.decodeLossyIntIfPresent(forKey: .topPercent) ?? 0
        eligibleCount = try container.decodeLossyIntIfPresent(forKey: .eligibleCount) ?? 0
        userId = try container.decode(String.self, forKey: .userId)
        nickname = try container.decode(String.self, forKey: .nickname)
        avatarKey = try container.decodeStringIfPresent(forKey: .avatarKey)
        teamId = try container.decodeStringIfPresent(forKey: .teamId)
        teamName = try container.decodeStringIfPresent(forKey: .teamName)
        distanceMeters = try container.decodeLossyInt(forKey: .distanceMeters)
        durationSeconds = try container.decodeLossyIntIfPresent(forKey: .durationSeconds) ?? 0
        averagePaceSecondsPerKm = try container.decodeLossyIntIfPresent(forKey: .averagePaceSecondsPerKm)
        runCount = try container.decodeLossyIntIfPresent(forKey: .runCount) ?? 0
        activeDays = try container.decodeLossyIntIfPresent(forKey: .activeDays) ?? 0
        elapsedDays = try container.decodeLossyIntIfPresent(forKey: .elapsedDays) ?? 0
        consistencyRate = try container.decodeLossyIntIfPresent(forKey: .consistencyRate) ?? 0
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
    let season: SeasonPayload
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
        case season
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
        season = try container.decode(SeasonPayload.self, forKey: .season)
        eligible = (try? container.decode(Bool.self, forKey: .eligible)) ?? false
        requiredDistanceMeters = try container.decodeLossyIntIfPresent(forKey: .requiredDistanceMeters) ?? 0
        distanceMeters = try container.decodeLossyIntIfPresent(forKey: .distanceMeters) ?? 0
        remainingDistanceMeters = try container.decodeLossyIntIfPresent(forKey: .remainingDistanceMeters) ?? 0
        durationSeconds = try container.decodeLossyIntIfPresent(forKey: .durationSeconds) ?? 0
        averagePaceSecondsPerKm = try container.decodeLossyIntIfPresent(forKey: .averagePaceSecondsPerKm)
        runCount = try container.decodeLossyIntIfPresent(forKey: .runCount) ?? 0
        activeDays = try container.decodeLossyIntIfPresent(forKey: .activeDays) ?? 0
        consistencyRate = try container.decodeLossyIntIfPresent(forKey: .consistencyRate) ?? 0
        distanceRank = try container.decodeLossyIntIfPresent(forKey: .distanceRank)
        distanceTopPercent = try container.decodeLossyIntIfPresent(forKey: .distanceTopPercent)
        distanceEligibleCount = try container.decodeLossyIntIfPresent(forKey: .distanceEligibleCount) ?? 0
        paceRank = try container.decodeLossyIntIfPresent(forKey: .paceRank)
        paceTopPercent = try container.decodeLossyIntIfPresent(forKey: .paceTopPercent)
        paceEligibleCount = try container.decodeLossyIntIfPresent(forKey: .paceEligibleCount) ?? 0
        consistencyRank = try container.decodeLossyIntIfPresent(forKey: .consistencyRank)
        consistencyTopPercent = try container.decodeLossyIntIfPresent(forKey: .consistencyTopPercent)
        consistencyEligibleCount = try container.decodeLossyIntIfPresent(forKey: .consistencyEligibleCount) ?? 0
    }

    var domain: MyRankingSummary {
        MyRankingSummary(
            season: season.domain,
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

private extension KeyedDecodingContainer {
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

        return nil
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

        return nil
    }

    func decodeStringIfPresent(forKey key: Key) throws -> String? {
        guard contains(key), try !decodeNil(forKey: key) else { return nil }

        if let value = try? decode(String.self, forKey: key) {
            return value.isEmpty ? nil : value
        }

        return nil
    }
}

private extension UserRankingMetric {
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
