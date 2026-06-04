import Combine
import Foundation

@MainActor
final class NicknameSetupViewModel: ObservableObject {
    @Published var nickname = ""
    @Published private(set) var isLoading = false
    @Published private(set) var didComplete = false
    @Published var errorMessage: String?

    private let authService: AuthServiceProtocol
    private let agreements: [TermsAgreement]

    init(authService: AuthServiceProtocol, agreements: [TermsAgreement]) {
        self.authService = authService
        self.agreements = agreements
    }

    var hasValidLength: Bool {
        NicknameValidator.hasValidLength(trimmedNickname)
    }

    var containsOnlyAllowedCharacters: Bool {
        !trimmedNickname.isEmpty && NicknameValidator.containsOnlyAllowedCharacters(trimmedNickname)
    }

    var doesNotContainSpecialCharacters: Bool {
        trimmedNickname.isEmpty || NicknameValidator.doesNotContainSpecialCharacters(trimmedNickname)
    }

    var canSubmit: Bool {
        NicknameValidator.isValid(trimmedNickname)
    }

    private var trimmedNickname: String {
        nickname.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    func submit() async {
        guard canSubmit, !isLoading else { return }

        isLoading = true
        errorMessage = nil
        didComplete = false

        do {
            _ = try await authService.completeSignup(
                profile: SignupProfile(
                    nickname: trimmedNickname,
                    agreedTerms: agreements
                )
            )
            didComplete = true
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }
}
