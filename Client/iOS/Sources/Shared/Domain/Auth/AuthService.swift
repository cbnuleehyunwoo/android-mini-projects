import Foundation

protocol AuthServiceProtocol {
    func loginWithKakao() async throws -> AuthSession
    func completeSignup(profile: SignupProfile) async throws -> AuthSession
}

final class MockAuthService: AuthServiceProtocol {
    private let store: LocalAppStateStore

    init(store: LocalAppStateStore = LocalAppStateStore()) {
        self.store = store
    }

    func loginWithKakao() async throws -> AuthSession {
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
