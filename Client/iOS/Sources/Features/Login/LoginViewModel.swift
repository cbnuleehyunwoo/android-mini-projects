import AuthenticationServices
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

    func loginWithGoogle() async {
        guard !isLoading else { return }

        isLoading = true
        errorMessage = nil

        do {
            session = try await authService.loginWithGoogle()
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func configureAppleLoginRequest(_ request: ASAuthorizationAppleIDRequest) {
        request.requestedScopes = [.fullName, .email]
    }

    func loginWithApple(result: Result<ASAuthorization, Error>) async {
        guard !isLoading else { return }

        isLoading = true
        errorMessage = nil

        do {
            switch result {
            case .success(let authorization):
                guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential else {
                    throw AuthError.unavailable
                }
                guard let idToken = credential.identityToken.flatMap({ String(data: $0, encoding: .utf8) }) else {
                    throw AuthError.missingAppleIdentityToken
                }

                session = try await authService.loginWithApple(identityToken: idToken)
            case .failure(let error):
                let authorizationError = error as? ASAuthorizationError
                if authorizationError?.code == .canceled {
                    throw AuthError.cancelled
                }
                throw error
            }
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }
}
