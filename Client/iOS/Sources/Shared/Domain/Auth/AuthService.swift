import CryptoKit
import Foundation
import GoogleSignIn
import Security
import UIKit

protocol AuthServiceProtocol {
    func restoreSession() async throws -> AuthSession?
    func loginWithGoogle() async throws -> AuthSession
    func loginWithApple(identityToken: String, nonce: String) async throws -> AuthSession
    func completeSignup(profile: SignupProfile) async throws -> AuthSession
    func logout(accessToken: String) async throws
    func deleteAccount(accessToken: String) async throws
}

actor AuthSessionStore {
    static let shared = AuthSessionStore()

    private let service = "com.runpamine.app.auth"
    private let account = "was-session"
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()

    func load() throws -> AuthSession? {
        var query = baseQuery
        query[kSecReturnData as String] = true
        query[kSecMatchLimit as String] = kSecMatchLimitOne

        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)
        if status == errSecItemNotFound { return nil }
        guard status == errSecSuccess, let data = item as? Data else {
            throw AuthError.unavailable
        }
        do {
            return try decoder.decode(AuthSession.self, from: data)
        } catch {
            try? clear()
            return nil
        }
    }

    func save(_ session: AuthSession) throws {
        let data = try encoder.encode(session)
        let updateStatus = SecItemUpdate(
            baseQuery as CFDictionary,
            [kSecValueData as String: data] as CFDictionary
        )
        if updateStatus == errSecSuccess { return }
        guard updateStatus == errSecItemNotFound else {
            throw AuthError.unavailable
        }

        var query = baseQuery
        query[kSecValueData as String] = data
        query[kSecAttrAccessible as String] = kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
        guard SecItemAdd(query as CFDictionary, nil) == errSecSuccess else {
            throw AuthError.unavailable
        }
    }

    func clear() throws {
        let status = SecItemDelete(baseQuery as CFDictionary)
        guard status == errSecSuccess || status == errSecItemNotFound else {
            throw AuthError.unavailable
        }
    }

    private var baseQuery: [String: Any] {
        [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account
        ]
    }
}

actor AuthenticatedHTTPClient {
    private let session: URLSession
    private let apiBaseURL: URL?
    private let sessionStore: AuthSessionStore
    private var refreshTask: Task<AuthSession, Error>?

    init(
        session: URLSession = .shared,
        apiBaseURL: URL? = Bundle.main.authAPIBaseURL,
        sessionStore: AuthSessionStore = .shared
    ) {
        self.session = session
        self.apiBaseURL = apiBaseURL
        self.sessionStore = sessionStore
    }

    func data(for request: URLRequest) async throws -> (Data, URLResponse) {
        let storedSession = try? await sessionStore.load()
        let accessToken = storedSession?.accessToken ?? request.bearerAccessToken
        let firstResult = try await session.data(for: request.authorized(with: accessToken))

        guard (firstResult.1 as? HTTPURLResponse)?.statusCode == 401,
              storedSession != nil else {
            return firstResult
        }

        if let latest = try? await sessionStore.load(), latest.accessToken != accessToken {
            return try await session.data(for: request.authorized(with: latest.accessToken))
        }

        let refreshed = try await refreshSession()
        return try await session.data(for: request.authorized(with: refreshed.accessToken))
    }

    private func refreshSession() async throws -> AuthSession {
        if let refreshTask {
            return try await refreshTask.value
        }

        let task = Task<AuthSession, Error> {
            guard let apiBaseURL else { throw AuthError.missingAPIConfiguration }
            guard let current = try await sessionStore.load() else { throw AuthError.unavailable }

            var request = URLRequest(url: apiBaseURL.appendingAPIPath("/auth/refresh"))
            request.httpMethod = "POST"
            request.setValue("application/json", forHTTPHeaderField: "Accept")
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = try JSONEncoder().encode(RefreshTokenRequest(refreshToken: current.refreshToken ?? ""))

            let (data, response) = try await session.data(for: request)
            guard let httpResponse = response as? HTTPURLResponse else {
                throw AuthError.unavailable
            }
            guard 200..<300 ~= httpResponse.statusCode else {
                if httpResponse.statusCode == 401 {
                    try? await sessionStore.clear()
                }
                throw AuthAPIError.from(data: data, statusCode: httpResponse.statusCode)
            }

            let envelope = try JSONDecoder().decode(TokenRefreshEnvelope.self, from: data)
            let refreshed = AuthSession(
                accessToken: envelope.data.accessToken,
                refreshToken: envelope.data.refreshToken,
                userID: current.userID,
                needsSignup: current.needsSignup
            )
            try await sessionStore.save(refreshed)
            return refreshed
        }
        refreshTask = task
        defer { refreshTask = nil }
        return try await task.value
    }
}

private extension URLRequest {
    var bearerAccessToken: String? {
        guard let authorization = value(forHTTPHeaderField: "Authorization"),
              authorization.hasPrefix("Bearer ") else {
            return nil
        }
        return String(authorization.dropFirst("Bearer ".count))
    }

    func authorized(with accessToken: String?) -> URLRequest {
        guard let accessToken else { return self }
        var request = self
        request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        return request
    }
}

@MainActor
final class WASAuthService: AuthServiceProtocol {
    private let store: LocalAppStateStore
    private let apiBaseURL: URL?
    private let session: URLSession
    private let sessionStore: AuthSessionStore
    private let httpClient: AuthenticatedHTTPClient
    private let decoder: JSONDecoder
    private let encoder: JSONEncoder

    init(
        store: LocalAppStateStore = LocalAppStateStore(),
        bundle: Bundle = .main,
        session: URLSession = .shared,
        sessionStore: AuthSessionStore = .shared,
        decoder: JSONDecoder = JSONDecoder(),
        encoder: JSONEncoder = JSONEncoder(),
        httpClient: AuthenticatedHTTPClient? = nil
    ) {
        self.store = store
        apiBaseURL = bundle.authAPIBaseURL
        self.session = session
        self.sessionStore = sessionStore
        self.decoder = decoder
        self.encoder = encoder
        self.httpClient = httpClient ?? AuthenticatedHTTPClient(
            session: session,
            apiBaseURL: bundle.authAPIBaseURL,
            sessionStore: sessionStore
        )
    }

    func restoreSession() async throws -> AuthSession? {
        try await sessionStore.load()
    }

    func loginWithGoogle() async throws -> AuthSession {
        guard let googleConfiguration = Bundle.main.googleSignInConfiguration else {
            throw AuthError.missingGoogleConfiguration
        }
        guard let presentingViewController = UIApplication.shared.runpamineTopViewController else {
            throw AuthError.unavailable
        }

        do {
            GIDSignIn.sharedInstance.configuration = googleConfiguration
            let nonce = AuthNonce.make()
            let signInResult = try await GIDSignIn.sharedInstance.signIn(
                withPresenting: presentingViewController,
                hint: nil,
                additionalScopes: nil,
                nonce: AuthNonce.sha256Hex(nonce)
            )
            guard let idToken = signInResult.user.idToken?.tokenString else {
                throw AuthError.missingGoogleIDToken
            }

            let envelope: GoogleLoginEnvelope = try await send(
                path: "/auth/google",
                method: "POST",
                body: GoogleLoginRequest(idToken: idToken, nonce: nonce)
            )
            let authSession = AuthSession(
                accessToken: envelope.data.accessToken,
                refreshToken: envelope.data.refreshToken,
                userID: envelope.data.user.id,
                needsSignup: !store.hasCompletedSignup
            )
            try await sessionStore.save(authSession)
            return authSession
        } catch {
            let nsError = error as NSError
            if nsError.domain == "com.google.GIDSignIn", nsError.code == -5 {
                throw AuthError.cancelled
            }
            throw error
        }
    }

    func loginWithApple(identityToken: String, nonce: String) async throws -> AuthSession {
        let envelope: AppleLoginEnvelope = try await send(
            path: "/auth/apple/ios",
            method: "POST",
            body: AppleLoginRequest(identityToken: identityToken, nonce: nonce)
        )
        let authSession = AuthSession(
            accessToken: envelope.data.accessToken,
            refreshToken: envelope.data.refreshToken,
            userID: envelope.data.userID,
            needsSignup: !store.hasCompletedSignup
        )
        try await sessionStore.save(authSession)
        return authSession
    }

    func completeSignup(profile: SignupProfile) async throws -> AuthSession {
        guard NicknameValidator.isValid(profile.nickname) else {
            throw AuthError.invalidNickname
        }
        guard let current = try await sessionStore.load() else {
            throw AuthError.unavailable
        }

        store.saveSignup(profile: profile)
        let updated = AuthSession(
            accessToken: current.accessToken,
            refreshToken: current.refreshToken,
            userID: current.userID,
            needsSignup: false
        )
        try await sessionStore.save(updated)
        return updated
    }

    func logout(accessToken: String) async throws {
        guard let apiBaseURL else { throw AuthError.missingLogoutConfiguration }
        var request = URLRequest(url: apiBaseURL.appendingAPIPath("/auth/logout"))
        request.httpMethod = "POST"
        request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let data: Data
        let response: URLResponse
        do {
            (data, response) = try await httpClient.data(for: request)
        } catch let error as AuthAPIError where [401, 403].contains(error.statusCode) {
            try? await sessionStore.clear()
            return
        }
        guard let httpResponse = response as? HTTPURLResponse else { throw AuthError.logoutFailed }
        if [401, 403].contains(httpResponse.statusCode) {
            try? await sessionStore.clear()
            return
        }
        guard 200..<300 ~= httpResponse.statusCode else { throw AuthError.logoutFailed }
        if !data.isEmpty {
            let envelope = try decoder.decode(AuthLogoutEnvelope.self, from: data)
            guard envelope.data.success else { throw AuthError.logoutFailed }
        }
        try await sessionStore.clear()
    }

    func deleteAccount(accessToken: String) async throws {
        guard let apiBaseURL else { throw AuthError.missingAccountDeletionConfiguration }
        var request = URLRequest(url: apiBaseURL.appendingAPIPath("/account/me"))
        request.httpMethod = "DELETE"
        request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let (data, response) = try await httpClient.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse,
              200..<300 ~= httpResponse.statusCode else {
            throw AuthError.accountDeletionFailed
        }
        let envelope = try decoder.decode(AuthDeleteAccountEnvelope.self, from: data)
        guard envelope.data.deleted else { throw AuthError.accountDeletionFailed }
        try await sessionStore.clear()
    }

    private func send<Response: Decodable, Body: Encodable>(
        path: String,
        method: String,
        body: Body
    ) async throws -> Response {
        guard let apiBaseURL else { throw AuthError.missingAPIConfiguration }
        var request = URLRequest(url: apiBaseURL.appendingAPIPath(path))
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try encoder.encode(body)

        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else { throw AuthError.unavailable }
        guard 200..<300 ~= httpResponse.statusCode else {
            throw AuthAPIError.from(data: data, statusCode: httpResponse.statusCode)
        }
        return try decoder.decode(Response.self, from: data)
    }
}

final class MockAuthService: AuthServiceProtocol {
    private let store: LocalAppStateStore

    init(store: LocalAppStateStore = LocalAppStateStore()) {
        self.store = store
    }

    func restoreSession() async throws -> AuthSession? {
        guard store.hasCompletedSignup else { return nil }
        return mockSession(needsSignup: false)
    }

    func loginWithGoogle() async throws -> AuthSession {
        try await Task.sleep(nanoseconds: 450_000_000)
        return mockSession(needsSignup: !store.hasCompletedSignup)
    }

    func loginWithApple(identityToken: String, nonce: String) async throws -> AuthSession {
        try await Task.sleep(nanoseconds: 450_000_000)
        return mockSession(needsSignup: !store.hasCompletedSignup)
    }

    func completeSignup(profile: SignupProfile) async throws -> AuthSession {
        guard NicknameValidator.isValid(profile.nickname) else { throw AuthError.invalidNickname }
        store.saveSignup(profile: profile)
        return mockSession(needsSignup: false)
    }

    func logout(accessToken: String) async throws {
        try await Task.sleep(nanoseconds: 250_000_000)
    }

    func deleteAccount(accessToken: String) async throws {
        try await Task.sleep(nanoseconds: 350_000_000)
    }

    private func mockSession(needsSignup: Bool) -> AuthSession {
        AuthSession(
            accessToken: "mock-access-token",
            refreshToken: "mock-refresh-token",
            userID: UUID().uuidString,
            needsSignup: needsSignup
        )
    }
}

enum AuthNonce {
    static func make(length: Int = 32) -> String {
        let characters = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        var generator = SystemRandomNumberGenerator()
        return String((0..<length).map { _ in characters.randomElement(using: &generator) ?? "0" })
    }

    static func sha256Hex(_ value: String) -> String {
        SHA256.hash(data: Data(value.utf8))
            .map { String(format: "%02x", $0) }
            .joined()
    }
}

private struct GoogleLoginRequest: Encodable {
    let idToken: String
    let nonce: String
}

private struct AppleLoginRequest: Encodable {
    let identityToken: String
    let nonce: String
}

private struct RefreshTokenRequest: Encodable {
    let refreshToken: String
}

private struct GoogleLoginEnvelope: Decodable {
    let data: GoogleLoginPayload
}

private struct GoogleLoginPayload: Decodable {
    let accessToken: String
    let refreshToken: String
    let user: LoginUserPayload
}

private struct LoginUserPayload: Decodable {
    let id: String
    let email: String?
}

private struct AppleLoginEnvelope: Decodable {
    let data: AppleLoginPayload
}

private struct AppleLoginPayload: Decodable {
    let accessToken: String
    let refreshToken: String
    let userID: String
}

private struct TokenRefreshEnvelope: Decodable {
    let data: TokenRefreshPayload
}

private struct TokenRefreshPayload: Decodable {
    let accessToken: String
    let refreshToken: String
}

private struct AuthLogoutEnvelope: Decodable {
    let data: AuthLogoutPayload
}

private struct AuthLogoutPayload: Decodable {
    let success: Bool
}

private struct AuthDeleteAccountEnvelope: Decodable {
    let data: AuthDeleteAccountPayload
}

private struct AuthDeleteAccountPayload: Decodable {
    let deleted: Bool
}

private struct AuthErrorEnvelope: Decodable {
    let error: AuthErrorPayload
}

private struct AuthErrorPayload: Decodable {
    let message: String
}

private struct AuthAPIError: LocalizedError {
    let statusCode: Int
    let message: String?

    var errorDescription: String? {
        message ?? "요청에 실패했어요. (\(statusCode))"
    }

    static func from(data: Data, statusCode: Int) -> AuthAPIError {
        let message = try? JSONDecoder().decode(AuthErrorEnvelope.self, from: data).error.message
        return AuthAPIError(statusCode: statusCode, message: message)
    }
}

extension Bundle {
    var authAPIBaseURL: URL? {
        guard
            let baseURLString = object(forInfoDictionaryKey: "APIBaseURL") as? String,
            !baseURLString.isEmpty,
            !baseURLString.hasPrefix("$(")
        else {
            return nil
        }
        return URL(string: baseURLString)?.runpamineAPIBaseURL
    }

    var googleSignInConfiguration: GIDConfiguration? {
        guard
            let clientID = object(forInfoDictionaryKey: "GIDClientID") as? String,
            let serverClientID = object(forInfoDictionaryKey: "GIDServerClientID") as? String,
            !serverClientID.isEmpty,
            !clientID.isEmpty
        else {
            return nil
        }
        return GIDConfiguration(clientID: clientID, serverClientID: serverClientID)
    }
}

extension URL {
    var runpamineAPIBaseURL: URL {
        if Array(pathComponents.suffix(2)) == ["api", "v1"] {
            return self
        }
        return appendingPathComponent("api").appendingPathComponent("v1")
    }
}

private extension URL {
    func appendingAPIPath(_ path: String) -> URL {
        var url = self
        path.split(separator: "/").forEach { url.appendPathComponent(String($0)) }
        return url
    }
}

private extension UIApplication {
    var runpamineTopViewController: UIViewController? {
        connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first(where: \.isKeyWindow)?
            .rootViewController?
            .topPresentedViewController
    }
}

private extension UIViewController {
    var topPresentedViewController: UIViewController {
        if let presentedViewController {
            return presentedViewController.topPresentedViewController
        }
        if let navigationController = self as? UINavigationController {
            return navigationController.visibleViewController?.topPresentedViewController ?? navigationController
        }
        if let tabBarController = self as? UITabBarController {
            return tabBarController.selectedViewController?.topPresentedViewController ?? tabBarController
        }
        return self
    }
}
