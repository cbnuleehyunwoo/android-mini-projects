import SwiftUI

final class TeamDashboardCache: ObservableObject {
    @Published var records: [RunningRecord] = []
    @Published var teamMembers: [TeamMember]?
    @Published var dailySummary: TeamDailySummary?
    @Published var teamStats: TeamStats?
    @Published var hasResolvedDashboard = false
    @Published var isSkeletonVisible = false
    @Published var selectedDate = Calendar.current.startOfDay(for: Date())
    @Published var isDateLoading = false

    func clearDashboard() {
        teamMembers = nil
        dailySummary = nil
        teamStats = nil
        hasResolvedDashboard = false
        isSkeletonVisible = false
        isDateLoading = false
        selectedDate = Calendar.current.startOfDay(for: Date())
    }
}

struct TeamDashboardView: View {
    let team: RunningTeam?
    let nickname: String
    let teamService: TeamServiceProtocol
    let accessToken: String?
    let currentUserID: String?
    @ObservedObject var cache: TeamDashboardCache
    var refreshRevision = 0
    let onCreateTeam: () -> Void
    let onJoinTeam: () -> Void
    let onInvite: () -> Void
    let onLeaveTeam: () -> Void
    let onSelectMember: (TeamMemberStatsDetail) -> Void
    @State private var isShowingTeamMenu = false
    @State private var isShowingLeaveConfirmation = false
    @State private var isLeavingTeam = false
    @State private var leaveTeamErrorMessage: String?

    var body: some View {
        Group {
            if displayTeam == nil {
                TeamEmptyStateView(onCreateTeam: onCreateTeam, onJoinTeam: onJoinTeam)
            } else if isWaitingForInitialDashboard {
                ZStack {
                    Color.white

                    TeamDashboardSkeletonView()
                        .opacity(cache.isSkeletonVisible ? 1 : 0)
                }
            } else {
                teamContent
            }
        }
        .task(id: dashboardRefreshIdentifier) {
            await refreshDashboard()
        }
    }

    private var dashboardRefreshIdentifier: String {
        "\(team?.id.uuidString ?? "no-team")|\(refreshRevision)"
    }

    private var teamContent: some View {
        ZStack(alignment: .topTrailing) {
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

                        Button {
                            withAnimation(.easeInOut(duration: 0.16)) {
                                isShowingTeamMenu.toggle()
                            }
                        } label: {
                            Image(systemName: "ellipsis")
                                .font(.system(size: 28, weight: .bold))
                                .foregroundStyle(AppTheme.Colors.primary)
                                .frame(width: 48, height: 48)
                                .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel("팀 메뉴")
                    }
                    .padding(.horizontal, 24)
                    .padding(.top, 32)

                    HStack(spacing: 8) {
                        dateButton(
                            systemName: "chevron.left",
                            accessibilityLabel: "이전 날짜",
                            isEnabled: !cache.isDateLoading,
                            action: moveToPreviousDate
                        )

                        Text(summaryDateText)
                            .font(AppTheme.Typography.font(size: 18, weight: .medium))
                            .foregroundStyle(.black)
                            .frame(minWidth: 190)

                        dateButton(
                            systemName: "chevron.right",
                            accessibilityLabel: "다음 날짜",
                            isEnabled: canMoveToNextDate && !cache.isDateLoading,
                            action: moveToNextDate
                        )
                    }
                    .padding(.top, 10)

                    HStack(spacing: 8) {
                        metricCard(value: teamDistanceText, label: "팀 총 거리")
                        metricCard(value: "\(completedMemberCount) / \(totalMemberCount)", label: "완료 / 전체")
                    }
                    .padding(.horizontal, 24)
                    .padding(.top, 20)

                    LazyVStack(spacing: 28) {
                        ForEach(memberCards, id: \.id) { member in
                            TeamMemberRunCard(member: member)
                                .contentShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
                                .onTapGesture {
                                    guard let detail = member.detail else { return }
                                    onSelectMember(detail)
                                }
                        }
                    }
                    .padding(.horizontal, 28)
                    .padding(.top, 28)
                    .padding(.bottom, 98)
                }
            }

            if isShowingTeamMenu {
                Color.black.opacity(0.001)
                    .ignoresSafeArea()
                    .onTapGesture {
                        withAnimation(.easeInOut(duration: 0.16)) {
                            isShowingTeamMenu = false
                        }
                    }

                teamMenu
                    .padding(.top, 82)
                    .padding(.trailing, 24)
                    .transition(.opacity.combined(with: .scale(scale: 0.96, anchor: .topTrailing)))
                    .zIndex(2)
            }

            if isShowingLeaveConfirmation {
                TeamLeaveConfirmationDialog(
                    isLoading: isLeavingTeam,
                    errorMessage: leaveTeamErrorMessage,
                    onDismiss: {
                        guard !isLeavingTeam else { return }
                        leaveTeamErrorMessage = nil
                        withAnimation(.easeInOut(duration: 0.18)) {
                            isShowingLeaveConfirmation = false
                        }
                    },
                    onConfirm: {
                        Task {
                            await leaveTeam()
                        }
                    }
                )
                .zIndex(3)
            }
        }
        .background(Color.white)
        .onAppear {
            if !isRemoteDashboard {
                cache.records = RunningHistoryStore().load()
            }
        }
    }

    private var teamMenu: some View {
        VStack(spacing: 0) {
            Button {
                withAnimation(.easeInOut(duration: 0.16)) {
                    isShowingTeamMenu = false
                }
                onInvite()
            } label: {
                Text("팀원 초대")
                    .font(AppTheme.Typography.font(size: 18, weight: .medium))
                    .foregroundStyle(Color.black)
                    .frame(width: 96, height: 42)
            }
            .buttonStyle(.plain)

            Rectangle()
                .fill(Color(red: 0.78, green: 0.78, blue: 0.78))
                .frame(width: 78, height: 1)

            Button {
                leaveTeamErrorMessage = nil
                withAnimation(.easeInOut(duration: 0.16)) {
                    isShowingTeamMenu = false
                    isShowingLeaveConfirmation = true
                }
            } label: {
                Text("팀 탈퇴")
                    .font(AppTheme.Typography.font(size: 18, weight: .medium))
                    .foregroundStyle(AppTheme.Colors.danger)
                    .frame(width: 96, height: 42)
            }
            .buttonStyle(.plain)
        }
        .padding(.vertical, 6)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        .overlay {
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(Color(red: 0.72, green: 0.72, blue: 0.72), lineWidth: 1)
        }
        .shadow(color: .black.opacity(0.12), radius: 10, x: 0, y: 4)
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

    private func dateButton(
        systemName: String,
        accessibilityLabel: String,
        isEnabled: Bool,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            Image(systemName: systemName)
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(isEnabled ? Color.black : Color.gray.opacity(0.3))
                .frame(width: 40, height: 40)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .disabled(!isEnabled)
        .accessibilityLabel(accessibilityLabel)
    }

    private var memberCards: [TeamMemberCardModel] {
        if let dailySummary = cache.dailySummary {
            let statsMembersByID = cache.teamStats?.members.reduce(into: [String: TeamMemberStats]()) { result, member in
                result[member.matchingID] = member
                result[member.id] = member
            } ?? [:]
            let dailyMembersByID = dailySummary.members.reduce(into: [String: TeamDailyMember]()) { result, member in
                result[member.id] = member
            }

            if let teamMembers = cache.teamMembers {
                return teamMembers.map { member in
                    TeamMemberCardModel(
                        teamMember: member,
                        dailyMember: dailyMembersByID[member.id],
                        memberStats: statsMembersByID[member.id],
                        isCurrentUser: member.id == currentUserID
                    )
                }
            }

            return dailySummary.members.map { member in
                TeamMemberCardModel(
                    member: member,
                    memberStats: statsMembersByID[member.id],
                    isCurrentUser: member.id == currentUserID
                )
            }
        }

        if let teamMembers = cache.teamMembers {
            let statsMembersByID = cache.teamStats?.members.reduce(into: [String: TeamMemberStats]()) { result, member in
                result[member.matchingID] = member
                result[member.id] = member
            } ?? [:]

            return teamMembers.map { member in
                TeamMemberCardModel(
                    teamMember: member,
                    dailyMember: nil,
                    memberStats: statsMembersByID[member.id],
                    isCurrentUser: member.id == currentUserID
                )
            }
        }

        if isRemoteDashboard {
            return []
        }

        return [
            TeamMemberCardModel.runningMember(
                id: "member-primary",
                name: nickname,
                records: cache.records,
                isCurrentUser: true
            ),
            TeamMemberCardModel.emptyBurger(index: 1)
        ]
    }

    private var teamDistanceText: String {
        if let dailySummary = cache.dailySummary {
            return TeamDashboardFormatter.distanceKilometers(dailySummary.teamTotalDistanceMeters)
        }

        if isRemoteDashboard {
            return TeamDashboardFormatter.distanceKilometers(0)
        }

        let totalDistance = cache.records.reduce(0) { $0 + $1.distanceKilometers }
        return "\(totalDistance.formatted(.number.precision(.fractionLength(2)))) km"
    }

    private var completedMemberCount: Int {
        if let dailySummary = cache.dailySummary {
            return dailySummary.completedMemberCount
        }

        return isRemoteDashboard ? 0 : memberCards.filter(\.hasRunRecord).count
    }

    private var totalMemberCount: Int {
        if let dailySummary = cache.dailySummary {
            return dailySummary.totalMemberCount
        }

        return isRemoteDashboard ? team?.memberCount ?? 0 : memberCards.count
    }

    private var displayTeam: RunningTeam? {
        cache.dailySummary?.team ?? cache.teamStats?.runningTeam ?? team
    }

    private var isWaitingForInitialDashboard: Bool {
        team != nil && accessToken != nil && cache.dailySummary == nil && !cache.hasResolvedDashboard
    }

    private var isRemoteDashboard: Bool {
        team != nil && accessToken != nil
    }

    private var summaryDateText: String {
        TeamDashboardFormatter.dateString(from: cache.selectedDate)
    }

    private var canMoveToNextDate: Bool {
        cache.selectedDate < Calendar.current.startOfDay(for: Date())
    }

    private func moveToPreviousDate() {
        moveDate(by: -1)
    }

    private func moveToNextDate() {
        guard canMoveToNextDate else { return }
        moveDate(by: 1)
    }

    private func moveDate(by value: Int) {
        guard !cache.isDateLoading else { return }
        guard let date = Calendar.current.date(byAdding: .day, value: value, to: cache.selectedDate) else { return }

        cache.selectedDate = Calendar.current.startOfDay(for: date)
        cache.isDateLoading = true
        let requestDate = cache.selectedDate
        Task {
            await refreshDailySummary(for: requestDate)
        }
    }

    @MainActor
    private func leaveTeam() async {
        guard !isLeavingTeam else { return }
        guard let accessToken else {
            leaveTeamErrorMessage = TeamError.unauthorized.localizedDescription
            return
        }

        isLeavingTeam = true
        leaveTeamErrorMessage = nil
        defer {
            isLeavingTeam = false
        }

        do {
            let result = try await teamService.leaveTeam(accessToken: accessToken)
            guard result.left else {
                leaveTeamErrorMessage = TeamError.invalidResponse.localizedDescription
                return
            }

            withAnimation(.easeInOut(duration: 0.18)) {
                isShowingLeaveConfirmation = false
            }
            cache.clearDashboard()
            onLeaveTeam()
        } catch {
            leaveTeamErrorMessage = error.localizedDescription
        }
    }

    @MainActor
    private func refreshDashboard() async {
        guard team != nil, let accessToken else { return }
        cache.selectedDate = Calendar.current.startOfDay(for: Date())
        cache.hasResolvedDashboard = false
        cache.isSkeletonVisible = false

        let clock = ContinuousClock()
        let loadingStartedAt = clock.now
        let minimumSkeletonDeadline = loadingStartedAt.advanced(by: .milliseconds(500))
        let shouldGateSkeleton = isWaitingForInitialDashboard
        if shouldGateSkeleton {
            cache.isSkeletonVisible = true
        }

        async let nextTeamMembers = optionalResult {
            try await teamService.fetchMyTeamMembers(accessToken: accessToken)
        }
        async let nextDailySummary = optionalResult {
            try await teamService.fetchDailySummary(date: cache.selectedDate, accessToken: accessToken)
        }
        async let nextTeamStats = optionalResult {
            try await teamService.fetchMyTeamStats(accessToken: accessToken)
        }

        let (teamMembers, dailySummary, teamStats) = await (
            nextTeamMembers,
            nextDailySummary,
            nextTeamStats
        )

        if shouldGateSkeleton {
            try? await clock.sleep(until: minimumSkeletonDeadline)
        }

        cache.teamMembers = teamMembers
        cache.dailySummary = dailySummary
        cache.teamStats = teamStats

        cache.hasResolvedDashboard = true
        cache.isSkeletonVisible = false
    }

    @MainActor
    private func refreshDailySummary(for date: Date) async {
        guard team != nil, let accessToken else {
            cache.isDateLoading = false
            return
        }
        defer { cache.isDateLoading = false }

        do {
            let summary = try await teamService.fetchDailySummary(date: date, accessToken: accessToken)
            guard Calendar.current.isDate(date, inSameDayAs: cache.selectedDate) else { return }
            if cache.dailySummary != summary {
                cache.dailySummary = summary
            }
        } catch {
            guard Calendar.current.isDate(date, inSameDayAs: cache.selectedDate) else { return }
            cache.dailySummary = nil
        }
    }
}

private func optionalResult<Value>(
    _ operation: () async throws -> Value
) async -> Value? {
    try? await operation()
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
                    skeletonBlock(width: 210, height: 34, cornerRadius: 8)
                        .frame(height: 48, alignment: .center)

                    Spacer()

                    skeletonBlock(width: 48, height: 48, cornerRadius: 12)
                }
                .padding(.horizontal, 24)
                .padding(.top, 32)

                skeletonBlock(width: 190, height: 22, cornerRadius: 8)
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
                .padding(.leading, 10)

            HStack(spacing: 0) {
                RoundedRectangle(cornerRadius: 0, style: .continuous)
                    .fill(TeamSkeletonStyle.fill)
                    .frame(width: 80, height: 80)
                    .padding(.leading, 10)

                Spacer()
                    .frame(width: 12)

                VStack(alignment: .leading, spacing: 10) {
                    TeamMetricRowSkeleton()
                    TeamMetricRowSkeleton()
                    TeamMetricRowSkeleton()
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                Spacer()
                    .frame(width: 10)

                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .fill(TeamSkeletonStyle.fill)
                    .frame(width: 70, height: 90)
            }
        }
        .padding(.leading, 16)
        .padding(.trailing, 25)
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
                .frame(width: 34, height: 16)
                .frame(width: 48, alignment: .leading)

            Spacer(minLength: 0)

            RoundedRectangle(cornerRadius: 4, style: .continuous)
                .fill(TeamSkeletonStyle.fill)
                .frame(width: 58, height: 16)
        }
        .frame(maxWidth: .infinity, minHeight: 20, maxHeight: 20, alignment: .leading)
    }
}

private enum TeamSkeletonStyle {
    static let fill = Color(red: 0.92, green: 0.94, blue: 0.97)
}

struct TeamMemberStatsDetail: Identifiable, Equatable {
    let id: String
    let name: String
    let joinedAt: String
    let totalDistance: String
    let totalRunCount: String
    let averagePace: String

    var joinedAtText: String {
        TeamMemberStatsDetailFormatter.joinedDateText(from: joinedAt)
    }
}

private struct TeamMemberCardModel: Identifiable {
    let id: String
    let name: String
    let animation: RunpamineLottieAnimation
    let distanceText: String
    let timeText: String
    let paceText: String
    let hasRunRecord: Bool
    let isCurrentUser: Bool
    let detail: TeamMemberStatsDetail?

    init(
        id: String,
        name: String,
        animation: RunpamineLottieAnimation,
        distanceText: String,
        timeText: String,
        paceText: String,
        hasRunRecord: Bool,
        isCurrentUser: Bool,
        detail: TeamMemberStatsDetail?
    ) {
        self.id = id
        self.name = name
        self.animation = animation
        self.distanceText = distanceText
        self.timeText = timeText
        self.paceText = paceText
        self.hasRunRecord = hasRunRecord
        self.isCurrentUser = isCurrentUser
        self.detail = detail
    }

    init(
        teamMember: TeamMember,
        dailyMember: TeamDailyMember?,
        memberStats: TeamMemberStats?,
        isCurrentUser: Bool
    ) {
        id = teamMember.id
        name = dailyMember?.nickname ?? teamMember.nickname
        animation = .teamMember(consecutiveRunDays: memberStats?.recentRunDays ?? (dailyMember?.completed == true ? 1 : nil))
        distanceText = TeamDashboardFormatter.memberDistanceKilometers(dailyMember?.distanceMeters ?? 0)
        timeText = if let durationSeconds = dailyMember?.durationSeconds, durationSeconds > 0 {
            RunningMetricFormatter.duration(TimeInterval(durationSeconds))
        } else {
            "--:--"
        }
        paceText = "\(RunningMetricFormatter.pace(dailyMember?.averagePaceSecondsPerKilometer.map(TimeInterval.init)))/km"
        hasRunRecord = dailyMember?.completed ?? false
        self.isCurrentUser = isCurrentUser
        detail = Self.statsDetail(
            memberStats: memberStats,
            dailyMember: dailyMember
        )
    }

    init(member: TeamDailyMember, memberStats: TeamMemberStats?, isCurrentUser: Bool) {
        let memberDistanceText = TeamDashboardFormatter.memberDistanceKilometers(member.distanceMeters)
        let memberPaceText = "\(RunningMetricFormatter.pace(member.averagePaceSecondsPerKilometer.map(TimeInterval.init)))/km"

        id = member.id
        name = member.nickname
        animation = .teamMember(consecutiveRunDays: memberStats?.recentRunDays ?? (member.completed ? 1 : nil))
        distanceText = memberDistanceText
        timeText = member.durationSeconds > 0 ? RunningMetricFormatter.duration(TimeInterval(member.durationSeconds)) : "--:--"
        paceText = memberPaceText
        hasRunRecord = member.completed
        self.isCurrentUser = isCurrentUser
        detail = Self.statsDetail(
            memberStats: memberStats,
            dailyMember: member
        )
    }

    private static func statsDetail(
        memberStats: TeamMemberStats?,
        dailyMember: TeamDailyMember?
    ) -> TeamMemberStatsDetail? {
        guard let memberStats else { return nil }

        return TeamMemberStatsDetail(
            id: memberStats.id,
            name: memberStats.nickname,
            joinedAt: dailyMember.flatMap { $0.teamJoinedAt.isEmpty ? nil : $0.teamJoinedAt } ?? memberStats.teamJoinedAt,
            totalDistance: TeamDashboardFormatter.totalDistanceKilometers(memberStats.distanceMeters),
            totalRunCount: "\(memberStats.runCount)",
            averagePace: RunningMetricFormatter.pace(memberStats.averagePaceSecondsPerKilometer.map(TimeInterval.init))
        )
    }

    static func runningMember(
        id: String,
        name: String,
        records: [RunningRecord],
        isCurrentUser: Bool
    ) -> TeamMemberCardModel {
        let totalDistanceMeters = records.reduce(0) { $0 + $1.distanceMeters }
        let totalElapsedTime = records.reduce(0) { $0 + $1.elapsedTime }
        let totalDistanceKilometers = totalDistanceMeters / 1_000
        let averagePace = totalDistanceKilometers > 0.01 ? totalElapsedTime / totalDistanceKilometers : nil
        let distanceText = "\(totalDistanceKilometers.formatted(.number.precision(.fractionLength(2)))) km"

        return TeamMemberCardModel(
            id: id,
            name: name,
            animation: .teamMember(consecutiveRunDays: TeamRunStreakCalculator.consecutiveRunDays(from: records)),
            distanceText: distanceText,
            timeText: totalElapsedTime > 0 ? RunningMetricFormatter.duration(totalElapsedTime) : "--:--",
            paceText: "\(RunningMetricFormatter.pace(averagePace))/km",
            hasRunRecord: !records.isEmpty,
            isCurrentUser: isCurrentUser,
            detail: TeamMemberStatsDetail(
                id: id,
                name: name,
                joinedAt: "",
                totalDistance: totalDistanceKilometers.formatted(.number.precision(.fractionLength(2))),
                totalRunCount: "\(records.count)",
                averagePace: RunningMetricFormatter.pace(averagePace)
            )
        )
    }

    static func emptyBurger(index: Int) -> TeamMemberCardModel {
        TeamMemberCardModel(
            id: "member-empty-\(index)",
            name: "버거킹 스마일",
            animation: .hamburger,
            distanceText: "0.00 km",
            timeText: "--:--",
            paceText: "0'00\"/km",
            hasRunRecord: false,
            isCurrentUser: false,
            detail: TeamMemberStatsDetail(
                id: "member-empty-\(index)",
                name: "버거킹 스마일",
                joinedAt: "",
                totalDistance: "0.00",
                totalRunCount: "0",
                averagePace: "0'00\""
            )
        )
    }
}

struct TeamMemberStatsDetailSheet: View {
    let detail: TeamMemberStatsDetail
    let onDismiss: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            HStack(alignment: .center, spacing: 16) {
                Text(detail.name)
                    .font(AppTheme.Typography.font(size: 26, weight: .bold))
                    .foregroundStyle(.white)
                    .lineLimit(1)
                    .minimumScaleFactor(0.65)

                Spacer(minLength: 0)

                Button(action: onDismiss) {
                    Image(systemName: "xmark")
                        .font(.system(size: 24, weight: .semibold))
                        .foregroundStyle(.white)
                        .frame(width: 44, height: 44)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel("닫기")
            }

            HStack(spacing: 6) {
                Image("icon_calender")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 13, height: 13)
                Text("\(detail.joinedAtText) 합류")
                    .font(AppTheme.Typography.font(size: 16, weight: .medium))
                    .lineLimit(1)
            }
            .foregroundStyle(.white.opacity(0.72))
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.top, 2)

            HStack(alignment: .top, spacing: 10) {
                detailMetric(label: "총 거리 (km)", value: detail.totalDistance)
                detailMetric(label: "총 러닝 횟수", value: detail.totalRunCount)
                detailMetric(label: "평균 페이스", value: detail.averagePace)
            }
            .padding(.top, 48)
        }
        .padding(.horizontal, 28)
        .padding(.top, 28)
        .padding(.bottom, 48)
        .frame(maxWidth: .infinity)
        .background {
            LinearGradient(
                colors: [Color(red: 0.23, green: 0.48, blue: 0.98), AppTheme.Colors.primary],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        }
        .clipShape(TeamMemberDetailTopRoundedShape(radius: 28))
        .frame(maxHeight: .infinity, alignment: .bottom)
        .ignoresSafeArea(.container, edges: .bottom)
        .accessibilityElement(children: .contain)
    }

    private func detailMetric(label: String, value: String) -> some View {
        VStack(spacing: 8) {
            Text(label)
                .font(AppTheme.Typography.font(size: 13, weight: .medium))
                .foregroundStyle(.white.opacity(0.72))
                .lineLimit(1)
                .minimumScaleFactor(0.72)

            Text(value)
                .font(AppTheme.Typography.font(size: 28, weight: .bold))
                .foregroundStyle(.white)
                .lineLimit(1)
                .minimumScaleFactor(0.7)
        }
        .frame(maxWidth: .infinity)
    }
}

private struct TeamMemberDetailTopRoundedShape: Shape {
    let radius: CGFloat

    func path(in rect: CGRect) -> Path {
        var path = Path()
        let radius = min(radius, min(rect.width, rect.height) / 2)

        path.move(to: CGPoint(x: rect.minX, y: rect.maxY))
        path.addLine(to: CGPoint(x: rect.minX, y: rect.minY + radius))
        path.addArc(
            center: CGPoint(x: rect.minX + radius, y: rect.minY + radius),
            radius: radius,
            startAngle: .degrees(180),
            endAngle: .degrees(270),
            clockwise: false
        )
        path.addLine(to: CGPoint(x: rect.maxX - radius, y: rect.minY))
        path.addArc(
            center: CGPoint(x: rect.maxX - radius, y: rect.minY + radius),
            radius: radius,
            startAngle: .degrees(270),
            endAngle: .degrees(0),
            clockwise: false
        )
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.maxY))
        path.closeSubpath()
        return path
    }
}

private struct TeamMemberRunCard: View {
    let member: TeamMemberCardModel
    @State private var isLottieVisible = false

    var body: some View {
        VStack(alignment: .leading, spacing: 24) {
            HStack(spacing: 6) {
                Text(member.name)
                    .font(AppTheme.Typography.font(size: 24, weight: .bold))
                    .foregroundStyle(.black)
                    .lineLimit(1)
                    .minimumScaleFactor(0.72)

                if member.isCurrentUser {
                    TeamCurrentUserBadge()
                }
            }
            .padding(.leading, 10)

            HStack(spacing: 0) {
                RunpamineLottieView(animation: member.animation, isPlaying: isLottieVisible)
                    .frame(width: 80, height: 80)
                    .clipped()
                    .padding(.leading, 10)

                Spacer()
                    .frame(width: 14)

                TeamRunMetricsBlock(member: member)

                Spacer()
                    .frame(width: 10)

                TeamCompletionStamp(isCompleted: member.hasRunRecord)
            }
        }
        .padding(.leading, 16)
        .padding(.trailing, 25)
        .padding(.top, 26)
        .padding(.bottom, 32)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
        .shadow(color: .black.opacity(0.22), radius: 9, x: 0, y: 6)
        .onAppear {
            isLottieVisible = true
        }
        .onDisappear {
            isLottieVisible = false
        }
    }
}

private struct TeamCurrentUserBadge: View {
    var body: some View {
        Text("나")
            .font(AppTheme.Typography.font(size: 10, weight: .black))
            .foregroundStyle(.white)
            .frame(width: 19, height: 20)
            .background(AppTheme.Colors.primary)
            .clipShape(RoundedRectangle(cornerRadius: 5, style: .continuous))
            .accessibilityLabel("내 카드")
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
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct TeamRunMetricRow: View {
    let icon: String
    let label: String
    let value: String

    var body: some View {
        HStack(spacing: 0) {
            Text(label)
                .font(AppTheme.Typography.font(size: 14, weight: .medium))
                .foregroundStyle(Color(red: 0.58, green: 0.64, blue: 0.72))
                .lineLimit(1)
                .frame(width: 48, alignment: .leading)

            Spacer(minLength: 0)

            Text(value)
                .font(AppTheme.Typography.font(size: 14, weight: .bold))
                .foregroundStyle(AppTheme.Colors.textPrimary)
                .lineLimit(1)
                .minimumScaleFactor(0.72)
        }
        .frame(maxWidth: .infinity, minHeight: 20, maxHeight: 20, alignment: .leading)
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

private struct TeamCompletionStamp: View {
    let isCompleted: Bool

    var body: some View {
        ZStack {
            if isCompleted {
                Image("stamp")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 64, height: 64)
                    .accessibilityLabel("러닝 완료")
            }
        }
        .frame(width: 70, height: 80)
    }
}

private struct TeamLeaveConfirmationDialog: View {
    let isLoading: Bool
    let errorMessage: String?
    let onDismiss: () -> Void
    let onConfirm: () -> Void

    var body: some View {
        ZStack {
            Color.black.opacity(0.18)
                .ignoresSafeArea()
                .onTapGesture {
                    guard !isLoading else { return }
                    onDismiss()
                }

            VStack(spacing: 0) {
                Text("팀 탈퇴")
                    .font(AppTheme.Typography.font(size: 24, weight: .bold))
                    .foregroundStyle(AppTheme.Colors.textPrimary)

                Text("정말 팀을 탈퇴하시겠습니까?")
                    .font(AppTheme.Typography.font(size: 18, weight: .medium))
                    .foregroundStyle(AppTheme.Colors.textSecondary)
                    .padding(.top, 22)

                if let errorMessage {
                    Text(errorMessage)
                        .font(AppTheme.Typography.font(size: 13, weight: .medium))
                        .foregroundStyle(AppTheme.Colors.danger)
                        .multilineTextAlignment(.center)
                        .padding(.top, 12)
                }

                HStack(spacing: 12) {
                    dialogButton(title: "취소", color: Color(red: 0.55, green: 0.55, blue: 0.55), action: onDismiss)
                        .disabled(isLoading)

                    dialogButton(
                        title: isLoading ? "탈퇴 중..." : "팀 탈퇴",
                        color: AppTheme.Colors.primary,
                        action: onConfirm
                    )
                    .disabled(isLoading)
                }
                .padding(.top, 34)
            }
            .padding(.horizontal, 28)
            .padding(.vertical, 30)
            .frame(maxWidth: 352)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
            .shadow(color: .black.opacity(0.16), radius: 18, x: 0, y: 8)
            .padding(.horizontal, 24)
        }
        .transition(.opacity)
    }

    private func dialogButton(title: String, color: Color, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(AppTheme.Typography.font(size: 18, weight: .bold))
                .foregroundStyle(.white)
                .lineLimit(1)
                .minimumScaleFactor(0.8)
                .frame(maxWidth: .infinity)
                .frame(height: 52)
                .background(color)
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        }
        .buttonStyle(.plain)
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
        let formatted = kilometers.formatted(.number.precision(.fractionLength(2)))
        return "\(formatted) km"
    }

    static func memberDistanceKilometers(_ distanceMeters: Int) -> String {
        let kilometers = Double(distanceMeters) / 1_000
        return "\(kilometers.formatted(.number.precision(.fractionLength(2)))) km"
    }

    static func totalDistanceKilometers(_ distanceMeters: Int) -> String {
        let kilometers = Double(distanceMeters) / 1_000
        return kilometers.formatted(.number.precision(.fractionLength(2)))
    }
}

private enum TeamMemberStatsDetailFormatter {
    private static let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.timeZone = TimeZone(identifier: "Asia/Seoul")
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()

    private static let displayFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.timeZone = TimeZone(identifier: "Asia/Seoul")
        formatter.dateFormat = "yyyy년 M월 d일"
        return formatter
    }()

    static func joinedDateText(from value: String) -> String {
        guard !value.isEmpty else { return "가입일 정보 없음" }

        let dateText = String(value.prefix(10))
        if let date = dateFormatter.date(from: dateText) {
            return displayFormatter.string(from: date)
        }

        if let date = ISO8601DateFormatter().date(from: value) {
            return displayFormatter.string(from: date)
        }

        return "가입일 정보 없음"
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
        currentUserID: "member-preview",
        cache: TeamDashboardCache(),
        onCreateTeam: {},
        onJoinTeam: {},
        onInvite: {},
        onLeaveTeam: {},
        onSelectMember: { _ in }
    )
}

#Preview("팀원 기록 카드") {
    TeamMemberRunCard(
        member: TeamMemberCardModel(
            id: "member-preview",
            name: "커비",
            animation: .running,
            distanceText: "12.00 km",
            timeText: "10:00",
            paceText: "0'50\"/km",
            hasRunRecord: true,
            isCurrentUser: true,
            detail: TeamMemberStatsDetail(
                id: "member-preview",
                name: "커비",
                joinedAt: "2026-05-01",
                totalDistance: "87.30",
                totalRunCount: "12",
                averagePace: "5'24\""
            )
        )
    )
    .padding(20)
    .frame(width: 390)
    .background(Color(red: 0.96, green: 0.96, blue: 0.96))
}
