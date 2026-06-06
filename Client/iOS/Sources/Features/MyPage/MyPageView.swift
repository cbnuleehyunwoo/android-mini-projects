import SwiftUI

struct MyPageView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var isChangingNickname = false
    @State private var nickname: String
    private let store: LocalAppStateStore
    private let onNicknameChanged: (String) -> Void

    init(store: LocalAppStateStore, onNicknameChanged: @escaping (String) -> Void = { _ in }) {
        self.store = store
        self.onNicknameChanged = onNicknameChanged
        _nickname = State(initialValue: store.nickname)
    }

    var body: some View {
        VStack(spacing: 0) {
            TopNavigationBar(title: "마이페이지") {
                dismiss()
            }
            .padding(.horizontal, 18)
            .padding(.top, 8)

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
                    .font(AppTheme.Typography.font(size: 20, weight: .black))
                    .foregroundStyle(AppTheme.Colors.textPrimary)
                    .padding(.top, 16)

                VStack(alignment: .leading, spacing: 12) {
                    sectionTitle("계정 설정")
                    settingsRow(icon: "square.and.pencil", title: "닉네임 변경", subtitle: "닉네임을 변경할 수 있습니다.", color: AppTheme.Colors.primary) {
                        isChangingNickname = true
                    }
                    settingsRow(icon: "rectangle.portrait.and.arrow.right", title: "로그아웃", subtitle: "계정에서 로그아웃합니다", color: AppTheme.Colors.danger) {
                        store.logout()
                        dismiss()
                    }

                    sectionTitle("약관 및 정책")
                        .padding(.top, 4)
                    settingsRow(icon: "shield.checkered", title: "개인정보처리방침", subtitle: "개인정보 수집 및 이용에 대한 안내", color: AppTheme.Colors.primary) {}
                    settingsRow(icon: "doc.text", title: "이용약관", subtitle: "서비스 이용에 관한 약관을 확인하세요", color: AppTheme.Colors.primary) {}

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
            MyNicknameChangeView(store: store) { nextNickname in
                nickname = nextNickname
                onNicknameChanged(nextNickname)
                isChangingNickname = false
            }
        }
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
        color: Color,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            HStack(spacing: 14) {
                Image(systemName: icon)
                    .font(.system(size: 22, weight: .medium))
                    .foregroundStyle(color)
                    .frame(width: 28)

                VStack(alignment: .leading, spacing: 3) {
                    Text(title)
                        .font(AppTheme.Typography.font(size: 15, weight: .bold))
                        .foregroundStyle(title == "로그아웃" ? AppTheme.Colors.danger : AppTheme.Colors.textPrimary)
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
            Image(systemName: "info.circle")
                .font(.system(size: 22, weight: .medium))
                .foregroundStyle(AppTheme.Colors.primary)
                .frame(width: 28)

            VStack(alignment: .leading, spacing: 3) {
                Text("앱 정보")
                    .font(AppTheme.Typography.font(size: 15, weight: .bold))
                    .foregroundStyle(AppTheme.Colors.textPrimary)
                Text("버전 1.2.3")
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
