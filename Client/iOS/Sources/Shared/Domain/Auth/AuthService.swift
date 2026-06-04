import Foundation

protocol AuthServiceProtocol {
    func loginWithKakao() async throws -> AuthSession
}

final class MockAuthService: AuthServiceProtocol {
    func loginWithKakao() async throws -> AuthSession {
        try await Task.sleep(nanoseconds: 450_000_000)
        return AuthSession(
            accessToken: "mock-access-token",
            refreshToken: "mock-refresh-token",
            userID: UUID().uuidString,
            needsSignup: true
        )
    }
}
