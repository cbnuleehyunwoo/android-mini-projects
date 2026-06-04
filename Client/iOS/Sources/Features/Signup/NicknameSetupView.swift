import SwiftUI

struct NicknameSetupView: View {
    @StateObject private var viewModel: NicknameSetupViewModel
    @FocusState private var isNicknameFocused: Bool
    let onBack: () -> Void
    let onComplete: () -> Void

    init(
        viewModel: NicknameSetupViewModel,
        onBack: @escaping () -> Void,
        onComplete: @escaping () -> Void
    ) {
        _viewModel = StateObject(wrappedValue: viewModel)
        self.onBack = onBack
        self.onComplete = onComplete
    }

    var body: some View {
        VStack(spacing: 0) {
            TopNavigationBar(title: "닉네임 변경", onBack: onBack)
                .padding(.horizontal, 18)
                .padding(.top, 8)

            VStack(alignment: .leading, spacing: 0) {
                Text("사용할 닉네임을\n입력해주세요")
                    .font(AppTheme.Typography.header1)
                    .lineSpacing(7)
                    .foregroundStyle(.black)
                    .padding(.top, 22)

                HStack(spacing: 12) {
                    Image(systemName: "person")
                        .font(.system(size: 22, weight: .medium))
                        .foregroundStyle(AppTheme.Colors.textSecondary.opacity(0.7))

                    TextField("예: 커비", text: $viewModel.nickname)
                        .font(AppTheme.Typography.title2)
                        .foregroundColor(.black)
                        .focused($isNicknameFocused)
                }
                .padding(.horizontal, 20)
                .frame(height: AppTheme.Layout.fieldHeight)
                .background(Color.white)
                .overlay {
                    RoundedRectangle(cornerRadius: AppTheme.Layout.cornerRadius, style: .continuous)
                        .stroke(isNicknameFocused ? AppTheme.Colors.primary : AppTheme.Colors.border, lineWidth: isNicknameFocused ? 2 : 1)
                }
                .clipShape(RoundedRectangle(cornerRadius: AppTheme.Layout.cornerRadius, style: .continuous))
                .padding(.top, 18)

                VStack(alignment: .leading, spacing: 10) {
                    ValidationRuleRow(text: "2-6자 이내", isValid: viewModel.hasValidLength)
                    ValidationRuleRow(text: "한글, 영문, 숫자 사용 가능", isValid: viewModel.containsOnlyAllowedCharacters)
                    ValidationRuleRow(text: "특수문자 사용 불가", isValid: viewModel.doesNotContainSpecialCharacters)
                }
                .padding(.top, 18)
                .padding(.leading, 8)

                if let message = viewModel.errorMessage {
                    Text(message)
                        .font(AppTheme.Typography.body2)
                        .foregroundStyle(AppTheme.Colors.danger)
                        .padding(.top, 14)
                        .padding(.leading, 8)
                }

                Spacer()

                PrimaryButton(
                    title: "변경하기",
                    isLoading: viewModel.isLoading,
                    isDisabled: !viewModel.canSubmit
                ) {
                    Task {
                        await viewModel.submit()
                        if viewModel.didComplete {
                            onComplete()
                        }
                    }
                }
                .padding(.bottom, 34)
            }
            .padding(.horizontal, AppTheme.Layout.horizontalPadding)
        }
        .background(Color.white)
        .onAppear {
            isNicknameFocused = true
        }
    }
}

#Preview {
    NicknameSetupView(
        viewModel: NicknameSetupViewModel(authService: MockAuthService(), agreements: []),
        onBack: {},
        onComplete: {}
    )
}
