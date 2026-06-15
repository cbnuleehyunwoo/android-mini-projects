import SwiftUI

struct TeamDashboardView: View {
    let team: RunningTeam?
    let nickname: String
    let teamService: TeamServiceProtocol
    let accessToken: String?
    let onCreateTeam: () -> Void
    let onJoinTeam: () -> Void
    let onInvite: () -> Void
    let onLeaveTeam: () -> Void
    @State private var records: [RunningRecord] = RunningHistoryStore().load()
    @State private var dailySummary: TeamDailySummary?
    @State private var seasonStats: TeamSeasonStats?
    @State private var hasResolvedDashboard = false
    @State private var selectedDate = Calendar.current.startOfDay(for: Date())
    @State private var isDateLoading = false
    @State private var isShowingTeamMenu = false
    @State private var isShowingLeaveConfirmation = false
    @State private var isLeavingTeam = false
    @State private var leaveTeamErrorMessage: String?

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
            await refreshDashboard()
        }
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
                            isEnabled: !isDateLoading,
                            action: moveToPreviousDate
                        )

                        Text(summaryDateText)
                            .font(AppTheme.Typography.font(size: 18, weight: .medium))
                            .foregroundStyle(.black)
                            .frame(minWidth: 190)

                        dateButton(
                            systemName: "chevron.right",
                            accessibilityLabel: "다음 날짜",
                            isEnabled: canMoveToNextDate && !isDateLoading,
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
            records = RunningHistoryStore().load()
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
        if let dailySummary {
            let seasonMembersByID = seasonStats?.members.reduce(into: [String: TeamSeasonMember]()) { result, member in
                result[member.id] = member
            } ?? [:]

            return dailySummary.members.map { member in
                TeamMemberCardModel(
                    member: member,
                    seasonMember: seasonMembersByID[member.id]
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
                records: records
            ),
            TeamMemberCardModel.emptyBurger(index: 1)
        ]
    }

    private var teamDistanceText: String {
        if let dailySummary {
            return TeamDashboardFormatter.distanceKilometers(dailySummary.teamTotalDistanceMeters)
        }

        if isRemoteDashboard {
            return TeamDashboardFormatter.distanceKilometers(0)
        }

        let totalDistance = records.reduce(0) { $0 + $1.distanceKilometers }
        return "\(totalDistance.formatted(.number.precision(.fractionLength(1)))) km"
    }

    private var completedMemberCount: Int {
        if let dailySummary {
            return dailySummary.completedMemberCount
        }

        return isRemoteDashboard ? 0 : memberCards.filter(\.hasRunRecord).count
    }

    private var totalMemberCount: Int {
        if let dailySummary {
            return dailySummary.totalMemberCount
        }

        return isRemoteDashboard ? team?.memberCount ?? 0 : memberCards.count
    }

    private var displayTeam: RunningTeam? {
        dailySummary?.team ?? seasonStats?.runningTeam ?? team
    }

    private var shouldShowSkeleton: Bool {
        team != nil && accessToken != nil && dailySummary == nil && !hasResolvedDashboard
    }

    private var isRemoteDashboard: Bool {
        team != nil && accessToken != nil
    }

    private var summaryDateText: String {
        TeamDashboardFormatter.dateString(from: selectedDate)
    }

    private var canMoveToNextDate: Bool {
        selectedDate < Calendar.current.startOfDay(for: Date())
    }

    private func moveToPreviousDate() {
        moveDate(by: -1)
    }

    private func moveToNextDate() {
        guard canMoveToNextDate else { return }
        moveDate(by: 1)
    }

    private func moveDate(by value: Int) {
        guard !isDateLoading else { return }
        guard let date = Calendar.current.date(byAdding: .day, value: value, to: selectedDate) else { return }

        selectedDate = Calendar.current.startOfDay(for: date)
        isDateLoading = true
        let requestDate = selectedDate
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
            dailySummary = nil
            seasonStats = nil
            onLeaveTeam()
        } catch {
            leaveTeamErrorMessage = error.localizedDescription
        }
    }

    @MainActor
    private func refreshDashboard() async {
        guard team != nil, let accessToken else { return }
        selectedDate = Calendar.current.startOfDay(for: Date())
        dailySummary = nil
        seasonStats = nil
        hasResolvedDashboard = false

        do {
            async let nextDailySummary = teamService.fetchDailySummary(date: selectedDate, accessToken: accessToken)
            async let nextSeasonStats = teamService.fetchMyTeamSeasonStats(seasonID: nil, accessToken: accessToken)

            dailySummary = try await nextDailySummary
            seasonStats = try? await nextSeasonStats
        } catch {
            dailySummary = nil
            seasonStats = nil
        }

        hasResolvedDashboard = true
    }

    @MainActor
    private func refreshDailySummary(for date: Date) async {
        guard team != nil, let accessToken else {
            isDateLoading = false
            return
        }
        defer { isDateLoading = false }

        do {
            let summary = try await teamService.fetchDailySummary(date: date, accessToken: accessToken)
            guard Calendar.current.isDate(date, inSameDayAs: selectedDate) else { return }
            dailySummary = summary
        } catch {
            guard Calendar.current.isDate(date, inSameDayAs: selectedDate) else { return }
            dailySummary = nil
        }
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
    let hasRunRecord: Bool

    init(
        id: String,
        name: String,
        animation: RunpamineLottieAnimation,
        distanceText: String,
        timeText: String,
        paceText: String,
        hasRunRecord: Bool
    ) {
        self.id = id
        self.name = name
        self.animation = animation
        self.distanceText = distanceText
        self.timeText = timeText
        self.paceText = paceText
        self.hasRunRecord = hasRunRecord
    }

    init(member: TeamDailyMember, seasonMember: TeamSeasonMember?) {
        id = member.id
        name = member.nickname
        animation = .teamMember(consecutiveRunDays: seasonMember?.consecutiveRunDays ?? (member.completed ? 1 : nil))
        distanceText = TeamDashboardFormatter.memberDistanceKilometers(member.distanceMeters)
        timeText = member.durationSeconds > 0 ? RunningMetricFormatter.duration(TimeInterval(member.durationSeconds)) : "--:--"
        paceText = "\(RunningMetricFormatter.pace(member.averagePaceSecondsPerKilometer.map(TimeInterval.init)))/km"
        hasRunRecord = member.completed
    }

    static func runningMember(
        id: String,
        name: String,
        records: [RunningRecord]
    ) -> TeamMemberCardModel {
        let totalDistanceMeters = records.reduce(0) { $0 + $1.distanceMeters }
        let totalElapsedTime = records.reduce(0) { $0 + $1.elapsedTime }
        let totalDistanceKilometers = totalDistanceMeters / 1_000
        let averagePace = totalDistanceKilometers > 0.01 ? totalElapsedTime / totalDistanceKilometers : nil

        return TeamMemberCardModel(
            id: id,
            name: name,
            animation: .teamMember(consecutiveRunDays: TeamRunStreakCalculator.consecutiveRunDays(from: records)),
            distanceText: "\(totalDistanceKilometers.formatted(.number.precision(.fractionLength(1)))) km",
            timeText: totalElapsedTime > 0 ? RunningMetricFormatter.duration(totalElapsedTime) : "--:--",
            paceText: "\(RunningMetricFormatter.pace(averagePace))/km",
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
        onInvite: {},
        onLeaveTeam: {}
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
            hasRunRecord: true
        )
    )
    .padding(20)
    .frame(width: 390)
    .background(Color(red: 0.96, green: 0.96, blue: 0.96))
}
