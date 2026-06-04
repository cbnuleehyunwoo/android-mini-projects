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
        static let body2 = font(size: 14, weight: .medium)

        static func font(size: CGFloat, weight: Weight = .regular) -> Font {
            Font.custom(weight.fontName, size: size)
        }
    }

    enum Colors {
        static let primary = Color(red: 0.03, green: 0.34, blue: 0.97)
        static let textPrimary = Color(red: 0.07, green: 0.11, blue: 0.17)
        static let danger = Color(red: 1.00, green: 0.23, blue: 0.19)
        static let kakao = Color(red: 1.00, green: 0.88, blue: 0.00)
        static let kakaoText = Color(red: 0.16, green: 0.10, blue: 0.00)
    }

    enum Layout {
        static let horizontalPadding: CGFloat = 32
        static let buttonHeight: CGFloat = 56
        static let cornerRadius: CGFloat = 8
    }
}
