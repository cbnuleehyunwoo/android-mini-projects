import Foundation

struct AuthSession: Codable, Equatable {
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

    var documentURL: URL {
        switch self {
        case .service:
            return URL(string: "https://sheer-mimosa-20f.notion.site/37958b8d8e6c8050b988fcc4e6279e25?pvs=74")!
        case .privacy:
            return URL(string: "https://sheer-mimosa-20f.notion.site/37958b8d8e6c80cdb6b8c29d6d6935f5?pvs=74")!
        }
    }
}

enum AuthError: LocalizedError, Equatable {
    case accountDeletionFailed
    case cancelled
    case invalidNickname
    case missingAppleIdentityToken
    case missingAccountDeletionConfiguration
    case missingGoogleConfiguration
    case missingGoogleIDToken
    case missingLogoutConfiguration
    case missingAPIConfiguration
    case logoutFailed
    case unavailable

    var errorDescription: String? {
        switch self {
        case .accountDeletionFailed:
            return "회원탈퇴에 실패했어요. 잠시 후 다시 시도해주세요."
        case .cancelled:
            return "로그인이 취소되었어요."
        case .invalidNickname:
            return "닉네임을 다시 확인해주세요."
        case .missingAppleIdentityToken:
            return "Apple 로그인 정보를 가져오지 못했어요."
        case .missingAccountDeletionConfiguration:
            return "회원탈퇴 설정을 확인해주세요."
        case .missingGoogleConfiguration:
            return "Google 로그인 설정을 확인해주세요."
        case .missingGoogleIDToken:
            return "Google 로그인 정보를 가져오지 못했어요."
        case .missingLogoutConfiguration:
            return "로그아웃 설정을 확인해주세요."
        case .missingAPIConfiguration:
            return "서버 API 주소를 확인해주세요."
        case .logoutFailed:
            return "로그아웃에 실패했어요. 잠시 후 다시 시도해주세요."
        case .unavailable:
            return "잠시 후 다시 시도해주세요."
        }
    }
}
