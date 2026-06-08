import SwiftUI

struct HomeView: View {
    let nickname: String
    let team: RunningTeam?
    let onCreateTeam: () -> Void
    let onJoinTeam: () -> Void
    let onOpenMyPage: () -> Void
    let onStartRunning: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            HomeHeaderView(nickname: nickname, onOpenMyPage: onOpenMyPage)
                .padding(.horizontal, 20)
                .padding(.top, 26)

            TeamStatusCard(team: team, onCreateTeam: onCreateTeam, onJoinTeam: onJoinTeam)
                .padding(.horizontal, 20)
                .padding(.top, 28)

            ZStack(alignment: .bottom) {
                HomeMapView()
                    .frame(maxWidth: .infinity)
                    .frame(maxHeight: .infinity)
                    .padding(.horizontal, 8)

                Button(action: onStartRunning) {
                    Text("시작")
                        .font(AppTheme.Typography.font(size: 20, weight: .black))
                        .foregroundStyle(.white)
                        .frame(width: 82, height: 82)
                        .background(AppTheme.Colors.primary)
                        .clipShape(Circle())
                }
                .padding(.bottom, -32)
            }
            .padding(.top, 8)
            .padding(.bottom, 55)
            .frame(maxHeight: .infinity)
        }
        .background(Color.white)
    }

}

private struct HomeHeaderView: View {
    let nickname: String
    let onOpenMyPage: () -> Void

    var body: some View {
        HStack(spacing: 10) {
            Circle()
                .fill(AppTheme.Colors.primary)
                .frame(width: 34, height: 34)
                .overlay {
                    Image(systemName: "person.fill")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundStyle(.white)
                }

            VStack(alignment: .leading, spacing: 2) {
                Text("안녕하세요, \(nickname)님!")
                    .font(AppTheme.Typography.font(size: 12, weight: .semibold))
                    .foregroundStyle(AppTheme.Colors.textPrimary)
                Text("오늘은 뛰기 좋은 날씨네요!")
                    .font(AppTheme.Typography.caption1)
                    .foregroundStyle(AppTheme.Colors.textPrimary)
            }

            Spacer()

            Button(action: onOpenMyPage) {
                Image(systemName: "person")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundStyle(AppTheme.Colors.textPrimary)
                    .frame(width: 36, height: 36)
            }
            .accessibilityLabel("마이페이지")
        }
    }
}

private struct TeamStatusCard: View {
    let team: RunningTeam?
    let onCreateTeam: () -> Void
    let onJoinTeam: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            if let team {
                Text(team.name)
                    .font(AppTheme.Typography.font(size: 14, weight: .bold))
                    .foregroundStyle(.white)
                    .lineLimit(1)
            } else {
                Text("참여한 팀이 없어요!")
                    .font(AppTheme.Typography.font(size: 14, weight: .bold))
                    .foregroundStyle(.white)
                Text("팀에 참여하거나 팀을 만들어보세요.")
                    .font(AppTheme.Typography.font(size: 12, weight: .semibold))
                    .foregroundStyle(.white)
            }

            if team == nil {
                HStack(spacing: 48) {
                    Button("팀 생성하기 >", action: onCreateTeam)
                    Button("팀 참가하기 >", action: onJoinTeam)
                }
                .font(AppTheme.Typography.font(size: 12, weight: .black))
                .foregroundStyle(AppTheme.Colors.success)
                .padding(.top, 6)
            }
        }
        .padding(.horizontal, 40)
        .frame(maxWidth: .infinity, alignment: .leading)
        .frame(height: 88)
        .background(AppTheme.Colors.primary)
        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
    }
}

#Preview {
    HomeView(
        nickname: "러너",
        team: nil,
        onCreateTeam: {},
        onJoinTeam: {},
        onOpenMyPage: {},
        onStartRunning: {}
    )
}
