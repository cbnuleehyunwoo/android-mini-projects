import Foundation

struct AuthSession: Equatable {
    let accessToken: String
    let refreshToken: String?
    let userID: String
    let needsSignup: Bool
}

enum AuthError: LocalizedError, Equatable {
    case cancelled
    case unavailable

    var errorDescription: String? {
        switch self {
        case .cancelled:
            return "로그인이 취소되었어요."
        case .unavailable:
            return "잠시 후 다시 시도해주세요."
        }
    }
}
