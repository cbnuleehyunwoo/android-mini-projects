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

        static let header1 = font(size: 30, weight: .black)
        static let header2 = font(size: 26, weight: .black)
        static let title2 = font(size: 20, weight: .semibold)
        static let body1 = font(size: 16, weight: .medium)
        static let splashTitle = font(size: 48, weight: .extraBold)
        static let body2 = font(size: 14, weight: .medium)
        static let caption1 = font(size: 12, weight: .medium)
        static let button = font(size: 17, weight: .bold)

        static func font(size: CGFloat, weight: Weight = .regular) -> Font {
            Font.custom(weight.fontName, size: size)
        }
    }

    enum Colors {
        static let primary = Color(red: 0.03, green: 0.34, blue: 0.97)
        static let textPrimary = Color(red: 0.07, green: 0.11, blue: 0.17)
        static let textSecondary = Color(red: 0.42, green: 0.47, blue: 0.55)
        static let border = Color(red: 0.86, green: 0.89, blue: 0.93)
        static let surface = Color(red: 0.97, green: 0.98, blue: 0.99)
        static let success = Color(red: 0.00, green: 0.76, blue: 0.48)
        static let danger = Color(red: 1.00, green: 0.23, blue: 0.19)
        static let kakao = Color(red: 1.00, green: 0.88, blue: 0.00)
        static let kakaoText = Color(red: 0.16, green: 0.10, blue: 0.00)
    }

    enum Layout {
        static let horizontalPadding: CGFloat = 32
        static let buttonHeight: CGFloat = 56
        static let fieldHeight: CGFloat = 68
        static let cornerRadius: CGFloat = 8
    }
}
