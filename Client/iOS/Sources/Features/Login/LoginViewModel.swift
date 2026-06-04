import Combine
import Foundation

@MainActor
final class LoginViewModel: ObservableObject {
    @Published private(set) var isLoading = false
    @Published private(set) var session: AuthSession?
    @Published var errorMessage: String?

    private let authService: AuthServiceProtocol

    init(authService: AuthServiceProtocol) {
        self.authService = authService
    }

    func loginWithKakao() async {
        guard !isLoading else { return }

        isLoading = true
        errorMessage = nil

        do {
            session = try await authService.loginWithKakao()
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }
}
