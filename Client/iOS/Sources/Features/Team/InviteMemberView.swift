import SwiftUI

struct InviteMemberView: View {
    @Environment(\.dismiss) private var dismiss
    let inviteCode: String

    var body: some View {
        VStack(spacing: 0) {
            TopNavigationBar(title: "팀 초대") {
                dismiss()
            }
            .padding(.horizontal, 18)
            .padding(.top, 8)

            VStack(spacing: 0) {
                Text("초대 코드")
                    .font(AppTheme.Typography.font(size: 18, weight: .black))
                    .foregroundStyle(AppTheme.Colors.textPrimary)
                    .padding(.top, 24)

                Text("아래 코드를 공유하면 누구든 팀에 참여할 수\n있어요")
                    .font(AppTheme.Typography.caption1)
                    .multilineTextAlignment(.center)
                    .foregroundStyle(AppTheme.Colors.textSecondary)
                    .padding(.top, 14)

                HStack(spacing: 8) {
                    ForEach(Array(inviteCode), id: \.self) { character in
                        Text(String(character))
                            .font(AppTheme.Typography.font(size: 24, weight: .black))
                            .foregroundStyle(character.isLetter ? Color(red: 0.38, green: 0.42, blue: 1.0) : AppTheme.Colors.textPrimary)
                            .frame(width: 36, height: 42)
                            .background(Color.white)
                            .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                            .shadow(color: .black.opacity(0.10), radius: 7, x: 0, y: 3)
                    }
                }
                .padding(.vertical, 20)
                .frame(maxWidth: .infinity)
                .background(AppTheme.Colors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                .padding(.top, 18)

                Button(action: {}) {
                    Label("코드 복사", systemImage: "doc.on.doc")
                        .font(AppTheme.Typography.font(size: 13, weight: .bold))
                        .foregroundStyle(Color(red: 0.38, green: 0.42, blue: 1.0))
                        .frame(width: 214, height: 40)
                        .background(Color(red: 0.92, green: 0.94, blue: 1.0))
                        .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                }
                .padding(.top, 16)

                Spacer()
            }
            .padding(.horizontal, 16)
        }
        .background(Color.white)
        .runpamineBackSwipe {
            dismiss()
        }
    }
}
