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
    case missingAppleIdentityToken
    case missingGoogleConfiguration
    case missingGoogleIDToken
    case missingLogoutConfiguration
    case missingSupabaseConfiguration
    case logoutFailed
    case unavailable

    var errorDescription: String? {
        switch self {
        case .cancelled:
            return "로그인이 취소되었어요."
        case .invalidNickname:
            return "닉네임을 다시 확인해주세요."
        case .missingAppleIdentityToken:
            return "Apple 로그인 정보를 가져오지 못했어요."
        case .missingGoogleConfiguration:
            return "Google 로그인 설정을 확인해주세요."
        case .missingGoogleIDToken:
            return "Google 로그인 정보를 가져오지 못했어요."
        case .missingLogoutConfiguration:
            return "로그아웃 설정을 확인해주세요."
        case .missingSupabaseConfiguration:
            return "Supabase 로그인 설정을 확인해주세요."
        case .logoutFailed:
            return "로그아웃에 실패했어요. 잠시 후 다시 시도해주세요."
        case .unavailable:
            return "잠시 후 다시 시도해주세요."
        }
    }
}
