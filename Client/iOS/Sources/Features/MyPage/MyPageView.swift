import SwiftUI

struct MyPageView: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.openURL) private var openURL
    @State private var isChangingNickname = false
    @State private var isDeletingAccount = false
    @State private var isLoggingOut = false
    @State private var isShowingLogoutConfirmation = false
    @State private var isShowingDeleteAccountConfirmation = false
    @State private var deleteAccountErrorMessage: String?
    @State private var logoutErrorMessage: String?
    @State private var nickname: String
    private let store: LocalAppStateStore
    private let profileService: ProfileServiceProtocol
    private let authService: AuthServiceProtocol
    private let accessToken: String?
    private let onLogoutCompleted: () -> Void
    private let onNicknameChanged: (String) -> Void

    init(
        store: LocalAppStateStore,
        profileService: ProfileServiceProtocol = MockProfileService(),
        authService: AuthServiceProtocol = MockAuthService(),
        accessToken: String? = nil,
        onLogoutCompleted: @escaping () -> Void = {},
        onNicknameChanged: @escaping (String) -> Void = { _ in }
    ) {
        self.store = store
        self.profileService = profileService
        self.authService = authService
        self.accessToken = accessToken
        self.onLogoutCompleted = onLogoutCompleted
        self.onNicknameChanged = onNicknameChanged
        _nickname = State(initialValue: store.nickname)
    }

    var body: some View {
        ZStack {
            VStack(spacing: 0) {
                ZStack {
                    Text("마이페이지")
                        .font(AppTheme.Typography.font(size: 20, weight: .semibold))
                        .foregroundStyle(AppTheme.Colors.textPrimary)

                    HStack {
                        Spacer()
                        Button(action: { dismiss() }) {
                            Image(systemName: "xmark")
                                .font(.system(size: 17, weight: .semibold))
                                .foregroundStyle(AppTheme.Colors.textPrimary)
                                .frame(width: 44, height: 44)
                                .contentShape(Rectangle())
                        }
                        .accessibilityLabel("닫기")
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: 60)
                .padding(.horizontal, 18)
                .padding(.top, 50)

                VStack(spacing: 0) {
                    Circle()
                        .fill(AppTheme.Colors.surface)
                        .frame(width: 76, height: 76)
                        .overlay {
                            Image(systemName: "person")
                                .font(.system(size: 34, weight: .medium))
                                .foregroundStyle(AppTheme.Colors.textSecondary.opacity(0.7))
                        }
                        .overlay {
                            Circle()
                                .stroke(AppTheme.Colors.border, lineWidth: 1)
                        }
                        .padding(.top, 34)

                    Text(nickname)
                        .font(AppTheme.Typography.font(size: 20, weight: .bold))
                        .foregroundStyle(AppTheme.Colors.textPrimary)
                        .padding(.top, 16)

                    VStack(alignment: .leading, spacing: 12) {
                        sectionTitle("계정 설정")
                        settingsRow(icon: "ic_write", title: "닉네임 변경", subtitle: "닉네임을 변경할 수 있습니다.") {
                            isChangingNickname = true
                        }
                        .disabled(isBusy)

                        settingsRow(icon: "ic_logout", title: "로그아웃", subtitle: "계정에서 로그아웃합니다", isDestructive: true) {
                            isShowingLogoutConfirmation = true
                        }
                        .disabled(isBusy)

                        if let logoutErrorMessage {
                            Text(logoutErrorMessage)
                                .font(AppTheme.Typography.caption1)
                                .foregroundStyle(AppTheme.Colors.danger)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }

                        settingsRow(icon: "ic_trash", title: "회원탈퇴", subtitle: "계정을 삭제합니다", isDestructive: true) {
                            isShowingDeleteAccountConfirmation = true
                        }
                        .disabled(isBusy)

                        if let deleteAccountErrorMessage {
                            Text(deleteAccountErrorMessage)
                                .font(AppTheme.Typography.caption1)
                                .foregroundStyle(AppTheme.Colors.danger)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }

                        sectionTitle("약관 및 정책")
                            .padding(.top, 4)
                        settingsRow(icon: "ic_shield", title: "개인정보처리방침", subtitle: "개인정보 수집 및 이용에 대한 안내") {
                            openURL(TermsAgreementID.privacy.documentURL)
                        }
                        settingsRow(icon: "ic_page", title: "이용약관", subtitle: "서비스 이용에 관한 약관을 확인하세요") {
                            openURL(TermsAgreementID.service.documentURL)
                        }

                        sectionTitle("기타")
                            .padding(.top, 4)
                        appInfoRow()
                    }
                    .padding(.horizontal, AppTheme.Layout.horizontalPadding)
                    .padding(.top, 48)

                    Spacer()
                }
            }
            .background(Color.white)
            .runpamineBackSwipe {
                dismiss()
            }
            .sheet(isPresented: $isChangingNickname) {
                MyNicknameChangeView(store: store, profileService: profileService, accessToken: accessToken) { nextNickname in
                    nickname = nextNickname
                    onNicknameChanged(nextNickname)
                    isChangingNickname = false
                }
                .networkErrorOverlay()
                .presentationDetents([.large])
            }

            if isShowingLogoutConfirmation {
                RunpamineConfirmationDialog(
                    title: "로그아웃",
                    message: "정말 로그아웃 하시겠습니까?",
                    dismissText: "취소",
                    confirmText: "로그아웃",
                    confirmButtonColor: AppTheme.Colors.primary,
                    dismissButtonColor: Color(red: 123/255, green: 123/255, blue: 123/255),
                    dismissButtonTextColor: .white,
                    titleColor: AppTheme.Colors.textPrimary,
                    messageColor: AppTheme.Colors.textSecondary,
                    confirmButtonWidthRatio: 0.6,
                    verticalPadding: 24,
                    messageTopPadding: 16,
                    buttonTopPadding: 28,
                    onDismiss: {
                        isShowingLogoutConfirmation = false
                    },
                    onConfirm: {
                        isShowingLogoutConfirmation = false
                        Task {
                            await logout()
                        }
                    }
                )
            }

            if isShowingDeleteAccountConfirmation {
                RunpamineConfirmationDialog(
                    title: "회원탈퇴",
                    message: "회원 탈퇴 시 모든 정보가 삭제됩니다.\n정말 탈퇴하시겠습니까?",
                    dismissText: "취소",
                    confirmText: "회원탈퇴",
                    confirmButtonColor: AppTheme.Colors.danger,
                    dismissButtonColor: Color(red: 123/255, green: 123/255, blue: 123/255),
                    dismissButtonTextColor: .white,
                    titleColor: AppTheme.Colors.textPrimary,
                    messageColor: AppTheme.Colors.textSecondary,
                    confirmButtonWidthRatio: 0.6,
                    verticalPadding: 24,
                    messageTopPadding: 16,
                    buttonTopPadding: 28,
                    onDismiss: {
                        isShowingDeleteAccountConfirmation = false
                    },
                    onConfirm: {
                        isShowingDeleteAccountConfirmation = false
                        Task {
                            await deleteAccount()
                        }
                    }
                )
            }
        }
    }

    private var isBusy: Bool {
        isLoggingOut || isDeletingAccount
    }

    @MainActor
    private func logout() async {
        guard !isBusy else { return }

        guard let accessToken else {
            completeLocalSessionRemoval()
            return
        }

        isLoggingOut = true
        logoutErrorMessage = nil
        deleteAccountErrorMessage = nil
        defer {
            isLoggingOut = false
        }

        do {
            try await authService.logout(accessToken: accessToken)
            completeLocalSessionRemoval(shouldResetOnboarding: false)
        } catch {
            logoutErrorMessage = error.localizedDescription
        }
    }

    @MainActor
    private func deleteAccount() async {
        guard !isBusy else { return }
        guard let accessToken else {
            deleteAccountErrorMessage = AuthError.unavailable.localizedDescription
            return
        }

        isDeletingAccount = true
        deleteAccountErrorMessage = nil
        logoutErrorMessage = nil
        defer {
            isDeletingAccount = false
        }

        do {
            try await authService.deleteAccount(accessToken: accessToken)
            completeLocalSessionRemoval(shouldResetOnboarding: true)
        } catch {
            deleteAccountErrorMessage = error.localizedDescription
        }
    }

    private func completeLocalSessionRemoval(shouldResetOnboarding: Bool = false) {
        if shouldResetOnboarding {
            store.deleteAccount()
        } else {
            store.logout()
        }
        dismiss()
        onLogoutCompleted()
    }

    private func sectionTitle(_ title: String) -> some View {
        Text(title)
            .font(AppTheme.Typography.font(size: 15, weight: .black))
            .foregroundStyle(AppTheme.Colors.textPrimary)
            .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func settingsRow(
        icon: String,
        title: String,
        subtitle: String,
        isDestructive: Bool = false,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            HStack(spacing: 14) {
                Image(icon)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 24, height: 24)
                    .frame(width: 28)

                VStack(alignment: .leading, spacing: 3) {
                    Text(title)
                        .font(AppTheme.Typography.font(size: 15, weight: .bold))
                        .foregroundStyle(isDestructive ? AppTheme.Colors.danger : AppTheme.Colors.textPrimary)
                    Text(subtitle)
                        .font(AppTheme.Typography.caption1)
                        .foregroundStyle(AppTheme.Colors.textSecondary)
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundStyle(AppTheme.Colors.textSecondary.opacity(0.55))
            }
            .padding(.horizontal, 16)
            .frame(height: 56)
            .background(Color.white)
            .overlay {
                RoundedRectangle(cornerRadius: 8, style: .continuous)
                    .stroke(AppTheme.Colors.border.opacity(0.75), lineWidth: 1)
            }
        }
    }

    private func appInfoRow() -> some View {
        HStack(spacing: 14) {
            Image("ic_info")
                .resizable()
                .scaledToFit()
                .frame(width: 24, height: 24)
                .frame(width: 28)

            VStack(alignment: .leading, spacing: 3) {
                Text("앱 정보")
                    .font(AppTheme.Typography.font(size: 15, weight: .bold))
                    .foregroundStyle(AppTheme.Colors.textPrimary)
                Text("버전 1.0.0")
                    .font(AppTheme.Typography.caption1)
                    .foregroundStyle(AppTheme.Colors.textSecondary)
            }
            Spacer()
        }
        .padding(.horizontal, 16)
        .frame(height: 56)
        .background(Color.white)
        .overlay {
            RoundedRectangle(cornerRadius: 8, style: .continuous)
                .stroke(AppTheme.Colors.border.opacity(0.75), lineWidth: 1)
        }
    }
}

#Preview {
    MyPageView(store: LocalAppStateStore(defaults: .standard))
}
