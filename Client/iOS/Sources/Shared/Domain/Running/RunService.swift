import Foundation

protocol RunServiceProtocol {
    func createRun(record: RunningRecord, accessToken: String) async throws -> CreatedRun
    func fetchWeeklyRuns(anchorDate: Date, accessToken: String) async throws -> RunPeriodSummary
    func fetchMonthlyRuns(year: Int, month: Int, accessToken: String) async throws -> RunPeriodSummary
    func fetchRunDetail(runID: String, accessToken: String) async throws -> RunningRecord
    func fetchRunSplits(runID: String, accessToken: String) async throws -> [RunningSplit]
}

final class RunAPIService: RunServiceProtocol {
    private let baseURL: URL
    private let httpClient: AuthenticatedHTTPClient
    private let decoder: JSONDecoder
    private let encoder: JSONEncoder

    init(
        baseURL: URL,
        session: URLSession = .shared,
        httpClient: AuthenticatedHTTPClient? = nil,
        decoder: JSONDecoder = JSONDecoder(),
        encoder: JSONEncoder = JSONEncoder()
    ) {
        self.baseURL = baseURL
        self.httpClient = httpClient ?? AuthenticatedHTTPClient(session: session)
        self.decoder = decoder
        self.encoder = encoder
    }

    convenience init(bundle: Bundle = .main, httpClient: AuthenticatedHTTPClient? = nil) throws {
        guard let baseURL = bundle.runAPIBaseURL else {
            throw RunAPIError.missingBaseURL
        }

        self.init(baseURL: baseURL, httpClient: httpClient)
    }

    func createRun(record: RunningRecord, accessToken: String) async throws -> CreatedRun {
        let response: CreatedRunEnvelope = try await request(
            path: "/runs",
            method: "POST",
            accessToken: accessToken,
            idempotencyKey: "run-\(record.id.uuidString)",
            body: CreateRunPayload(record)
        )
        return response.data.domain
    }

    func fetchWeeklyRuns(anchorDate: Date = Date(), accessToken: String) async throws -> RunPeriodSummary {
        let response: WeeklyRunsEnvelope = try await request(
            path: "/runs/me/week",
            queryItems: [URLQueryItem(name: "date", value: RunDateCoder.dateString(from: anchorDate))],
            method: "GET",
            accessToken: accessToken
        )
        return response.data.domain
    }

    func fetchMonthlyRuns(year: Int, month: Int, accessToken: String) async throws -> RunPeriodSummary {
        let response: MonthlyRunsEnvelope = try await request(
            path: "/runs/me/month",
            queryItems: [
                URLQueryItem(name: "year", value: "\(year)"),
                URLQueryItem(name: "month", value: "\(month)")
            ],
            method: "GET",
            accessToken: accessToken
        )
        return response.data.domain
    }

    func fetchRunDetail(runID: String, accessToken: String) async throws -> RunningRecord {
        let response: RunDetailEnvelope = try await request(
            path: "/runs/\(runID)",
            method: "GET",
            accessToken: accessToken
        )
        guard let record = response.data.domain else {
            throw RunAPIError.invalidResponse
        }

        return record
    }

    func fetchRunSplits(runID: String, accessToken: String) async throws -> [RunningSplit] {
        let response: RunSplitsEnvelope = try await request(
            path: "/runs/\(runID)/splits",
            method: "GET",
            accessToken: accessToken
        )
        return response.data.map(\.domain)
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
        idempotencyKey: String? = nil,
        body: Body
    ) async throws -> Response {
        var request = makeRequest(path: path, method: method, accessToken: accessToken)
        if let idempotencyKey {
            request.setValue(idempotencyKey, forHTTPHeaderField: "Idempotency-Key")
        }
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
        let (data, response) = try await httpClient.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw RunAPIError.invalidResponse
        }

        guard 200..<300 ~= httpResponse.statusCode else {
            throw RunAPIError.requestFailed(message: decodeErrorMessage(from: data), statusCode: httpResponse.statusCode)
        }

        return try decoder.decode(Response.self, from: data)
    }

    private func decodeErrorMessage(from data: Data) -> String? {
        try? decoder.decode(RunAPIErrorEnvelope.self, from: data).error.message
    }
}

final class MockRunService: RunServiceProtocol {
    private let historyStore: RunningHistoryStore

    init(historyStore: RunningHistoryStore = RunningHistoryStore()) {
        self.historyStore = historyStore
    }

    func createRun(record: RunningRecord, accessToken: String) async throws -> CreatedRun {
        CreatedRun(
            id: record.id.uuidString,
            distanceMeters: Int(record.distanceMeters.rounded()),
            durationSeconds: Int(record.elapsedTime.rounded()),
            averagePaceSecondsPerKilometer: record.averagePaceSecondsPerKilometer.map { Int($0.rounded()) },
            calories: record.estimatedCalories
        )
    }

    func fetchWeeklyRuns(anchorDate: Date = Date(), accessToken: String) async throws -> RunPeriodSummary {
        makePeriodSummary(records: historyStore.load().filter {
            Calendar.current.isDate($0.startedAt, equalTo: anchorDate, toGranularity: .weekOfYear)
        })
    }

    func fetchMonthlyRuns(year: Int, month: Int, accessToken: String) async throws -> RunPeriodSummary {
        makePeriodSummary(records: historyStore.load().filter { record in
            let components = Calendar.current.dateComponents([.year, .month], from: record.startedAt)
            return components.year == year && components.month == month
        })
    }

    func fetchRunDetail(runID: String, accessToken: String) async throws -> RunningRecord {
        guard let record = historyStore.load().first(where: { $0.id.uuidString == runID }) else {
            throw RunAPIError.invalidResponse
        }

        return record
    }

    func fetchRunSplits(runID: String, accessToken: String) async throws -> [RunningSplit] {
        try await fetchRunDetail(runID: runID, accessToken: accessToken).splits
    }

    private func makePeriodSummary(records: [RunningRecord]) -> RunPeriodSummary {
        RunPeriodSummary(
            totalDistanceMeters: Int(records.reduce(0) { $0 + $1.distanceMeters }.rounded()),
            days: records.map {
                RunDaySummary(
                    date: Calendar.current.startOfDay(for: $0.startedAt),
                    distanceMeters: Int($0.distanceMeters.rounded()),
                    hasRun: true
                )
            },
            runs: records.sorted { $0.startedAt > $1.startedAt }
        )
    }
}

enum RunAPIError: LocalizedError, Equatable {
    case invalidResponse
    case missingBaseURL
    case requestFailed(message: String?, statusCode: Int)

    var errorDescription: String? {
        switch self {
        case .invalidResponse:
            return "러닝 기록 응답을 확인해주세요."
        case .missingBaseURL:
            return "러닝 기록 API 주소를 확인해주세요."
        case let .requestFailed(message, statusCode):
            return message ?? "러닝 기록 요청에 실패했어요. (\(statusCode))"
        }
    }
}

private struct CreatedRunEnvelope: Decodable {
    let data: CreatedRunPayload
}

private struct WeeklyRunsEnvelope: Decodable {
    let data: WeeklyRunsPayload
}

private struct MonthlyRunsEnvelope: Decodable {
    let data: MonthlyRunsPayload
}

private struct RunDetailEnvelope: Decodable {
    let data: RunPayload
}

private struct RunSplitsEnvelope: Decodable {
    let data: [RunSplitPayload]
}

private struct CreatedRunPayload: Decodable {
    let id: String
    let distanceMeters: Int
    let durationSeconds: Int
    let averagePaceSecondsPerKm: Int?
    let calories: Int

    private enum CodingKeys: String, CodingKey {
        case id
        case distanceMeters
        case durationSeconds
        case averagePaceSecondsPerKm
        case calories
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decode(String.self, forKey: .id)
        distanceMeters = try container.decodeLossyInt(forKey: .distanceMeters)
        durationSeconds = try container.decodeLossyInt(forKey: .durationSeconds)
        averagePaceSecondsPerKm = try container.decodeLossyIntIfPresent(forKey: .averagePaceSecondsPerKm)
        calories = try container.decodeLossyInt(forKey: .calories)
    }

    var domain: CreatedRun {
        CreatedRun(
            id: id,
            distanceMeters: distanceMeters,
            durationSeconds: durationSeconds,
            averagePaceSecondsPerKilometer: averagePaceSecondsPerKm,
            calories: calories
        )
    }
}

private struct WeeklyRunsPayload: Decodable {
    let totalDistanceMeters: Int
    let days: [RunDayPayload]
    let runs: [RunPayload]

    private enum CodingKeys: String, CodingKey {
        case totalDistanceMeters
        case days
        case runs
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        totalDistanceMeters = try container.decodeLossyInt(forKey: .totalDistanceMeters)
        days = try container.decode([RunDayPayload].self, forKey: .days)
        runs = try container.decode([RunPayload].self, forKey: .runs)
    }

    var domain: RunPeriodSummary {
        RunPeriodSummary(
            totalDistanceMeters: totalDistanceMeters,
            days: days.compactMap(\.domain),
            runs: runs.compactMap(\.domain)
        )
    }
}

private struct MonthlyRunsPayload: Decodable {
    let totalDistanceMeters: Int
    let days: [RunDayPayload]
    let runs: [RunPayload]

    private enum CodingKeys: String, CodingKey {
        case totalDistanceMeters
        case days
        case runs
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        totalDistanceMeters = try container.decodeLossyInt(forKey: .totalDistanceMeters)
        days = try container.decode([RunDayPayload].self, forKey: .days)
        runs = try container.decode([RunPayload].self, forKey: .runs)
    }

    var domain: RunPeriodSummary {
        RunPeriodSummary(
            totalDistanceMeters: totalDistanceMeters,
            days: days.compactMap(\.domain),
            runs: runs.compactMap(\.domain)
        )
    }
}

private struct RunDayPayload: Decodable {
    let date: String
    let distanceMeters: Int
    let hasRun: Bool

    private enum CodingKeys: String, CodingKey {
        case date
        case distanceMeters
        case hasRun
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        date = try container.decode(String.self, forKey: .date)
        distanceMeters = try container.decodeLossyInt(forKey: .distanceMeters)
        hasRun = try container.decode(Bool.self, forKey: .hasRun)
    }

    var domain: RunDaySummary? {
        guard let date = RunDateCoder.date(from: date) else { return nil }
        return RunDaySummary(date: date, distanceMeters: distanceMeters, hasRun: hasRun)
    }
}

private struct RunPayload: Decodable {
    let id: String
    let date: String?
    let startedAt: String
    let endedAt: String
    let distanceMeters: Int
    let durationSeconds: Int
    let averagePaceSecondsPerKm: Int?
    let calories: Int
    let points: [RunPointPayload]?

    private enum CodingKeys: String, CodingKey {
        case id
        case date
        case startedAt
        case endedAt
        case distanceMeters
        case durationSeconds
        case averagePaceSecondsPerKm
        case calories
        case points
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decode(String.self, forKey: .id)
        date = try container.decodeIfPresent(String.self, forKey: .date)
        startedAt = try container.decode(String.self, forKey: .startedAt)
        endedAt = try container.decode(String.self, forKey: .endedAt)
        distanceMeters = try container.decodeLossyInt(forKey: .distanceMeters)
        durationSeconds = try container.decodeLossyInt(forKey: .durationSeconds)
        averagePaceSecondsPerKm = try container.decodeLossyIntIfPresent(forKey: .averagePaceSecondsPerKm)
        calories = try container.decodeLossyInt(forKey: .calories)
        points = try container.decodeIfPresent([RunPointPayload].self, forKey: .points)
    }

    var domain: RunningRecord? {
        guard
            let startedAt = RunDateCoder.dateTime(from: startedAt),
            let endedAt = RunDateCoder.dateTime(from: endedAt)
        else {
            return nil
        }

        return RunningRecord(
            id: UUID(uuidString: id) ?? UUID(),
            startedAt: startedAt,
            endedAt: endedAt,
            elapsedTime: TimeInterval(durationSeconds),
            distanceMeters: Double(distanceMeters),
            averagePaceSecondsPerKilometer: averagePaceSecondsPerKm.map(TimeInterval.init),
            calories: calories,
            route: (points ?? []).sorted { $0.sequence < $1.sequence }.compactMap(\.domain)
        )
    }
}

private struct CreateRunPayload: Encodable {
    let startedAt: String
    let endedAt: String
    let distanceMeters: Int
    let durationSeconds: Int
    let calories: Int
    let points: [CreateRunPointPayload]
    let splits: [CreateRunSplitPayload]

    init(_ record: RunningRecord) {
        startedAt = RunDateCoder.dateTimeString(from: record.startedAt)
        endedAt = RunDateCoder.dateTimeString(from: record.endedAt)
        distanceMeters = Int(record.distanceMeters.rounded())
        durationSeconds = Int(record.elapsedTime.rounded())
        calories = record.estimatedCalories
        points = record.createRunPoints.map(CreateRunPointPayload.init)
        splits = CreateRunSplitPayload.normalized(
            record.splits,
            totalDistanceMeters: distanceMeters,
            totalDurationMillis: durationSeconds * 1_000
        )
    }
}

private struct RunSplitPayload: Decodable {
    let sequence: Int
    let fromDistanceMeters: Double
    let toDistanceMeters: Double
    let distanceMeters: Double
    let durationMillis: Int
    let paceSecondsPerKm: Double

    var domain: RunningSplit {
        RunningSplit(
            sequence: sequence,
            fromDistanceMeters: fromDistanceMeters,
            toDistanceMeters: toDistanceMeters,
            distanceMeters: distanceMeters,
            durationMillis: durationMillis,
            paceSecondsPerKilometer: paceSecondsPerKm
        )
    }
}

private struct CreateRunSplitPayload: Encodable {
    let sequence: Int
    let distanceMeters: Int
    let durationMillis: Int

    private init(sequence: Int, distanceMeters: Int, durationMillis: Int) {
        self.sequence = sequence
        self.distanceMeters = distanceMeters
        self.durationMillis = durationMillis
    }

    static func normalized(
        _ splits: [RunningSplit],
        totalDistanceMeters: Int,
        totalDurationMillis: Int
    ) -> [CreateRunSplitPayload] {
        guard !splits.isEmpty, totalDistanceMeters > 0, totalDurationMillis > 0 else { return [] }

        let splitCount = Int(ceil(Double(totalDistanceMeters) / 1_000))
        guard splits.count >= splitCount else { return [] }

        var durations = Array(splits.prefix(splitCount).map(\.durationMillis))
        if splits.count > splitCount {
            durations[splitCount - 1] += splits.dropFirst(splitCount).reduce(0) { $0 + $1.durationMillis }
        }
        durations[splitCount - 1] += totalDurationMillis - durations.reduce(0, +)

        return (0..<splitCount).map { index in
            let distance = index == splitCount - 1
                ? totalDistanceMeters - (index * 1_000)
                : 1_000
            return CreateRunSplitPayload(
                sequence: index + 1,
                distanceMeters: distance,
                durationMillis: max(1, durations[index])
            )
        }
    }
}

private struct RunPointPayload: Decodable {
    let sequence: Int
    let latitude: Double
    let longitude: Double
    let horizontalAccuracyMeters: Double?
    let recordedAt: String

    var domain: RunningCoordinate? {
        guard let recordedAt = RunDateCoder.dateTime(from: recordedAt) else { return nil }
        return RunningCoordinate(
            .init(latitude: latitude, longitude: longitude),
            recordedAt: recordedAt,
            horizontalAccuracy: horizontalAccuracyMeters
        )
    }
}

private struct CreateRunPointPayload: Encodable {
    let sequence: Int
    let latitude: Double
    let longitude: Double
    let horizontalAccuracyMeters: Double?
    let recordedAt: String

    init(_ point: CreateRunPoint) {
        sequence = point.sequence
        latitude = point.latitude
        longitude = point.longitude
        horizontalAccuracyMeters = point.horizontalAccuracyMeters
        recordedAt = RunDateCoder.dateTimeString(from: point.recordedAt)
    }
}

private struct RunAPIErrorEnvelope: Decodable {
    let error: RunAPIErrorPayload
}

private struct RunAPIErrorPayload: Decodable {
    let message: String
}

private enum RunDateCoder {
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
        formatter.timeZone = TimeZone(secondsFromGMT: 0)
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()

    static func dateTimeString(from date: Date) -> String {
        fallbackDateTimeFormatter.string(from: date)
    }

    static func dateString(from date: Date) -> String {
        dateFormatter.string(from: date)
    }

    static func dateTime(from string: String) -> Date? {
        dateTimeFormatter.date(from: string) ?? fallbackDateTimeFormatter.date(from: string)
    }

    static func date(from string: String) -> Date? {
        dateFormatter.date(from: string)
    }
}

private extension Bundle {
    var runAPIBaseURL: URL? {
        guard
            let baseURLString = (
                object(forInfoDictionaryKey: "RunAPIBaseURL")
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
