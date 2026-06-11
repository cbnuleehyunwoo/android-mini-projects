import CryptoKit
import Foundation
import GoogleSignIn
import Supabase
import UIKit

protocol AuthServiceProtocol {
    func restoreSession() async throws -> AuthSession?
    func loginWithGoogle() async throws -> AuthSession
    func loginWithApple(identityToken: String) async throws -> AuthSession
    func completeSignup(profile: SignupProfile) async throws -> AuthSession
    func logout(accessToken: String) async throws
    func deleteAccount(accessToken: String) async throws
}

@MainActor
final class SupabaseAuthService: AuthServiceProtocol {
    private let store: LocalAppStateStore
    private let supabase: SupabaseClient?
    private let apiBaseURL: URL?
    private let session: URLSession
    private let decoder: JSONDecoder

    init(
        store: LocalAppStateStore = LocalAppStateStore(),
        bundle: Bundle = .main,
        session: URLSession = .shared,
        decoder: JSONDecoder = JSONDecoder()
    ) {
        self.store = store
        self.session = session
        self.decoder = decoder
        apiBaseURL = bundle.authAPIBaseURL?.apiBaseURL
        supabase = bundle.supabaseConfiguration.map {
            SupabaseClient(
                supabaseURL: $0.url,
                supabaseKey: $0.anonKey,
                options: .init(
                    auth: .init(
                        redirectToURL: SupabaseConfiguration.redirectURL,
                        emitLocalSessionAsInitialSession: true
                    )
                )
            )
        }
    }

    func restoreSession() async throws -> AuthSession? {
        guard let supabase else { return nil }

        let session = try await supabase.auth.session
        return AuthSession(
            accessToken: session.accessToken,
            refreshToken: session.refreshToken,
            userID: session.user.id.uuidString,
            needsSignup: !store.hasCompletedSignup
        )
    }

    func loginWithGoogle() async throws -> AuthSession {
        guard let supabase else {
            throw AuthError.missingSupabaseConfiguration
        }
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

            let session = try await supabase.auth.signInWithIdToken(
                credentials: .init(
                    provider: .google,
                    idToken: idToken,
                    accessToken: signInResult.user.accessToken.tokenString,
                    nonce: nonce
                )
            )

            return AuthSession(
                accessToken: session.accessToken,
                refreshToken: session.refreshToken,
                userID: session.user.id.uuidString,
                needsSignup: !store.hasCompletedSignup
            )
        } catch {
            let nsError = error as NSError
            if nsError.domain == "com.google.GIDSignIn", nsError.code == -5 {
                throw AuthError.cancelled
            }
            throw error
        }
    }

    func loginWithApple(identityToken: String) async throws -> AuthSession {
        guard let supabase else {
            throw AuthError.missingSupabaseConfiguration
        }

        let session = try await supabase.auth.signInWithIdToken(
            credentials: .init(
                provider: .apple,
                idToken: identityToken
            )
        )

        return AuthSession(
            accessToken: session.accessToken,
            refreshToken: session.refreshToken,
            userID: session.user.id.uuidString,
            needsSignup: !store.hasCompletedSignup
        )
    }

    func completeSignup(profile: SignupProfile) async throws -> AuthSession {
        try await Task.sleep(nanoseconds: 350_000_000)

        guard NicknameValidator.isValid(profile.nickname) else {
            throw AuthError.invalidNickname
        }

        store.saveSignup(profile: profile)

        return AuthSession(
            accessToken: "google-signup-complete",
            refreshToken: nil,
            userID: UUID().uuidString,
            needsSignup: false
        )
    }

    func logout(accessToken: String) async throws {
        guard let apiBaseURL else {
            throw AuthError.missingLogoutConfiguration
        }

        let logoutAccessToken = await currentAccessToken() ?? accessToken
        var request = URLRequest(url: apiBaseURL.appendingAPIPath("/auth/logout"))
        request.httpMethod = "POST"
        request.setValue("Bearer \(logoutAccessToken)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw AuthError.logoutFailed
        }

        if [401, 403].contains(httpResponse.statusCode) {
            try? await supabase?.auth.signOut(scope: .local)
            return
        }

        guard 200..<300 ~= httpResponse.statusCode else {
            throw AuthError.logoutFailed
        }

        if !data.isEmpty {
            let envelope = try decoder.decode(AuthLogoutEnvelope.self, from: data)
            guard envelope.data.success else {
                throw AuthError.logoutFailed
            }
        }

        try? await supabase?.auth.signOut(scope: .local)
    }

    func deleteAccount(accessToken: String) async throws {
        guard let apiBaseURL else {
            throw AuthError.missingAccountDeletionConfiguration
        }

        let deletionAccessToken = await currentAccessToken() ?? accessToken
        var request = URLRequest(url: apiBaseURL.appendingAPIPath("/account/me"))
        request.httpMethod = "DELETE"
        request.setValue("Bearer \(deletionAccessToken)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw AuthError.accountDeletionFailed
        }

        guard 200..<300 ~= httpResponse.statusCode else {
            throw AuthError.accountDeletionFailed
        }

        let envelope = try decoder.decode(AuthDeleteAccountEnvelope.self, from: data)
        guard envelope.data.deleted else {
            throw AuthError.accountDeletionFailed
        }

        try? await supabase?.auth.signOut(scope: .local)
    }

    private func currentAccessToken() async -> String? {
        guard let supabase else { return nil }
        return try? await supabase.auth.session.accessToken
    }
}

final class MockAuthService: AuthServiceProtocol {
    private let store: LocalAppStateStore

    init(store: LocalAppStateStore = LocalAppStateStore()) {
        self.store = store
    }

    func restoreSession() async throws -> AuthSession? {
        guard store.hasCompletedSignup else { return nil }

        return AuthSession(
            accessToken: "mock-access-token",
            refreshToken: "mock-refresh-token",
            userID: UUID().uuidString,
            needsSignup: false
        )
    }

    func loginWithGoogle() async throws -> AuthSession {
        try await Task.sleep(nanoseconds: 450_000_000)
        return AuthSession(
            accessToken: "mock-access-token",
            refreshToken: "mock-refresh-token",
            userID: UUID().uuidString,
            needsSignup: !store.hasCompletedSignup
        )
    }

    func loginWithApple(identityToken: String) async throws -> AuthSession {
        try await Task.sleep(nanoseconds: 450_000_000)
        return AuthSession(
            accessToken: "mock-access-token",
            refreshToken: "mock-refresh-token",
            userID: UUID().uuidString,
            needsSignup: !store.hasCompletedSignup
        )
    }

    func completeSignup(profile: SignupProfile) async throws -> AuthSession {
        try await Task.sleep(nanoseconds: 350_000_000)

        guard NicknameValidator.isValid(profile.nickname) else {
            throw AuthError.invalidNickname
        }

        store.saveSignup(profile: profile)

        return AuthSession(
            accessToken: "mock-access-token",
            refreshToken: "mock-refresh-token",
            userID: UUID().uuidString,
            needsSignup: false
        )
    }

    func logout(accessToken: String) async throws {
        try await Task.sleep(nanoseconds: 250_000_000)
    }

    func deleteAccount(accessToken: String) async throws {
        try await Task.sleep(nanoseconds: 350_000_000)
    }
}

enum AuthNonce {
    static func make(length: Int = 32) -> String {
        let characters = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        var generator = SystemRandomNumberGenerator()

        return String((0..<length).map { _ in
            characters.randomElement(using: &generator) ?? "0"
        })
    }

    static func sha256Hex(_ value: String) -> String {
        SHA256.hash(data: Data(value.utf8))
            .map { String(format: "%02x", $0) }
            .joined()
    }
}

private struct SupabaseConfiguration {
    static let redirectURL = URL(string: "runpamine://auth-callback/")!

    let url: URL
    let anonKey: String
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

private extension Bundle {
    var authAPIBaseURL: URL? {
        guard
            let baseURLString = object(forInfoDictionaryKey: "ProfileAPIBaseURL") as? String,
            !baseURLString.isEmpty,
            !baseURLString.hasPrefix("$(")
        else {
            return nil
        }

        return URL(string: baseURLString)
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

    var supabaseConfiguration: SupabaseConfiguration? {
        guard
            let baseURLString = object(forInfoDictionaryKey: "SupabaseURL") as? String,
            let baseURL = URL(string: baseURLString),
            let anonKey = object(forInfoDictionaryKey: "SupabaseAnonKey") as? String,
            !anonKey.isEmpty
        else {
            return nil
        }

        return SupabaseConfiguration(url: baseURL, anonKey: anonKey)
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
