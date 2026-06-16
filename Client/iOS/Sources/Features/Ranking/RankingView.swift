import SwiftUI

final class RankingCache: ObservableObject {
    @Published var selectedScope: RankingScope = .team
    @Published var selectedMetric: UserRankingMetric = .distance
    @Published var teamBoardsByMetric: [UserRankingMetric: TeamRankingBoard] = [:]
    @Published var userBoardsByMetric: [UserRankingMetric: UserRankingBoard] = [:]
    @Published var mySummary: MyRankingSummary?
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var activeRankingRequestID: UUID?
}

struct RankingView: View {
    @ObservedObject var cache: RankingCache

    private let rankingService: RankingServiceProtocol
    private let accessToken: String?
    private let team: RunningTeam?
    private let nickname: String

    init(
        rankingService: RankingServiceProtocol = MockRankingService(),
        accessToken: String? = nil,
        team: RunningTeam? = nil,
        nickname: String = "김영희",
        cache: RankingCache = RankingCache()
    ) {
        self.rankingService = rankingService
        self.accessToken = accessToken
        self.team = team
        self.nickname = nickname
        self.cache = cache
    }

    var body: some View {
        VStack(spacing: 0) {
            topControls
                .padding(.top, 18)

            metricControl
                .padding(.horizontal, 22)
                .padding(.top, 15)

            summaryCard
                .padding(.horizontal, 22)
                .padding(.top, 28)

            Divider()
                .background(AppTheme.Colors.border)
                .padding(.top, 24)

            ScrollView(showsIndicators: false) {
                VStack(spacing: 0) {
                    rankingCard
                }
                .padding(.top, 24)
                .padding(.bottom, 26)
            }
        }
        .background(Color.white)
        .task {
            await loadRankings()
        }
        .onChange(of: cache.selectedScope) { _, _ in
            Task { await loadRankings() }
        }
        .onChange(of: cache.selectedMetric) { _, _ in
            Task { await loadRankings() }
        }
    }

    private var topControls: some View {
        HStack(spacing: 0) {
            scopeButton(.team)
            scopeButton(.personal)
        }
        .frame(height: 72)
    }

    private var metricControl: some View {
        HStack(spacing: 10) {
            metricButton(.distance, title: "전체 거리")
            metricButton(.pace, title: "페이스")
            metricButton(.consistency, title: cache.selectedScope == .team ? "평균 활동일" : "횟수")
        }
    }

    private var summaryCard: some View {
        Group {
            if cache.isLoading, currentSummary == nil {
                RankingSummarySkeletonCard()
            } else if let summary = currentSummary {
                HStack(spacing: 18) {
                    RankBadge(rank: summary.rank, isHighlighted: true)

                    Text(summary.name)
                        .font(AppTheme.Typography.font(size: 14, weight: .bold))
                        .foregroundStyle(AppTheme.Colors.primary)
                        .lineLimit(1)

                    Spacer(minLength: 12)

                    Text(summary.value)
                        .font(AppTheme.Typography.font(size: 14, weight: .bold))
                        .foregroundStyle(AppTheme.Colors.primary)
                        .lineLimit(1)
                        .minimumScaleFactor(0.78)
                }
                .padding(.horizontal, 12)
                .frame(height: 65)
                .background(Color(red: 0.92, green: 0.96, blue: 1.0))
                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            } else {
                HStack {
                    Text(cache.errorMessage ?? (cache.isLoading ? "랭킹을 불러오는 중입니다." : "랭킹 데이터가 없습니다."))
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
                Text(cache.selectedScope == .team ? "전체 팀 순위" : "전체 개인 순위")
                    .font(AppTheme.Typography.font(size: 20, weight: .bold))
                    .foregroundStyle(AppTheme.Colors.textPrimary)

                Spacer()

                Text(cache.selectedScope == .team ? teamSubtitle : personalSubtitle)
                    .font(AppTheme.Typography.font(size: 12, weight: .regular))
                    .foregroundStyle(Color(red: 0.58, green: 0.63, blue: 0.70))
            }

            if cache.isLoading, rows.isEmpty {
                RankingRowsSkeleton()
            } else if rows.isEmpty {
                Text(cache.errorMessage ?? "아직 표시할 랭킹이 없습니다.")
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
        .padding(.bottom, 22)
    }

    private func scopeButton(_ scope: RankingScope) -> some View {
        Button {
            withAnimation(.easeInOut(duration: 0.18)) {
                cache.selectedScope = scope
            }
        } label: {
            ZStack(alignment: .bottom) {
                Text(scope.title)
                    .font(AppTheme.Typography.font(size: 15, weight: .bold))
                    .foregroundStyle(cache.selectedScope == scope ? AppTheme.Colors.primary : Color(red: 0.62, green: 0.67, blue: 0.74))
                    .frame(maxWidth: .infinity)
                    .frame(height: 45)

                if cache.selectedScope == scope {
                    Rectangle()
                        .fill(AppTheme.Colors.primary)
                        .frame(height: 4)
                }
            }
        }
        .buttonStyle(.plain)
    }

    private func metricButton(_ metric: UserRankingMetric, title: String) -> some View {
        Button {
            withAnimation(.easeInOut(duration: 0.18)) {
                cache.selectedMetric = metric
            }
        } label: {
            Text(title)
                .font(AppTheme.Typography.font(size: 13, weight: .bold))
                .foregroundStyle(cache.selectedMetric == metric ? AppTheme.Colors.primary : Color(red: 0.42, green: 0.45, blue: 0.50))
                .lineLimit(1)
                .minimumScaleFactor(0.82)
                .frame(maxWidth: .infinity)
                .frame(height: 38)
                .background(cache.selectedMetric == metric ? Color(red: 0.93, green: 0.95, blue: 1.0) : Color(red: 0.95, green: 0.95, blue: 0.96))
                .clipShape(Capsule())
                .overlay(
                    Capsule()
                        .stroke(cache.selectedMetric == metric ? AppTheme.Colors.primary : Color.clear, lineWidth: 2)
                )
        }
        .buttonStyle(.plain)
    }

    private var currentSummary: RankingSummaryRow? {
        switch cache.selectedScope {
        case .team:
            guard let team else {
                return RankingSummaryRow(rank: nil, name: "팀 없음", value: "랭킹 집계 전")
            }
            guard let entry = highlightedTeamEntry else {
                return RankingSummaryRow(rank: nil, name: team.name, value: "랭킹 집계 전")
            }
            return RankingSummaryRow(
                rank: entry.rank,
                name: entry.teamName,
                value: "\(cache.selectedMetric.value(from: entry)) (\(formatTopPercent(entry.topPercent)))"
            )
        case .personal:
            guard let summary = cache.mySummary else { return highlightedUserEntry.map(summaryRow) }
            guard let rank = cache.selectedMetric.rank(from: summary) else {
                return RankingSummaryRow(rank: nil, name: nickname, value: "-")
            }
            let topPercent = cache.selectedMetric.topPercent(from: summary)
            return RankingSummaryRow(
                rank: rank,
                name: nickname,
                value: "\(cache.selectedMetric.summaryValue(from: summary)) (\(topPercent.map(formatTopPercent) ?? "랭킹 대기"))"
            )
        }
    }

    private var highlightedTeamEntry: TeamRankingEntry? {
        guard let board = currentTeamBoard else { return nil }

        if let team, let entry = board.rankings.first(where: { $0.teamID == team.id.uuidString || $0.teamName == team.name }) {
            return entry
        }

        return nil
    }

    private var highlightedUserEntry: UserRankingEntry? {
        guard let board = currentUserBoard else { return nil }
        let summaryRank = cache.mySummary.flatMap { cache.selectedMetric.rank(from: $0) }
        if let entry = board.rankings.first(where: { $0.nickname == nickname }) {
            return entry
        }

        if let summaryRank {
            return board.rankings.first(where: { $0.rank == summaryRank })
        }

        return nil
    }

    private var rows: [RankingListRow] {
        switch cache.selectedScope {
        case .team:
            let highlightedID = highlightedTeamEntry?.teamID
            return (currentTeamBoard?.rankings ?? []).map { entry in
                RankingListRow(
                    id: entry.teamID,
                    rank: entry.rank,
                    name: entry.teamName,
                    value: cache.selectedMetric.value(from: entry),
                    isHighlighted: entry.teamID == highlightedID
                )
            }
        case .personal:
            let highlightedID = highlightedUserEntry?.userID
            return (currentUserBoard?.rankings ?? []).map { entry in
                RankingListRow(
                    id: entry.userID,
                    rank: entry.rank,
                    name: entry.nickname,
                    value: cache.selectedMetric.value(from: entry),
                    isHighlighted: entry.userID == highlightedID
                )
            }
        }
    }

    private var currentUserBoard: UserRankingBoard? {
        cache.userBoardsByMetric[cache.selectedMetric]
    }

    private var currentTeamBoard: TeamRankingBoard? {
        cache.teamBoardsByMetric[cache.selectedMetric]
    }

    private var teamSubtitle: String {
        switch cache.selectedMetric {
        case .distance:
            return "팀 총 거리 기준"
        case .pace:
            return "팀 평균 페이스 기준"
        case .consistency:
            return "평균 활동일 기준"
        }
    }

    private var personalSubtitle: String {
        switch cache.selectedMetric {
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
            cache.errorMessage = "로그인이 필요합니다."
            cache.isLoading = false
            return
        }

        let requestID = UUID()
        let requestScope = cache.selectedScope
        let requestMetric = cache.selectedMetric
        cache.activeRankingRequestID = requestID
        cache.isLoading = true
        cache.errorMessage = nil

        do {
            let token = accessToken ?? ""
            async let summary = rankingService.fetchMyRankingSummary(seasonID: nil, accessToken: token)

            switch requestScope {
            case .team:
                async let teamRanking = rankingService.fetchTeamRankings(metric: requestMetric, seasonID: nil, accessToken: token)
                let nextTeamBoard = try await teamRanking
                let nextSummary = try await summary
                guard shouldApplyRankingResponse(requestID: requestID, scope: requestScope, metric: requestMetric) else { return }
                if cache.teamBoardsByMetric[requestMetric] != nextTeamBoard {
                    cache.teamBoardsByMetric[requestMetric] = nextTeamBoard
                }
                if cache.mySummary != nextSummary {
                    cache.mySummary = nextSummary
                }
            case .personal:
                async let userRanking = rankingService.fetchUserRankings(metric: requestMetric, seasonID: nil, accessToken: token)
                let nextUserBoard = try await userRanking
                let nextSummary = try await summary
                guard shouldApplyRankingResponse(requestID: requestID, scope: requestScope, metric: requestMetric) else { return }
                if cache.userBoardsByMetric[requestMetric] != nextUserBoard {
                    cache.userBoardsByMetric[requestMetric] = nextUserBoard
                }
                if cache.mySummary != nextSummary {
                    cache.mySummary = nextSummary
                }
            }
        } catch {
            guard shouldApplyRankingResponse(requestID: requestID, scope: requestScope, metric: requestMetric) else { return }
            cache.errorMessage = error.localizedDescription
        }

        guard shouldApplyRankingResponse(requestID: requestID, scope: requestScope, metric: requestMetric) else { return }
        cache.isLoading = false
    }

    private func shouldApplyRankingResponse(
        requestID: UUID,
        scope: RankingScope,
        metric: UserRankingMetric
    ) -> Bool {
        cache.activeRankingRequestID == requestID && cache.selectedScope == scope && cache.selectedMetric == metric
    }

    private func summaryRow(from entry: UserRankingEntry) -> RankingSummaryRow {
        RankingSummaryRow(
            rank: entry.rank,
            name: entry.nickname,
            value: "\(cache.selectedMetric.value(from: entry)) (\(formatTopPercent(entry.topPercent)))"
        )
    }

    private func formatTopPercent(_ percent: Int) -> String {
        "상위 \(max(0, percent))%"
    }
}

enum RankingScope {
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
    let rank: Int?
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
                .font(AppTheme.Typography.font(size: 14, weight: .bold))
                .foregroundStyle(row.isHighlighted ? AppTheme.Colors.primary : Color(red: 0.22, green: 0.27, blue: 0.36))
                .lineLimit(1)

            Spacer(minLength: 12)

            Text(row.value)
                .font(AppTheme.Typography.font(size: 14, weight: .semibold))
                .foregroundStyle(row.isHighlighted ? AppTheme.Colors.primary : Color(red: 0.60, green: 0.65, blue: 0.72))
                .lineLimit(1)
                .minimumScaleFactor(0.78)
        }
        .padding(.horizontal, 18)
        .frame(height: 52)
        .background(row.isHighlighted ? Color(red: 0.92, green: 0.96, blue: 1.0) : Color(red: 0.98, green: 0.98, blue: 0.99))
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }
}

private struct RankingSummarySkeletonCard: View {
    var body: some View {
        HStack(spacing: 18) {
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .fill(RankingSkeletonStyle.fill)
                .frame(width: 28, height: 28)

            RoundedRectangle(cornerRadius: 6, style: .continuous)
                .fill(RankingSkeletonStyle.fill)
                .frame(width: 82, height: 14)

            Spacer(minLength: 12)

            RoundedRectangle(cornerRadius: 6, style: .continuous)
                .fill(RankingSkeletonStyle.fill)
                .frame(width: 108, height: 14)
        }
        .padding(.horizontal, 12)
        .frame(height: 65)
        .background(Color(red: 0.92, green: 0.96, blue: 1.0))
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        .allowsHitTesting(false)
    }
}

private struct RankingRowsSkeleton: View {
    var body: some View {
        VStack(spacing: 10) {
            ForEach(0..<6, id: \.self) { index in
                RankingRowSkeleton(isHighlighted: index == 1)
            }
        }
        .allowsHitTesting(false)
    }
}

private struct RankingRowSkeleton: View {
    let isHighlighted: Bool

    var body: some View {
        HStack(spacing: 16) {
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .fill(RankingSkeletonStyle.badgeFill)
                .frame(width: 28, height: 28)

            RoundedRectangle(cornerRadius: 6, style: .continuous)
                .fill(RankingSkeletonStyle.fill)
                .frame(width: nameWidth, height: 14)

            Spacer(minLength: 12)

            RoundedRectangle(cornerRadius: 6, style: .continuous)
                .fill(RankingSkeletonStyle.fill)
                .frame(width: valueWidth, height: 14)
        }
        .padding(.horizontal, 18)
        .frame(height: 52)
        .background(isHighlighted ? Color(red: 0.92, green: 0.96, blue: 1.0) : Color(red: 0.98, green: 0.98, blue: 0.99))
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }

    private var nameWidth: CGFloat {
        isHighlighted ? 82 : 68
    }

    private var valueWidth: CGFloat {
        isHighlighted ? 92 : 76
    }
}

private enum RankingSkeletonStyle {
    static let fill = Color(red: 0.86, green: 0.89, blue: 0.94)
    static let badgeFill = Color(red: 0.72, green: 0.77, blue: 0.84)
}

private struct RankBadge: View {
    let rank: Int?
    let isHighlighted: Bool

    var body: some View {
        Text(rank.map(String.init) ?? "-")
            .font(AppTheme.Typography.font(size: 14, weight: .black))
            .foregroundStyle(Color.white)
            .frame(width: 28, height: 28)
            .background(isHighlighted ? AppTheme.Colors.primary : Color(red: 0.62, green: 0.67, blue: 0.74))
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
    }
}

private extension UserRankingMetric {
    func value(from entry: TeamRankingEntry) -> String {
        switch self {
        case .distance:
            return String(format: "%.2f km", Double(entry.distanceMeters) / 1_000)
        case .pace:
            return "\(formatPace(entry.averagePaceSecondsPerKilometer))/km"
        case .consistency:
            return formatActiveDays(entry.averageActiveDays)
        }
    }

    func value(from entry: UserRankingEntry) -> String {
        switch self {
        case .distance:
            return String(format: "%.2f km", Double(entry.distanceMeters) / 1_000)
        case .pace:
            return "\(formatPace(entry.averagePaceSecondsPerKilometer))/km"
        case .consistency:
            return "\(entry.activeDays)일"
        }
    }

    func summaryValue(from summary: MyRankingSummary) -> String {
        switch self {
        case .distance:
            return String(format: "%.2f km", Double(summary.distanceMeters) / 1_000)
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

    private func formatActiveDays(_ days: Double) -> String {
        if days.rounded() == days {
            return "\(Int(days))일"
        }

        return String(format: "%.1f일", days)
    }
}

#Preview {
    RankingView()
}
