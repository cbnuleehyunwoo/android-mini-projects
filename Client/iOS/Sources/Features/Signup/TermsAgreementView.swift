import SwiftUI

struct TermsAgreementView: View {
    @StateObject private var viewModel: TermsAgreementViewModel
    let onBack: () -> Void
    let onComplete: ([TermsAgreement]) -> Void

    init(
        viewModel: TermsAgreementViewModel,
        onBack: @escaping () -> Void,
        onComplete: @escaping ([TermsAgreement]) -> Void
    ) {
        _viewModel = StateObject(wrappedValue: viewModel)
        self.onBack = onBack
        self.onComplete = onComplete
    }

    var body: some View {
        VStack(spacing: 0) {
            TopNavigationBar(title: "회원가입", onBack: onBack)
                .padding(.horizontal, 18)
                .padding(.top, 8)

            VStack(alignment: .leading, spacing: 0) {
                Text("서비스 이용을 위해 약관에 동의해주세요")
                    .font(AppTheme.Typography.font(size: 16, weight: .semibold))
                    .foregroundStyle(AppTheme.Colors.textSecondary)
                    .padding(.top, 44)

                Button {
                    viewModel.toggleAll()
                } label: {
                    HStack(spacing: 14) {
                        Image(systemName: viewModel.isAllAccepted ? "checkmark.square.fill" : "square.fill")
                            .font(.system(size: 28, weight: .semibold))
                            .foregroundStyle(viewModel.isAllAccepted ? AppTheme.Colors.primary : AppTheme.Colors.border)

                        Text("전체 동의")
                            .font(AppTheme.Typography.font(size: 16, weight: .semibold))
                            .foregroundStyle(AppTheme.Colors.textPrimary)

                        Spacer()
                    }
                    .padding(20)
                    .frame(height: 78)
                    .background(AppTheme.Colors.surface)
                    .overlay {
                        RoundedRectangle(cornerRadius: AppTheme.Layout.cornerRadius, style: .continuous)
                            .stroke(AppTheme.Colors.border, lineWidth: 1)
                    }
                    .clipShape(RoundedRectangle(cornerRadius: AppTheme.Layout.cornerRadius, style: .continuous))
                }
                .padding(.top, 42)

                VStack(spacing: 24) {
                    ForEach(viewModel.agreements) { agreement in
                        AgreementRow(
                            title: agreement.title,
                            isRequired: agreement.isRequired,
                            isAccepted: agreement.isAccepted
                        ) {
                            viewModel.toggle(agreement.id)
                        }
                    }
                }
                .padding(.top, 34)

                Spacer()

                PrimaryButton(
                    title: "가입하기",
                    isDisabled: !viewModel.canContinue,
                    action: {
                        onComplete(viewModel.acceptedRequiredAgreements)
                    }
                )
                .padding(.bottom, 34)
            }
            .padding(.horizontal, AppTheme.Layout.horizontalPadding)
        }
        .background(Color.white)
        .runpamineBackSwipe(onBack: onBack)
    }
}

#Preview {
    TermsAgreementView(
        viewModel: TermsAgreementViewModel(),
        onBack: {},
        onComplete: { _ in }
    )
}
