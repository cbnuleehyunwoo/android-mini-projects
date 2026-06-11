import CryptoKit
import Foundation
import GoogleSignIn
import Supabase
import UIKit

protocol AuthServiceProtocol {
    func restoreSession() async throws -> AuthSession?
    func loginWithGoogle() async throws -> AuthSession
    func loginWithApple(identityToken: String, nonce: String?) async throws -> AuthSession
    func completeSignup(profile: SignupProfile) async throws -> AuthSession
}

@MainActor
final class SupabaseAuthService: AuthServiceProtocol {
    private let store: LocalAppStateStore
    private let supabase: SupabaseClient?

    init(store: LocalAppStateStore = LocalAppStateStore()) {
        self.store = store
        supabase = Bundle.main.supabaseConfiguration.map {
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

    func loginWithApple(identityToken: String, nonce: String?) async throws -> AuthSession {
        guard let supabase else {
            throw AuthError.missingSupabaseConfiguration
        }

        let session = try await supabase.auth.signInWithIdToken(
            credentials: .init(
                provider: .apple,
                idToken: identityToken,
                nonce: nonce
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

    func loginWithApple(identityToken: String, nonce: String?) async throws -> AuthSession {
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

private extension Bundle {
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
