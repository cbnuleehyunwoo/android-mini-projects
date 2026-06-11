import SwiftUI

struct TeamDashboardView: View {
    let team: RunningTeam?
    let nickname: String
    let teamService: TeamServiceProtocol
    let accessToken: String?
    let onCreateTeam: () -> Void
    let onJoinTeam: () -> Void
    let onInvite: () -> Void
    @State private var records: [RunningRecord] = RunningHistoryStore().load()
    @State private var seasonStats: TeamSeasonStats?
    @State private var hasResolvedSeasonStats = false

    var body: some View {
        Group {
            if displayTeam == nil {
                TeamEmptyStateView(onCreateTeam: onCreateTeam, onJoinTeam: onJoinTeam)
            } else if shouldShowSkeleton {
                TeamDashboardSkeletonView()
            } else {
                teamContent
            }
        }
        .task(id: team?.id) {
            await refreshSeasonStats()
        }
    }

    private var teamContent: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                HStack(alignment: .center, spacing: 12) {
                    Text(displayTeam?.name ?? "")
                        .font(AppTheme.Typography.font(size: 30, weight: .bold))
                        .foregroundStyle(AppTheme.Colors.primary)
                        .lineLimit(1)
                        .minimumScaleFactor(0.72)
                        .frame(height: 48, alignment: .center)

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

                Text(summaryDateText)
                    .font(AppTheme.Typography.font(size: 18, weight: .medium))
                    .foregroundStyle(.black)
                    .padding(.top, 10)

                HStack(spacing: 8) {
                    metricCard(value: teamDistanceText, label: "팀 총 거리")
                    metricCard(value: "\(completedMemberCount) / \(totalMemberCount)", label: "완료 / 전체")
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
                .font(AppTheme.Typography.font(size: 26, weight: .bold))
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
        if let seasonStats {
            return seasonStats.members.map { TeamMemberCardModel(member: $0) }
        }

        return [
            TeamMemberCardModel.runningMember(
                id: "member-primary",
                name: nickname,
                records: records
            ),
            TeamMemberCardModel.emptyBurger(index: 1)
        ]
    }

    private var teamDistanceText: String {
        if let seasonStats {
            return TeamDashboardFormatter.distanceKilometers(seasonStats.teamTotalDistanceMeters)
        }

        let totalDistance = records.reduce(0) { $0 + $1.distanceKilometers }
        return "\(totalDistance.formatted(.number.precision(.fractionLength(1)))) km"
    }

    private var completedMemberCount: Int {
        seasonStats?.completedMemberCount ?? memberCards.filter(\.hasRunRecord).count
    }

    private var totalMemberCount: Int {
        seasonStats?.totalMemberCount ?? memberCards.count
    }

    private var displayTeam: RunningTeam? {
        seasonStats?.runningTeam ?? team
    }

    private var shouldShowSkeleton: Bool {
        team != nil && accessToken != nil && seasonStats == nil && !hasResolvedSeasonStats
    }

    private var summaryDateText: String {
        TeamDashboardFormatter.dateString(from: Date())
    }

    @MainActor
    private func refreshSeasonStats() async {
        guard team != nil, let accessToken else { return }
        seasonStats = nil
        hasResolvedSeasonStats = false

        do {
            seasonStats = try await teamService.fetchMyTeamSeasonStats(seasonID: nil, accessToken: accessToken)
        } catch {
            seasonStats = nil
        }

        hasResolvedSeasonStats = true
    }
}

private struct TeamEmptyStateView: View {
    let onCreateTeam: () -> Void
    let onJoinTeam: () -> Void

    var body: some View {
        VStack(spacing: 34) {
            VStack(spacing: 18) {
                Text("아직 참여한 팀이 없어요")
                    .font(AppTheme.Typography.font(size: 25, weight: .black))
                    .foregroundStyle(AppTheme.Colors.textPrimary)
                    .multilineTextAlignment(.center)

                Text("팀을 만들거나 초대 코드로\n팀에 참가해보세요.")
                    .font(AppTheme.Typography.font(size: 16, weight: .semibold))
                    .foregroundStyle(AppTheme.Colors.textSecondary)
                    .multilineTextAlignment(.center)
                    .lineSpacing(5)
            }

            VStack(spacing: 12) {
                Button(action: onCreateTeam) {
                    HStack(spacing: 26) {
                        Image("icon_team_plus")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 32, height: 32)

                        VStack(alignment: .leading, spacing: 3) {
                            Text("팀 생성하기")
                                .font(AppTheme.Typography.font(size: 18, weight: .black))
                            Text("새 팀을 만들고 팀원을 초대해요")
                                .font(AppTheme.Typography.font(size: 12, weight: .semibold))
                                .opacity(0.82)
                        }
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.system(size: 24, weight: .black))
                    }
                    .foregroundStyle(.white)
                    .padding(.horizontal, 24)
                    .frame(maxWidth: .infinity)
                    .frame(height: 94)
                    .background(AppTheme.Colors.primary)
                    .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
                }

                Button(action: onJoinTeam) {
                    HStack(spacing: 26) {
                        Image("icon_team_key")
                            .resizable()
                            .renderingMode(.template)
                            .scaledToFit()
                            .frame(width: 24, height: 24)

                        VStack(alignment: .leading, spacing: 3) {
                            Text("팀 참가하기")
                                .font(AppTheme.Typography.font(size: 18, weight: .black))
                            Text("초대 코드로 기존 팀에 들어가요")
                                .font(AppTheme.Typography.font(size: 12, weight: .semibold))
                                .opacity(0.72)
                        }
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.system(size: 24, weight: .black))
                    }
                    .foregroundStyle(AppTheme.Colors.primary)
                    .padding(.horizontal, 24)
                    .frame(maxWidth: .infinity)
                    .frame(height: 94)
                    .background(AppTheme.Colors.surface)
                    .overlay {
                        RoundedRectangle(cornerRadius: 22, style: .continuous)
                            .stroke(AppTheme.Colors.primary, lineWidth: 1.5)
                    }
                    .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
                }
            }
            .frame(maxWidth: .infinity)
            .padding(.horizontal, 30)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.white)
    }
}

private struct TeamDashboardSkeletonView: View {
    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                HStack(spacing: 12) {
                    skeletonBlock(width: 220, height: 42, cornerRadius: 8)

                    Spacer()

                    skeletonBlock(width: 48, height: 48, cornerRadius: 12)
                }
                .padding(.horizontal, 24)
                .padding(.top, 32)

                skeletonBlock(width: 230, height: 30, cornerRadius: 8)
                    .padding(.top, 10)

                HStack(spacing: 8) {
                    TeamMetricSkeletonCard()
                    TeamMetricSkeletonCard()
                }
                .padding(.horizontal, 24)
                .padding(.top, 20)

                VStack(spacing: 28) {
                    ForEach(0..<3, id: \.self) { _ in
                        TeamMemberRunCardSkeleton()
                    }
                }
                .padding(.horizontal, 28)
                .padding(.top, 28)
                .padding(.bottom, 98)
            }
        }
        .background(Color.white)
        .allowsHitTesting(false)
    }

    private func skeletonBlock(width: CGFloat, height: CGFloat, cornerRadius: CGFloat) -> some View {
        RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
            .fill(TeamSkeletonStyle.fill)
            .frame(width: width, height: height)
    }
}

private struct TeamMetricSkeletonCard: View {
    var body: some View {
        VStack(spacing: 10) {
            RoundedRectangle(cornerRadius: 8, style: .continuous)
                .fill(TeamSkeletonStyle.fill)
                .frame(width: 86, height: 26)
            RoundedRectangle(cornerRadius: 6, style: .continuous)
                .fill(TeamSkeletonStyle.fill)
                .frame(width: 74, height: 14)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 90)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        .shadow(color: .black.opacity(0.10), radius: 8, x: 0, y: 2)
    }
}

private struct TeamMemberRunCardSkeleton: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 24) {
            RoundedRectangle(cornerRadius: 8, style: .continuous)
                .fill(TeamSkeletonStyle.fill)
                .frame(width: 150, height: 28)

            HStack(spacing: 0) {
                RoundedRectangle(cornerRadius: 0, style: .continuous)
                    .fill(TeamSkeletonStyle.fill)
                    .frame(width: 80, height: 80)

                Spacer()
                    .frame(width: 14)

                VStack(alignment: .leading, spacing: 10) {
                    TeamMetricRowSkeleton()
                    TeamMetricRowSkeleton()
                    TeamMetricRowSkeleton()
                }
                .frame(width: 136, alignment: .leading)

                Spacer(minLength: 0)

                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .fill(TeamSkeletonStyle.fill)
                    .frame(width: 70, height: 90)
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 26)
        .padding(.bottom, 32)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
        .shadow(color: .black.opacity(0.16), radius: 9, x: 0, y: 6)
    }
}

private struct TeamMetricRowSkeleton: View {
    var body: some View {
        HStack(spacing: 0) {
            RoundedRectangle(cornerRadius: 4, style: .continuous)
                .fill(TeamSkeletonStyle.fill)
                .frame(width: 18, height: 18)
                .frame(width: 24, height: 20, alignment: .leading)

            RoundedRectangle(cornerRadius: 4, style: .continuous)
                .fill(TeamSkeletonStyle.fill)
                .frame(width: 34, height: 16)
                .frame(width: 48, alignment: .leading)

            RoundedRectangle(cornerRadius: 4, style: .continuous)
                .fill(TeamSkeletonStyle.fill)
                .frame(width: 58, height: 16)
                .frame(width: 64, alignment: .leading)
        }
        .frame(width: 136, height: 20, alignment: .leading)
    }
}

private enum TeamSkeletonStyle {
    static let fill = Color(red: 0.92, green: 0.94, blue: 0.97)
}

private struct TeamMemberCardModel: Identifiable {
    let id: String
    let name: String
    let animation: RunpamineLottieAnimation
    let distanceText: String
    let timeText: String
    let paceText: String
    let caloriesText: String
    let hasRunRecord: Bool

    init(
        id: String,
        name: String,
        animation: RunpamineLottieAnimation,
        distanceText: String,
        timeText: String,
        paceText: String,
        caloriesText: String,
        hasRunRecord: Bool
    ) {
        self.id = id
        self.name = name
        self.animation = animation
        self.distanceText = distanceText
        self.timeText = timeText
        self.paceText = paceText
        self.caloriesText = caloriesText
        self.hasRunRecord = hasRunRecord
    }

    init(member: TeamSeasonMember) {
        id = member.id
        name = member.nickname
        animation = .teamMember(consecutiveRunDays: member.consecutiveRunDays)
        distanceText = TeamDashboardFormatter.memberDistanceKilometers(member.seasonDistanceMeters)
        timeText = member.seasonDurationSeconds > 0 ? RunningMetricFormatter.duration(TimeInterval(member.seasonDurationSeconds)) : "--:--"
        paceText = "\(RunningMetricFormatter.pace(member.averagePaceSecondsPerKilometer.map(TimeInterval.init)))/km"
        caloriesText = "\(member.seasonCalories)"
        hasRunRecord = member.consecutiveRunDays > 0
    }

    static func runningMember(
        id: String,
        name: String,
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
            animation: .teamMember(consecutiveRunDays: TeamRunStreakCalculator.consecutiveRunDays(from: records)),
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
            animation: .hamburger,
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
                .font(AppTheme.Typography.font(size: 24, weight: .bold))
                .foregroundStyle(.black)

            HStack(spacing: 0) {
                RunpamineLottieView(animation: member.animation)
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

private enum TeamDashboardFormatter {
    private static let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.timeZone = TimeZone(identifier: "Asia/Seoul")
        formatter.dateFormat = "yyyy년 M월 d일 - EEEE"
        return formatter
    }()

    static func dateString(from date: Date) -> String {
        dateFormatter.string(from: date)
    }

    static func distanceKilometers(_ distanceMeters: Int) -> String {
        let kilometers = Double(distanceMeters) / 1_000
        let formatted = kilometers.formatted(.number.precision(.fractionLength(kilometers.truncatingRemainder(dividingBy: 1) == 0 ? 0 : 1)))
        return "\(formatted) km"
    }

    static func memberDistanceKilometers(_ distanceMeters: Int) -> String {
        let kilometers = Double(distanceMeters) / 1_000
        return "\(kilometers.formatted(.number.precision(.fractionLength(1)))) km"
    }
}

private enum TeamRunStreakCalculator {
    private static var calendar: Calendar {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(identifier: "Asia/Seoul") ?? .current
        return calendar
    }

    static func consecutiveRunDays(from records: [RunningRecord], now: Date = Date()) -> Int {
        let runningDays = Set(records.map { calendar.startOfDay(for: $0.startedAt) })
        guard !runningDays.isEmpty else { return -1 }

        let today = calendar.startOfDay(for: now)
        if runningDays.contains(today) {
            var count = 0
            var day = today

            while runningDays.contains(day) {
                count += 1
                guard let previousDay = calendar.date(byAdding: .day, value: -1, to: day) else { break }
                day = previousDay
            }

            return count
        }

        guard let latestRunDay = runningDays.max() else { return -1 }
        let missedDays = calendar.dateComponents([.day], from: latestRunDay, to: today).day ?? 1
        return -max(missedDays, 1)
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
        teamService: MockTeamService(),
        accessToken: "preview-token",
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
            animation: .running,
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
}
