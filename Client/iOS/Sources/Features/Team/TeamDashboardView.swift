import SwiftUI

struct TeamDashboardView: View {
    let team: RunningTeam?
    let nickname: String
    let onCreateTeam: () -> Void
    let onJoinTeam: () -> Void
    let onInvite: () -> Void
    @State private var records: [RunningRecord] = RunningHistoryStore().load()

    var body: some View {
        if team == nil {
            TeamEmptyStateView(onCreateTeam: onCreateTeam, onJoinTeam: onJoinTeam)
        } else {
            teamContent
        }
    }

    private var teamContent: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                HStack(alignment: .firstTextBaseline, spacing: 12) {
                    Text(team?.name ?? "")
                        .font(AppTheme.Typography.font(size: 36, weight: .black))
                        .foregroundStyle(AppTheme.Colors.primary)
                        .lineLimit(1)
                        .minimumScaleFactor(0.72)

                    Spacer()

                    Button(action: onInvite) {
                        Image("icon_plus")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 36, height: 36)
                            .frame(width: 48, height: 48)
                    }
                    .accessibilityLabel("팀원 초대")
                }
                .padding(.horizontal, 24)
                .padding(.top, 32)

                Text("2026년 6월 2일 - 화요일")
                    .font(AppTheme.Typography.font(size: 24, weight: .black))
                    .foregroundStyle(.black)
                    .padding(.top, 10)

                HStack(spacing: 8) {
                    metricCard(value: teamDistanceText, label: "팀 총 거리")
                    metricCard(value: "\(completedMemberCount) / \(memberCards.count)", label: "완료 / 전체")
                }
                .padding(.horizontal, 24)
                .padding(.top, 20)

                VStack(spacing: 28) {
                    ForEach(memberCards, id: \.id) { member in
                        TeamMemberRunCard(member: member)
                    }
                }
                .padding(.horizontal, 28)
                .padding(.top, 28)
                .padding(.bottom, 98)
            }
        }
        .background(Color.white)
        .onAppear {
            records = RunningHistoryStore().load()
        }
    }

    private func metricCard(value: String, label: String) -> some View {
        VStack(spacing: 4) {
            Text(value)
                .font(AppTheme.Typography.font(size: 26, weight: .black))
                .foregroundStyle(AppTheme.Colors.primary)
            Text(label)
                .font(AppTheme.Typography.font(size: 14, weight: .medium))
                .foregroundStyle(AppTheme.Colors.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 90)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        .shadow(color: .black.opacity(0.10), radius: 8, x: 0, y: 2)
    }

    private var memberCards: [TeamMemberCardModel] {
        [
            TeamMemberCardModel.runningMember(
                id: "member-primary",
                name: nickname,
                imageName: "encho",
                records: records
            ),
            TeamMemberCardModel.emptyBurger(index: 1)
        ]
    }

    private var teamDistanceText: String {
        let totalDistance = records.reduce(0) { $0 + $1.distanceKilometers }
        return "\(totalDistance.formatted(.number.precision(.fractionLength(1)))) km"
    }

    private var completedMemberCount: Int {
        memberCards.filter(\.hasRunRecord).count
    }
}

private struct TeamEmptyStateView: View {
    let onCreateTeam: () -> Void
    let onJoinTeam: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Text("팀")
                    .font(AppTheme.Typography.header1)
                    .foregroundStyle(AppTheme.Colors.primary)
                Spacer()
            }
            .padding(.horizontal, 24)
            .padding(.top, 36)

            Spacer()

            VStack(spacing: 18) {
                ZStack {
                    Circle()
                        .fill(AppTheme.Colors.primary.opacity(0.10))
                        .frame(width: 94, height: 94)
                    Image(systemName: "person.3.fill")
                        .font(.system(size: 38, weight: .semibold))
                        .foregroundStyle(AppTheme.Colors.primary)
                }

                Text("아직 참여한 팀이 없어요")
                    .font(AppTheme.Typography.font(size: 25, weight: .black))
                    .foregroundStyle(AppTheme.Colors.textPrimary)
                    .padding(.top, 8)

                Text("팀을 만들거나 초대 코드로\n팀에 참가해보세요.")
                    .font(AppTheme.Typography.font(size: 16, weight: .semibold))
                    .foregroundStyle(AppTheme.Colors.textSecondary)
                    .multilineTextAlignment(.center)
                    .lineSpacing(5)
            }

            VStack(spacing: 12) {
                Button(action: onCreateTeam) {
                    HStack(spacing: 12) {
                        Image(systemName: "plus")
                            .font(.system(size: 21, weight: .black))
                        VStack(alignment: .leading, spacing: 3) {
                            Text("팀 생성하기")
                                .font(AppTheme.Typography.font(size: 18, weight: .black))
                            Text("새 팀을 만들고 팀원을 초대해요")
                                .font(AppTheme.Typography.font(size: 12, weight: .semibold))
                                .opacity(0.82)
                        }
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.system(size: 16, weight: .black))
                    }
                    .foregroundStyle(.white)
                    .padding(.horizontal, 22)
                    .frame(height: 76)
                    .background(AppTheme.Colors.primary)
                    .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                }

                Button(action: onJoinTeam) {
                    HStack(spacing: 12) {
                        Image(systemName: "key.fill")
                            .font(.system(size: 20, weight: .black))
                        VStack(alignment: .leading, spacing: 3) {
                            Text("팀 참가하기")
                                .font(AppTheme.Typography.font(size: 18, weight: .black))
                            Text("초대 코드로 기존 팀에 들어가요")
                                .font(AppTheme.Typography.font(size: 12, weight: .semibold))
                                .opacity(0.72)
                        }
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.system(size: 16, weight: .black))
                    }
                    .foregroundStyle(AppTheme.Colors.primary)
                    .padding(.horizontal, 22)
                    .frame(height: 76)
                    .background(AppTheme.Colors.surface)
                    .overlay {
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .stroke(AppTheme.Colors.primary, lineWidth: 1.5)
                    }
                    .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                }
            }
            .padding(.horizontal, 24)
            .padding(.top, 54)
            .padding(.bottom, 98)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.white)
    }
}

private struct TeamMemberCardModel: Identifiable {
    let id: String
    let name: String
    let imageName: String
    let distanceText: String
    let timeText: String
    let paceText: String
    let caloriesText: String
    let hasRunRecord: Bool

    static func runningMember(
        id: String,
        name: String,
        imageName: String,
        records: [RunningRecord]
    ) -> TeamMemberCardModel {
        let totalDistanceMeters = records.reduce(0) { $0 + $1.distanceMeters }
        let totalElapsedTime = records.reduce(0) { $0 + $1.elapsedTime }
        let totalCalories = records.reduce(0) { $0 + $1.estimatedCalories }
        let totalDistanceKilometers = totalDistanceMeters / 1_000
        let averagePace = totalDistanceKilometers > 0.01 ? totalElapsedTime / totalDistanceKilometers : nil

        return TeamMemberCardModel(
            id: id,
            name: name,
            imageName: imageName,
            distanceText: "\(totalDistanceKilometers.formatted(.number.precision(.fractionLength(1)))) km",
            timeText: totalElapsedTime > 0 ? RunningMetricFormatter.duration(totalElapsedTime) : "--:--",
            paceText: "\(RunningMetricFormatter.pace(averagePace))/km",
            caloriesText: "\(totalCalories)",
            hasRunRecord: !records.isEmpty
        )
    }

    static func emptyBurger(index: Int) -> TeamMemberCardModel {
        TeamMemberCardModel(
            id: "member-empty-\(index)",
            name: "버거킹 스마일",
            imageName: "bk",
            distanceText: "0.0 km",
            timeText: "--:--",
            paceText: "0'00\"/km",
            caloriesText: "0",
            hasRunRecord: false
        )
    }
}

private struct TeamMemberRunCard: View {
    let member: TeamMemberCardModel

    var body: some View {
        VStack(alignment: .leading, spacing: 24) {
            Text(member.name)
                .font(AppTheme.Typography.font(size: 24, weight: .black))
                .foregroundStyle(.black)

            HStack(spacing: 0) {
                Image(member.imageName)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 80, height: 80)
                    .clipped()

                Spacer()
                    .frame(width: 14)

                TeamRunMetricsBlock(member: member)

                Spacer(minLength: 0)

                TeamCaloriesBadge(caloriesText: member.caloriesText)
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 26)
        .padding(.bottom, 32)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
        .shadow(color: .black.opacity(0.22), radius: 9, x: 0, y: 6)
    }
}

private struct TeamRunMetricsBlock: View {
    let member: TeamMemberCardModel

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            TeamRunMetricRow(icon: "icon_footprint", label: "거리", value: member.distanceText)
            TeamRunMetricRow(icon: "icon_metric_time", label: "시간", value: member.timeText)
            TeamRunMetricRow(icon: "icon_metric_pace", label: "페이스", value: member.paceText)
        }
        .frame(width: 136, alignment: .leading)
    }
}

private struct TeamRunMetricRow: View {
    let icon: String
    let label: String
    let value: String

    var body: some View {
        HStack(spacing: 0) {
            TeamRunMetricIcon(icon: icon)

            Text(label)
                .font(AppTheme.Typography.font(size: 14, weight: .medium))
                .foregroundStyle(Color(red: 0.58, green: 0.64, blue: 0.72))
                .lineLimit(1)
                .frame(width: 48, alignment: .leading)

            Text(value)
                .font(AppTheme.Typography.font(size: 14, weight: .black))
                .foregroundStyle(AppTheme.Colors.textPrimary)
                .lineLimit(1)
                .minimumScaleFactor(0.72)
                .frame(width: 64, alignment: .leading)
        }
        .frame(width: 136, height: 20, alignment: .leading)
    }
}

private struct TeamRunMetricIcon: View {
    let icon: String

    var body: some View {
        Image(icon)
            .resizable()
            .renderingMode(.template)
            .scaledToFit()
            .foregroundStyle(.black)
            .frame(width: 18, height: 18, alignment: .leading)
            .frame(width: 24, height: 20, alignment: .leading)
    }
}

private struct TeamCaloriesBadge: View {
    let caloriesText: String

    var body: some View {
        VStack(spacing: 1) {
            Image("icon_metric_kcal")
                .resizable()
                .renderingMode(.template)
                .scaledToFit()
                .foregroundStyle(Color(red: 0.93, green: 0.33, blue: 0.05))
                .frame(width: 23, height: 23)

            Text(caloriesText)
                .font(AppTheme.Typography.font(size: 28, weight: .black))

            Text("kcal")
                .font(AppTheme.Typography.font(size: 13, weight: .medium))
        }
        .padding(.horizontal, 3)
        .padding(.vertical, 6.5)
        .foregroundStyle(Color(red: 0.93, green: 0.33, blue: 0.05))
        .frame(width: 70, height: 90)
        .background(Color(red: 1.0, green: 0.95, blue: 0.91))
        .overlay {
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(Color(red: 1.0, green: 0.72, blue: 0.49), lineWidth: 1)
        }
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }
}

#Preview("팀 기록 화면") {
    TeamDashboardView(
        team: RunningTeam(
            id: UUID(),
            name: "런파민",
            distanceKilometers: 324,
            memberCount: 1,
            memberLimit: 4,
            inviteCode: "RUN200"
        ),
        nickname: "커비",
        onCreateTeam: {},
        onJoinTeam: {},
        onInvite: {}
    )
}

#Preview("팀원 기록 카드") {
    TeamMemberRunCard(
        member: TeamMemberCardModel(
            id: "member-preview",
            name: "커비",
            imageName: "encho",
            distanceText: "12.0 km",
            timeText: "10:00",
            paceText: "0'50\"/km",
            caloriesText: "200",
            hasRunRecord: true
        )
    )
    .padding(20)
    .frame(width: 390)
    .background(Color(red: 0.96, green: 0.96, blue: 0.96))
    .previewLayout(.sizeThatFits)
}
