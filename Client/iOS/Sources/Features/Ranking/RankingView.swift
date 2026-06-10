import SwiftUI

struct RankingView: View {
    @State private var selectedScope: RankingScope = .team
    @State private var selectedMetric: UserRankingMetric = .distance
    @State private var teamBoard: TeamRankingBoard?
    @State private var userBoard: UserRankingBoard?
    @State private var mySummary: MyRankingSummary?
    @State private var isLoading = false
    @State private var errorMessage: String?

    private let rankingService: RankingServiceProtocol
    private let accessToken: String?
    private let team: RunningTeam?
    private let nickname: String

    init(
        rankingService: RankingServiceProtocol = MockRankingService(),
        accessToken: String? = nil,
        team: RunningTeam? = nil,
        nickname: String = "김영희"
    ) {
        self.rankingService = rankingService
        self.accessToken = accessToken
        self.team = team
        self.nickname = nickname
    }

    var body: some View {
        VStack(spacing: 0) {
            topControls

            ScrollView(showsIndicators: false) {
                VStack(spacing: 22) {
                    if selectedScope == .personal {
                        metricControl
                            .padding(.top, 32)
                    }

                    summaryCard
                        .padding(.top, selectedScope == .team ? 32 : 0)

                    Divider()
                        .background(AppTheme.Colors.border)

                    rankingCard
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 26)
            }
        }
        .background(Color.white)
        .task {
            await loadRankings()
        }
        .onChange(of: selectedScope) { _, _ in
            Task { await loadRankings() }
        }
        .onChange(of: selectedMetric) { _, _ in
            Task { await loadRankings() }
        }
    }

    private var topControls: some View {
        HStack(spacing: 0) {
            scopeButton(.team)
            scopeButton(.personal)
        }
        .padding(4)
        .frame(height: RankingLayout.filterOuterHeight)
        .background(Color(red: 0.88, green: 0.88, blue: 0.88))
        .clipShape(Capsule())
        .padding(.horizontal, 58)
        .padding(.top, 18)
    }

    private var metricControl: some View {
        HStack(spacing: 0) {
            metricButton(.distance, title: "KM")
            metricButton(.pace, title: "페이스")
            metricButton(.consistency, title: "스트릭")
        }
        .padding(4)
        .frame(height: RankingLayout.filterOuterHeight)
        .background(Color(red: 0.88, green: 0.88, blue: 0.88))
        .clipShape(Capsule())
    }

    private var summaryCard: some View {
        Group {
            if let summary = currentSummary {
                HStack(spacing: 18) {
                    RankBadge(rank: summary.rank, isHighlighted: true)

                    Text(summary.name)
                        .font(AppTheme.Typography.font(size: 20, weight: .bold))
                        .foregroundStyle(AppTheme.Colors.primary)
                        .lineLimit(1)

                    Spacer(minLength: 12)

                    Text(summary.value)
                        .font(AppTheme.Typography.font(size: 20, weight: .bold))
                        .foregroundStyle(AppTheme.Colors.primary)
                        .lineLimit(1)
                        .minimumScaleFactor(0.78)
                }
                .padding(.horizontal, 24)
                .frame(height: 96)
                .background(Color(red: 0.92, green: 0.96, blue: 1.0))
                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            } else {
                HStack {
                    Text(errorMessage ?? (isLoading ? "랭킹을 불러오는 중입니다." : "랭킹 데이터가 없습니다."))
                        .font(AppTheme.Typography.font(size: 16, weight: .semibold))
                        .foregroundStyle(AppTheme.Colors.textSecondary)
                }
                .frame(maxWidth: .infinity)
                .frame(height: 96)
                .background(Color(red: 0.92, green: 0.96, blue: 1.0))
                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            }
        }
    }

    private var rankingCard: some View {
        VStack(spacing: 16) {
            HStack(alignment: .lastTextBaseline) {
                Text(selectedScope == .team ? "전체 팀 순위" : personalTitle)
                    .font(AppTheme.Typography.font(size: 20, weight: .black))
                    .foregroundStyle(AppTheme.Colors.textPrimary)

                Spacer()

                Text(selectedScope == .team ? "팀 총 거리 기준" : personalSubtitle)
                    .font(AppTheme.Typography.font(size: 14, weight: .medium))
                    .foregroundStyle(Color(red: 0.58, green: 0.63, blue: 0.70))
            }

            if isLoading, rows.isEmpty {
                ProgressView()
                    .frame(maxWidth: .infinity)
                    .frame(height: 180)
            } else if rows.isEmpty {
                Text(errorMessage ?? "아직 표시할 랭킹이 없습니다.")
                    .font(AppTheme.Typography.font(size: 16, weight: .semibold))
                    .foregroundStyle(AppTheme.Colors.textSecondary)
                    .frame(maxWidth: .infinity)
                    .frame(height: 180)
            } else {
                VStack(spacing: 10) {
                    ForEach(rows) { row in
                        RankingRow(row: row)
                    }
                }
            }
        }
        .padding(.horizontal, 22)
        .padding(.top, 26)
        .padding(.bottom, 22)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
        .shadow(color: Color.black.opacity(0.08), radius: 20, x: 0, y: 10)
    }

    private func scopeButton(_ scope: RankingScope) -> some View {
        Button {
            withAnimation(.easeInOut(duration: 0.18)) {
                selectedScope = scope
            }
        } label: {
            Text(scope.title)
                .font(AppTheme.Typography.font(size: 18, weight: .bold))
                .foregroundStyle(selectedScope == scope ? Color.white : Color(red: 0.61, green: 0.66, blue: 0.73))
                .frame(maxWidth: .infinity)
                .frame(height: RankingLayout.filterInnerHeight)
                .background {
                    if selectedScope == scope {
                        Capsule()
                            .fill(AppTheme.Colors.primary)
                    }
                }
        }
        .buttonStyle(.plain)
    }

    private func metricButton(_ metric: UserRankingMetric, title: String) -> some View {
        Button {
            withAnimation(.easeInOut(duration: 0.18)) {
                selectedMetric = metric
            }
        } label: {
            Text(title)
                .font(AppTheme.Typography.font(size: 16, weight: .bold))
                .foregroundStyle(selectedMetric == metric ? Color.white : Color(red: 0.61, green: 0.66, blue: 0.73))
                .frame(maxWidth: .infinity)
                .frame(height: RankingLayout.filterInnerHeight)
                .background {
                    if selectedMetric == metric {
                        Capsule()
                            .fill(AppTheme.Colors.primary)
                    }
                }
        }
        .buttonStyle(.plain)
    }

    private var currentSummary: RankingSummaryRow? {
        switch selectedScope {
        case .team:
            guard let entry = highlightedTeamEntry else { return nil }
            return RankingSummaryRow(
                rank: entry.rank,
                name: entry.teamName,
                value: "\(formatDistance(entry.distanceMeters, spaced: true)) (\(formatTopPercent(entry.topPercent)))"
            )
        case .personal:
            guard let summary = mySummary else { return highlightedUserEntry.map(summaryRow) }
            guard let rank = selectedMetric.rank(from: summary) ?? highlightedUserEntry?.rank else { return nil }
            let topPercent = selectedMetric.topPercent(from: summary) ?? highlightedUserEntry?.topPercent
            return RankingSummaryRow(
                rank: rank,
                name: nickname,
                value: "\(selectedMetric.summaryValue(from: summary)) (\(topPercent.map(formatTopPercent) ?? "랭킹 대기"))"
            )
        }
    }

    private var highlightedTeamEntry: TeamRankingEntry? {
        guard let board = teamBoard else { return nil }

        if let team, let entry = board.rankings.first(where: { $0.teamID == team.id.uuidString || $0.teamName == team.name }) {
            return entry
        }

        return board.rankings.first(where: { $0.rank == 2 }) ?? board.rankings.first
    }

    private var highlightedUserEntry: UserRankingEntry? {
        guard let board = userBoard else { return nil }
        let summaryRank = mySummary.flatMap { selectedMetric.rank(from: $0) }
        return board.rankings.first(where: { $0.nickname == nickname })
            ?? board.rankings.first(where: { $0.rank == summaryRank })
            ?? board.rankings.first(where: { $0.rank == 2 })
            ?? board.rankings.first
    }

    private var rows: [RankingListRow] {
        switch selectedScope {
        case .team:
            let highlightedID = highlightedTeamEntry?.teamID
            return (teamBoard?.rankings ?? []).map { entry in
                RankingListRow(
                    id: entry.teamID,
                    rank: entry.rank,
                    name: entry.teamName,
                    value: formatDistance(entry.distanceMeters, spaced: false),
                    isHighlighted: entry.teamID == highlightedID
                )
            }
        case .personal:
            let highlightedID = highlightedUserEntry?.userID
            return (userBoard?.rankings ?? []).map { entry in
                RankingListRow(
                    id: entry.userID,
                    rank: entry.rank,
                    name: entry.nickname,
                    value: selectedMetric.value(from: entry),
                    isHighlighted: entry.userID == highlightedID
                )
            }
        }
    }

    private var personalTitle: String {
        switch selectedMetric {
        case .distance:
            return "개인 거리 순위"
        case .pace:
            return "개인 페이스 순위"
        case .consistency:
            return "개인 스트릭 순위"
        }
    }

    private var personalSubtitle: String {
        switch selectedMetric {
        case .distance:
            return "누적 거리 기준"
        case .pace:
            return "평균 페이스 기준"
        case .consistency:
            return "활동일 기준"
        }
    }

    @MainActor
    private func loadRankings() async {
        if accessToken == nil, !(rankingService is MockRankingService) {
            errorMessage = "로그인이 필요합니다."
            isLoading = false
            return
        }

        isLoading = true
        errorMessage = nil

        do {
            let token = accessToken ?? ""
            async let summary = rankingService.fetchMyRankingSummary(seasonID: nil, accessToken: token)

            switch selectedScope {
            case .team:
                async let teamRanking = rankingService.fetchTeamRankings(seasonID: nil, accessToken: token)
                teamBoard = try await teamRanking
            case .personal:
                async let userRanking = rankingService.fetchUserRankings(metric: selectedMetric, seasonID: nil, accessToken: token)
                userBoard = try await userRanking
            }

            mySummary = try await summary
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    private func summaryRow(from entry: UserRankingEntry) -> RankingSummaryRow {
        RankingSummaryRow(
            rank: entry.rank,
            name: entry.nickname,
            value: "\(selectedMetric.value(from: entry)) (\(formatTopPercent(entry.topPercent)))"
        )
    }

    private func formatDistance(_ meters: Int, spaced: Bool) -> String {
        let value = Double(meters) / 1_000
        return String(format: spaced ? "%.1f km" : "%.1fkm", value)
    }

    private func formatTopPercent(_ percent: Int) -> String {
        "상위 \(max(0, percent))%"
    }
}

private enum RankingLayout {
    static let filterOuterHeight: CGFloat = 42
    static let filterInnerHeight: CGFloat = 34
}

private enum RankingScope {
    case team
    case personal

    var title: String {
        switch self {
        case .team:
            return "팀 랭킹"
        case .personal:
            return "개인 랭킹"
        }
    }
}

private struct RankingSummaryRow {
    let rank: Int
    let name: String
    let value: String
}

private struct RankingListRow: Identifiable {
    let id: String
    let rank: Int
    let name: String
    let value: String
    let isHighlighted: Bool
}

private struct RankingRow: View {
    let row: RankingListRow

    var body: some View {
        HStack(spacing: 16) {
            RankBadge(rank: row.rank, isHighlighted: row.isHighlighted)

            Text(row.name)
                .font(AppTheme.Typography.font(size: 18, weight: .bold))
                .foregroundStyle(row.isHighlighted ? AppTheme.Colors.primary : Color(red: 0.22, green: 0.27, blue: 0.36))
                .lineLimit(1)

            Spacer(minLength: 12)

            Text(row.value)
                .font(AppTheme.Typography.font(size: 18, weight: .bold))
                .foregroundStyle(row.isHighlighted ? AppTheme.Colors.primary : Color(red: 0.60, green: 0.65, blue: 0.72))
                .lineLimit(1)
                .minimumScaleFactor(0.78)
        }
        .padding(.horizontal, 18)
        .frame(height: 64)
        .background(row.isHighlighted ? Color(red: 0.92, green: 0.96, blue: 1.0) : Color(red: 0.98, green: 0.98, blue: 0.99))
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }
}

private struct RankBadge: View {
    let rank: Int
    let isHighlighted: Bool

    var body: some View {
        Text("\(rank)")
            .font(AppTheme.Typography.font(size: 20, weight: .black))
            .foregroundStyle(Color.white)
            .frame(width: 44, height: 44)
            .background(isHighlighted ? AppTheme.Colors.primary : Color(red: 0.62, green: 0.67, blue: 0.74))
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
    }
}

private extension UserRankingMetric {
    func value(from entry: UserRankingEntry) -> String {
        switch self {
        case .distance:
            return String(format: "%.1f km", Double(entry.distanceMeters) / 1_000)
        case .pace:
            return "\(formatPace(entry.averagePaceSecondsPerKilometer))/km"
        case .consistency:
            return "\(entry.activeDays)일"
        }
    }

    func summaryValue(from summary: MyRankingSummary) -> String {
        switch self {
        case .distance:
            return String(format: "%.1f km", Double(summary.distanceMeters) / 1_000)
        case .pace:
            return "\(formatPace(summary.averagePaceSecondsPerKilometer))/km"
        case .consistency:
            return "\(summary.activeDays)일"
        }
    }

    func rank(from summary: MyRankingSummary) -> Int? {
        switch self {
        case .distance:
            return summary.distanceRank
        case .pace:
            return summary.paceRank
        case .consistency:
            return summary.consistencyRank
        }
    }

    func topPercent(from summary: MyRankingSummary) -> Int? {
        switch self {
        case .distance:
            return summary.distanceTopPercent
        case .pace:
            return summary.paceTopPercent
        case .consistency:
            return summary.consistencyTopPercent
        }
    }

    private func formatPace(_ seconds: Int?) -> String {
        guard let seconds else { return "--'--\"" }
        return String(format: "%d'%02d\"", max(0, seconds) / 60, max(0, seconds) % 60)
    }
}

#Preview {
    RankingView()
}
