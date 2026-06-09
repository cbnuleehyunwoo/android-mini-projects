import Foundation

enum TeamNameValidator {
    static func isValid(_ name: String) -> Bool {
        hasValidLength(name)
            && containsOnlyAllowedCharacters(name)
            && doesNotContainSpecialCharacters(name)
    }

    static func hasValidLength(_ name: String) -> Bool {
        (2...10).contains(name.count)
    }

    static func containsOnlyAllowedCharacters(_ name: String) -> Bool {
        name.range(of: "^[가-힣ㄱ-ㅎㅏ-ㅣA-Za-z0-9]+$", options: .regularExpression) != nil
    }

    static func doesNotContainSpecialCharacters(_ name: String) -> Bool {
        name.range(of: "[^가-힣ㄱ-ㅎㅏ-ㅣA-Za-z0-9]", options: .regularExpression) == nil
    }
}
