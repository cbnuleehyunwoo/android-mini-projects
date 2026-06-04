import Foundation

struct AuthSession: Equatable {
    let accessToken: String
    let refreshToken: String?
    let userID: String
    let needsSignup: Bool
}

struct SignupProfile: Equatable {
    let nickname: String
    let agreedTerms: [TermsAgreement]
}

struct TermsAgreement: Identifiable, Equatable, Codable {
    let id: TermsAgreementID
    let title: String
    let isRequired: Bool
    var isAccepted: Bool
}

enum TermsAgreementID: String, CaseIterable, Codable {
    case service
    case privacy
}

enum AuthError: LocalizedError, Equatable {
    case cancelled
    case invalidNickname
    case unavailable

    var errorDescription: String? {
        switch self {
        case .cancelled:
            return "로그인이 취소되었어요."
        case .invalidNickname:
            return "닉네임을 다시 확인해주세요."
        case .unavailable:
            return "잠시 후 다시 시도해주세요."
        }
    }
}
