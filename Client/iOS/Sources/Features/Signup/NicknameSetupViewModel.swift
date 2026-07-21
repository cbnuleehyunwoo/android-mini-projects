import Combine
import Foundation

@MainActor
final class NicknameSetupViewModel: ObservableObject {
    @Published var nickname = ""
    @Published private(set) var isLoading = false
    @Published private(set) var didComplete = false
    @Published var errorMessage: String?

    private let authService: AuthServiceProtocol
    private let profileService: ProfileServiceProtocol?
    private let accessToken: String?
    private let agreements: [TermsAgreement]

    init(
        authService: AuthServiceProtocol,
        profileService: ProfileServiceProtocol? = nil,
        accessToken: String? = nil,
        agreements: [TermsAgreement]
    ) {
        self.authService = authService
        self.profileService = profileService
        self.accessToken = accessToken
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
            if let profileService, let accessToken {
                _ = try await profileService.createProfile(
                    form: ProfileMutationForm(nickname: trimmedNickname),
                    accessToken: accessToken
                )
            }

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
