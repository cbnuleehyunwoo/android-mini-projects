import SwiftUI

struct TeamJoinView: View {
    @StateObject private var viewModel: TeamJoinViewModel
    @FocusState private var isCodeFocused: Bool
    let onDismiss: () -> Void
    let onJoined: (RunningTeam) -> Void

    init(viewModel: TeamJoinViewModel, onDismiss: @escaping () -> Void, onJoined: @escaping (RunningTeam) -> Void) {
        _viewModel = StateObject(wrappedValue: viewModel)
        self.onDismiss = onDismiss
        self.onJoined = onJoined
    }

    var body: some View {
        VStack(spacing: 0) {
            TopNavigationBar(title: "팀 참가") {
                onDismiss()
            }
            .padding(.horizontal, 18)
            .padding(.top, 8)

            VStack(alignment: .leading, spacing: 0) {
                Text("팀 초대 코드를\n입력해주세요")
                    .font(AppTheme.Typography.font(size: 29, weight: .black))
                    .lineSpacing(7)
                    .foregroundStyle(.black)
                    .padding(.top, 28)

                TextField("예: ABCDEF", text: $viewModel.inviteCode)
                    .font(AppTheme.Typography.title2)
                    .foregroundColor(.black)
                    .padding(.horizontal, 20)
                    .frame(height: 64)
                    .background(Color.white)
                    .overlay {
                        RoundedRectangle(cornerRadius: AppTheme.Layout.cornerRadius, style: .continuous)
                            .stroke(viewModel.hasError ? AppTheme.Colors.danger : AppTheme.Colors.primary, lineWidth: 2)
                    }
                    .clipShape(RoundedRectangle(cornerRadius: AppTheme.Layout.cornerRadius, style: .continuous))
                    .focused($isCodeFocused)
                    .padding(.top, 18)

                if viewModel.hasError {
                    HStack(spacing: 10) {
                        Image(systemName: "xmark")
                            .font(.system(size: 13, weight: .bold))
                        Text("팀 코드가 올바르지 않습니다.")
                            .font(AppTheme.Typography.body1)
                    }
                    .foregroundStyle(AppTheme.Colors.danger)
                    .padding(.top, 18)
                    .padding(.leading, 8)
                }

                Spacer()

                PrimaryButton(
                    title: "팀 참가하기",
                    isLoading: viewModel.isLoading,
                    isDisabled: !viewModel.canSubmit
                ) {
                    Task {
                        await viewModel.joinTeam()
                        if let team = viewModel.joinedTeam {
                            onJoined(team)
                            onDismiss()
                        }
                    }
                }
                .padding(.bottom, 34)
            }
            .padding(.horizontal, AppTheme.Layout.horizontalPadding)
        }
        .background(Color.white)
        .runpamineBackSwipe {
            onDismiss()
        }
        .onAppear {
            isCodeFocused = true
        }
    }
}
