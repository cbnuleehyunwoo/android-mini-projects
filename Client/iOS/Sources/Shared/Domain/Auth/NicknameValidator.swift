import Foundation

enum NicknameValidator {
    static func isValid(_ nickname: String) -> Bool {
        let trimmed = nickname.trimmingCharacters(in: .whitespacesAndNewlines)
        return hasValidLength(trimmed)
            && containsOnlyAllowedCharacters(trimmed)
            && doesNotContainSpecialCharacters(trimmed)
    }

    static func hasValidLength(_ nickname: String) -> Bool {
        (2...6).contains(nickname.count)
    }

    static func containsOnlyAllowedCharacters(_ nickname: String) -> Bool {
        nickname.range(
            of: "^[가-힣ㄱ-ㅎㅏ-ㅣA-Za-z0-9]+$",
            options: .regularExpression
        ) != nil
    }

    static func doesNotContainSpecialCharacters(_ nickname: String) -> Bool {
        nickname.range(
            of: "[^가-힣ㄱ-ㅎㅏ-ㅣA-Za-z0-9]",
            options: .regularExpression
        ) == nil
    }
}
