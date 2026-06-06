import SwiftUI

struct TeamCreateView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var viewModel: TeamCreateViewModel
    @FocusState private var isNameFocused: Bool
    let onCreated: (RunningTeam) -> Void

    init(viewModel: TeamCreateViewModel, onCreated: @escaping (RunningTeam) -> Void) {
        _viewModel = StateObject(wrappedValue: viewModel)
        self.onCreated = onCreated
    }

    var body: some View {
        VStack(spacing: 0) {
            TopNavigationBar(title: "팀 생성") {
                dismiss()
            }
            .padding(.horizontal, 18)
            .padding(.top, 8)

            VStack(alignment: .leading, spacing: 0) {
                Text("팀 이름을\n입력해주세요")
                    .font(AppTheme.Typography.header2)
                    .lineSpacing(6)
                    .foregroundStyle(.black)
                    .padding(.top, 28)

                HStack(spacing: 12) {
                    Image(systemName: "person")
                        .font(.system(size: 22, weight: .medium))
                        .foregroundStyle(AppTheme.Colors.textSecondary.opacity(0.7))

                    TextField("예: 팀 커브볼", text: $viewModel.teamName)
                        .font(AppTheme.Typography.title2)
                        .foregroundColor(.black)
                        .focused($isNameFocused)
                }
                .padding(.horizontal, 16)
                .frame(height: 62)
                .background(Color.white)
                .overlay {
                    RoundedRectangle(cornerRadius: AppTheme.Layout.cornerRadius, style: .continuous)
                        .stroke(viewModel.shouldShowDuplicateError ? AppTheme.Colors.danger : AppTheme.Colors.primary, lineWidth: 2)
                }
                .clipShape(RoundedRectangle(cornerRadius: AppTheme.Layout.cornerRadius, style: .continuous))
                .padding(.top, 14)

                VStack(alignment: .leading, spacing: 9) {
                    ValidationRuleRow(text: "2-6자 이내", isValid: viewModel.hasValidLength)
                    ValidationRuleRow(text: "한글, 영문, 숫자 사용 가능", isValid: viewModel.containsOnlyAllowedCharacters)
                    ValidationRuleRow(text: "특수문자 사용 불가", isValid: viewModel.doesNotContainSpecialCharacters)
                    if viewModel.shouldShowDuplicateError {
                        ValidationRuleRow(text: "중복된 팀 이름입니다.", isValid: false)
                    }
                }
                .padding(.top, 14)
                .padding(.leading, 8)

                Spacer()

                PrimaryButton(
                    title: "팀 생성하기",
                    isLoading: viewModel.isLoading,
                    isDisabled: !viewModel.canSubmit
                ) {
                    Task {
                        await viewModel.createTeam()
                        if let team = viewModel.createdTeam {
                            onCreated(team)
                            dismiss()
                        }
                    }
                }
                .padding(.bottom, 34)
            }
            .padding(.horizontal, AppTheme.Layout.horizontalPadding)
        }
        .background(Color.white)
        .runpamineBackSwipe {
            dismiss()
        }
        .onAppear {
            isNameFocused = true
        }
    }
}
