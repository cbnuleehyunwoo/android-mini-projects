import SwiftUI

private enum FeedbackOption: String, CaseIterable, Identifiable {
    case characterCustomizing
    case earnPoints
    case notifyIncompleteMembers
    case indoorRunning
    case proofPhoto
    case custom

    var id: String { rawValue }

    var title: String {
        switch self {
        case .characterCustomizing: return "캐릭터 커스터마이징"
        case .earnPoints: return "러닝으로 포인트(인앱 재화) 획득"
        case .notifyIncompleteMembers: return "미완료 그룹원에게 알림 보내기"
        case .indoorRunning: return "실내 러닝"
        case .proofPhoto: return "러닝 인증샷"
        case .custom: return "직접 입력"
        }
    }
}

struct FeedbackView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var selectedOptions: Set<FeedbackOption> = []
    @State private var customText: String = ""
    @State private var isSubmitted: Bool = false

    var body: some View {
        ZStack(alignment: .topLeading) {
            Color.white.ignoresSafeArea()

            if isSubmitted {
                completionContent
            } else {
                writingContent
            }

            Button(action: { dismiss() }) {
                Image(systemName: "xmark")
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundStyle(AppTheme.Colors.textPrimary)
                    .frame(width: 44, height: 44)
                    .contentShape(Rectangle())
            }
            .accessibilityLabel("닫기")
            .padding(.leading, 8)
            .padding(.top, 8)
        }
    }

    private var canSubmit: Bool {
        !selectedOptions.isEmpty
    }

    private var writingContent: some View {
        VStack(spacing: 0) {
            Text("피드백 하기")
                .font(AppTheme.Typography.font(size: 20, weight: .semibold))
                .foregroundStyle(AppTheme.Colors.textPrimary)
                .frame(height: 60)

            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Text("어떤 기능이 추가되었으면 좋겠나요?(복수선택 가능)")
                        .font(AppTheme.Typography.font(size: 16, weight: .bold))
                        .foregroundStyle(AppTheme.Colors.textPrimary)
                        .padding(.bottom, 4)

                    ForEach(FeedbackOption.allCases) { option in
                        FeedbackOptionRow(
                            title: option.title,
                            isSelected: selectedOptions.contains(option)
                        ) {
                            toggle(option)
                        }
                    }

                    if selectedOptions.contains(.custom) {
                        Text("추가하시고 싶은 기능을 알려주세요 (선택)")
                            .font(AppTheme.Typography.font(size: 16, weight: .bold))
                            .foregroundStyle(AppTheme.Colors.textPrimary)
                            .padding(.top, 8)

                        customTextEditor
                    }
                }
                .padding(.horizontal, AppTheme.Layout.horizontalPadding)
                .padding(.top, 12)
                .padding(.bottom, 24)
            }

            PrimaryButton(title: "피드백 보내기", isDisabled: !canSubmit) {
                isSubmitted = true
            }
            .padding(.horizontal, AppTheme.Layout.horizontalPadding)
            .padding(.bottom, 14)
        }
    }

    private var customTextEditor: some View {
        ZStack(alignment: .topLeading) {
            RoundedRectangle(cornerRadius: 8, style: .continuous)
                .fill(AppTheme.Colors.surface)

            if customText.isEmpty {
                Text("추가하시고 싶은 기능을 입력해주세요.")
                    .font(AppTheme.Typography.font(size: 15, weight: .regular))
                    .foregroundStyle(AppTheme.Colors.textSecondary)
                    .padding(.horizontal, 16)
                    .padding(.top, 16)
                    .allowsHitTesting(false)
            }

            TextEditor(text: $customText)
                .font(AppTheme.Typography.font(size: 15, weight: .regular))
                .foregroundStyle(AppTheme.Colors.textPrimary)
                .scrollContentBackground(.hidden)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
        }
        .frame(height: 140)
    }

    private var completionContent: some View {
        VStack(spacing: 0) {
            Spacer()

            Image("onboarding_feedback")
                .resizable()
                .scaledToFit()
                .frame(width: 240, height: 240)
                .accessibilityHidden(true)

            Text("피드백이 전송됐어요!")
                .font(AppTheme.Typography.font(size: 24, weight: .bold))
                .foregroundStyle(AppTheme.Colors.textPrimary)
                .padding(.top, 12)

            Text("소중한 의견 정말 감사해요 🙏\n더 좋은 런파민이 되도록 노력할게요!")
                .font(AppTheme.Typography.font(size: 16, weight: .medium))
                .foregroundStyle(AppTheme.Colors.textSecondary)
                .multilineTextAlignment(.center)
                .lineSpacing(4)
                .padding(.top, 16)

            Spacer()

            PrimaryButton(title: "확인") {
                dismiss()
            }
            .padding(.horizontal, AppTheme.Layout.horizontalPadding)
            .padding(.bottom, 14)
        }
    }

    private func toggle(_ option: FeedbackOption) {
        if selectedOptions.contains(option) {
            selectedOptions.remove(option)
        } else {
            selectedOptions.insert(option)
        }
    }
}

private struct FeedbackOptionRow: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 12) {
                checkbox
                Text(title)
                    .font(AppTheme.Typography.font(size: 16, weight: isSelected ? .semibold : .medium))
                    .foregroundStyle(isSelected ? AppTheme.Colors.primary : AppTheme.Colors.textPrimary)
                Spacer(minLength: 0)
            }
            .padding(.horizontal, 16)
            .frame(height: 60)
            .background(
                RoundedRectangle(cornerRadius: 10, style: .continuous)
                    .fill(isSelected ? AppTheme.Colors.primary.opacity(0.06) : Color.white)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 10, style: .continuous)
                    .stroke(
                        isSelected ? AppTheme.Colors.primary : AppTheme.Colors.border.opacity(0.75),
                        lineWidth: isSelected ? 1.5 : 1
                    )
            )
        }
        .buttonStyle(.plain)
    }

    private var checkbox: some View {
        RoundedRectangle(cornerRadius: 6, style: .continuous)
            .fill(isSelected ? AppTheme.Colors.primary : Color.clear)
            .frame(width: 24, height: 24)
            .overlay(
                RoundedRectangle(cornerRadius: 6, style: .continuous)
                    .stroke(isSelected ? AppTheme.Colors.primary : AppTheme.Colors.border, lineWidth: 1.5)
            )
            .overlay(
                Image(systemName: "checkmark")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundStyle(.white)
                    .opacity(isSelected ? 1 : 0)
            )
    }
}

#Preview {
    FeedbackView()
}
