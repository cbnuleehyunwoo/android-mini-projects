import Foundation

protocol ProfileServiceProtocol {
    func fetchHomeState(accessToken: String) async throws -> HomeState
    func createProfile(form: ProfileMutationForm, accessToken: String) async throws -> UserProfile
    func fetchMyProfile(accessToken: String) async throws -> UserProfile?
    func updateMyProfile(form: ProfileMutationForm, accessToken: String) async throws -> UserProfile
}

final class ProfileAPIService: ProfileServiceProtocol {
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
        guard let baseURL = bundle.profileAPIBaseURL else {
            throw ProfileAPIError.missingBaseURL
        }

        self.init(baseURL: baseURL)
    }

    func fetchHomeState(accessToken: String) async throws -> HomeState {
        let response: HomeStateEnvelope = try await request(
            path: "/home",
            method: "GET",
            accessToken: accessToken
        )
        return response.data.domain
    }

    func createProfile(form: ProfileMutationForm, accessToken: String) async throws -> UserProfile {
        let response: UserProfileEnvelope = try await request(
            path: "/profile",
            method: "POST",
            accessToken: accessToken,
            body: ProfileMutationPayload(form)
        )
        return response.data.domain
    }

    func fetchMyProfile(accessToken: String) async throws -> UserProfile? {
        let response: UserProfileEnvelope = try await request(
            path: "/profile/me",
            method: "GET",
            accessToken: accessToken
        )
        return response.data.domain
    }

    func updateMyProfile(form: ProfileMutationForm, accessToken: String) async throws -> UserProfile {
        let response: UserProfileEnvelope = try await request(
            path: "/profile/me",
            method: "PATCH",
            accessToken: accessToken,
            body: ProfileMutationPayload(form)
        )
        return response.data.domain
    }

    private func request<Response: Decodable>(
        path: String,
        method: String,
        accessToken: String
    ) async throws -> Response {
        try await send(request: makeRequest(path: path, method: method, accessToken: accessToken))
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

    private func makeRequest(path: String, method: String, accessToken: String) -> URLRequest {
        var request = URLRequest(url: baseURL.appendingAPIPath(path))
        request.httpMethod = method
        request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        return request
    }

    private func send<Response: Decodable>(request: URLRequest) async throws -> Response {
        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw ProfileAPIError.invalidResponse
        }

        guard 200..<300 ~= httpResponse.statusCode else {
            throw ProfileAPIError.requestFailed(message: decodeErrorMessage(from: data), statusCode: httpResponse.statusCode)
        }

        return try decoder.decode(Response.self, from: data)
    }

    private func decodeErrorMessage(from data: Data) -> String? {
        try? decoder.decode(ProfileAPIErrorEnvelope.self, from: data).error.message
    }
}

final class MockProfileService: ProfileServiceProtocol {
    private var profile: UserProfile?
    private var team: TeamSummary?

    init(profile: UserProfile? = nil, team: TeamSummary? = nil) {
        self.profile = profile
        self.team = team
    }

    func fetchHomeState(accessToken: String) async throws -> HomeState {
        HomeState(profile: profile, team: team)
    }

    func createProfile(form: ProfileMutationForm, accessToken: String) async throws -> UserProfile {
        let createdProfile = UserProfile(
            id: UUID().uuidString,
            nickname: form.nickname,
            avatarKey: form.avatarKey,
            teamId: nil,
            totalDistanceMeters: 0,
            totalDurationSeconds: 0,
            totalRunCount: 0
        )
        profile = createdProfile
        return createdProfile
    }

    func fetchMyProfile(accessToken: String) async throws -> UserProfile? {
        profile
    }

    func updateMyProfile(form: ProfileMutationForm, accessToken: String) async throws -> UserProfile {
        let updatedProfile = UserProfile(
            id: profile?.id ?? UUID().uuidString,
            nickname: form.nickname,
            avatarKey: form.avatarKey,
            teamId: profile?.teamId,
            totalDistanceMeters: profile?.totalDistanceMeters ?? 0,
            totalDurationSeconds: profile?.totalDurationSeconds ?? 0,
            totalRunCount: profile?.totalRunCount ?? 0
        )
        profile = updatedProfile
        return updatedProfile
    }
}

enum ProfileAPIError: LocalizedError, Equatable {
    case invalidResponse
    case missingBaseURL
    case requestFailed(message: String?, statusCode: Int)

    var errorDescription: String? {
        switch self {
        case .invalidResponse:
            return "프로필 응답을 확인해주세요."
        case .missingBaseURL:
            return "프로필 API 주소를 확인해주세요."
        case let .requestFailed(message, statusCode):
            if statusCode == 409, message == "Nickname already exists" {
                return "중복된 닉네임입니다."
            }
            return message ?? "프로필 요청에 실패했어요. (\(statusCode))"
        }
    }
}

private struct HomeStateEnvelope: Decodable {
    let data: HomeStatePayload
}

private struct UserProfileEnvelope: Decodable {
    let data: UserProfilePayload
}

private struct HomeStatePayload: Decodable {
    let profile: UserProfilePayload?
    let team: TeamSummaryPayload?

    var domain: HomeState {
        HomeState(profile: profile?.domain, team: team?.domain)
    }
}

private struct UserProfilePayload: Decodable {
    let id: String
    let nickname: String
    let avatarKey: String?
    let teamId: String?
    let totalDistanceMeters: Int?
    let totalDurationSeconds: Int?
    let totalRunCount: Int?

    var domain: UserProfile {
        UserProfile(
            id: id,
            nickname: nickname,
            avatarKey: avatarKey,
            teamId: teamId,
            totalDistanceMeters: totalDistanceMeters ?? 0,
            totalDurationSeconds: totalDurationSeconds ?? 0,
            totalRunCount: totalRunCount ?? 0
        )
    }
}

private struct TeamSummaryPayload: Decodable {
    let id: String
    let name: String
    let joinCode: String?
    let ownerId: String?
    let memberCount: Int
    let isOwner: Bool

    var domain: TeamSummary {
        TeamSummary(
            id: id,
            name: name,
            joinCode: joinCode,
            ownerId: ownerId,
            memberCount: memberCount,
            isOwner: isOwner
        )
    }
}

private struct ProfileMutationPayload: Encodable {
    let nickname: String
    let avatarKey: String?

    init(_ form: ProfileMutationForm) {
        nickname = form.nickname
        avatarKey = form.avatarKey
    }
}

private struct ProfileAPIErrorEnvelope: Decodable {
    let error: ProfileAPIErrorPayload
}

private struct ProfileAPIErrorPayload: Decodable {
    let message: String
}

private extension Bundle {
    var profileAPIBaseURL: URL? {
        guard
            let baseURLString = object(forInfoDictionaryKey: "APIBaseURL") as? String,
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
        appendingPathComponent(path.trimmingCharacters(in: CharacterSet(charactersIn: "/")))
    }
}
