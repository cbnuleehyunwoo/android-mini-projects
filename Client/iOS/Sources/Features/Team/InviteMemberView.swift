import SwiftUI
import UIKit

struct InviteMemberView: View {
    let inviteCode: String
    let onDismiss: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            TopNavigationBar(title: "팀 초대") {
                onDismiss()
            }
            .padding(.horizontal, 18)
            .padding(.top, 8)

            VStack(spacing: 0) {
                Text("아래 코드를 공유하면 누구든 팀에 참여할 수 있어요")
                    .font(AppTheme.Typography.font(size: 16, weight: .medium))
                    .lineLimit(1)
                    .minimumScaleFactor(0.82)
                    .foregroundStyle(.black)
                    .padding(.top, 44)

                HStack(spacing: 8) {
                    ForEach(Array(inviteCode.enumerated()), id: \.offset) { _, character in
                        Text(String(character))
                            .font(AppTheme.Typography.font(size: 24, weight: .bold))
                            .foregroundStyle(character.isLetter ? AppTheme.Colors.primary : AppTheme.Colors.textPrimary)
                            .frame(width: 46, height: 54)
                            .background(Color.white)
                            .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
                            .shadow(color: .black.opacity(0.09), radius: 12, x: 0, y: 5)
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: 102)
                .background(Color(red: 0.95, green: 0.96, blue: 0.98))
                .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
                .padding(.top, 48)

                Button {
                    UIPasteboard.general.string = inviteCode
                } label: {
                    Label("코드 복사", systemImage: "doc.on.doc")
                        .font(AppTheme.Typography.font(size: 18, weight: .medium))
                        .foregroundStyle(AppTheme.Colors.primary)
                        .frame(maxWidth: .infinity)
                        .frame(height: 49)
                        .background(Color(red: 0.93, green: 0.95, blue: 1.0))
                        .clipShape(RoundedRectangle(cornerRadius: 15, style: .continuous))
                }
                .padding(.top, 30)

                Spacer()
            }
            .padding(.horizontal, 24)
        }
        .background(Color.white)
        .runpamineBackSwipe {
            onDismiss()
        }
    }

}
