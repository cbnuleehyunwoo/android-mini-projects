import SwiftUI

enum AppTheme {
    enum Typography {
        enum Weight {
            case regular
            case medium
            case semibold
            case bold
            case extraBold
            case black

            fileprivate var fontName: String {
                switch self {
                case .regular:
                    return "Pretendard-Regular"
                case .medium:
                    return "Pretendard-Medium"
                case .semibold:
                    return "Pretendard-SemiBold"
                case .bold:
                    return "Pretendard-Bold"
                case .extraBold:
                    return "Pretendard-ExtraBold"
                case .black:
                    return "Pretendard-Black"
                }
            }
        }

        static let splashTitle = font(size: 48, weight: .extraBold)

        static func font(size: CGFloat, weight: Weight = .regular) -> Font {
            Font.custom(weight.fontName, size: size)
        }
    }

    enum Colors {
        static let textPrimary = Color(red: 0.07, green: 0.11, blue: 0.17)
    }
}
