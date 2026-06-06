import SwiftUI

struct MyNicknameChangeView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var nickname: String
    @FocusState private var isFocused: Bool
    private let store: LocalAppStateStore
    let onChanged: (String) -> Void

    init(store: LocalAppStateStore, onChanged: @escaping (String) -> Void) {
        self.store = store
        self.onChanged = onChanged
        _nickname = State(initialValue: store.nickname)
    }

    private var trimmedNickname: String {
        nickname.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    private var canSubmit: Bool {
        NicknameValidator.isValid(trimmedNickname)
    }

    var body: some View {
        VStack(spacing: 0) {
            TopNavigationBar(title: "닉네임 변경") {
                dismiss()
            }
            .padding(.horizontal, 18)
            .padding(.top, 8)

            VStack(alignment: .leading, spacing: 0) {
                Text("사용할 닉네임을\n입력해주세요")
                    .font(AppTheme.Typography.font(size: 29, weight: .black))
                    .lineSpacing(7)
                    .foregroundStyle(.black)
                    .padding(.top, 28)

                HStack(spacing: 12) {
                    Image(systemName: "person")
                        .font(.system(size: 22, weight: .medium))
                        .foregroundStyle(AppTheme.Colors.textSecondary.opacity(0.7))

                    TextField("예: 호이", text: $nickname)
                        .font(AppTheme.Typography.title2)
                        .foregroundColor(.black)
                        .focused($isFocused)
                }
                .padding(.horizontal, 20)
                .frame(height: AppTheme.Layout.fieldHeight)
                .background(Color.white)
                .overlay {
                    RoundedRectangle(cornerRadius: AppTheme.Layout.cornerRadius, style: .continuous)
                        .stroke(AppTheme.Colors.primary, lineWidth: 2)
                }
                .clipShape(RoundedRectangle(cornerRadius: AppTheme.Layout.cornerRadius, style: .continuous))
                .padding(.top, 18)

                VStack(alignment: .leading, spacing: 10) {
                    ValidationRuleRow(text: "2-6자 이내", isValid: NicknameValidator.hasValidLength(trimmedNickname))
                    ValidationRuleRow(text: "한글, 영문, 숫자 사용 가능", isValid: !trimmedNickname.isEmpty && NicknameValidator.containsOnlyAllowedCharacters(trimmedNickname))
                    ValidationRuleRow(text: "특수문자 사용 불가", isValid: trimmedNickname.isEmpty || NicknameValidator.doesNotContainSpecialCharacters(trimmedNickname))
                }
                .padding(.top, 18)
                .padding(.leading, 8)

                Spacer()

                PrimaryButton(title: "변경하기", isDisabled: !canSubmit) {
                    store.saveNickname(trimmedNickname)
                    onChanged(trimmedNickname)
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
            isFocused = true
        }
    }
}

#Preview {
    MyNicknameChangeView(store: LocalAppStateStore(defaults: .standard)) { _ in }
}
