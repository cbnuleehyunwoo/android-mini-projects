import MapKit
import SwiftUI

final class HistoryCache: ObservableObject {
    @Published var selectedPeriod: HistoryPeriod = .week
    @Published var displayedWeek = Date()
    @Published var displayedMonth = Date()
    @Published var records: [RunningRecord] = RunningHistoryStore().load()
    @Published var daySummaries: [RunDaySummary] = []
    @Published var totalDistanceMeters: Int?
    @Published var summariesByIdentifier: [String: RunPeriodSummary] = [:]
    @Published var loadedRefreshIdentifier: String?
    @Published var thumbnailRecordsByID: [UUID: RunningRecord] = [:]
    @Published var thumbnailFetchIDs: Set<UUID> = []
}

struct HistoryView: View {
    @ObservedObject var cache: HistoryCache
    @State private var selectedRecord: RunningRecord?

    private let runService: RunServiceProtocol
    private let historyStore: RunningHistoryStore
    private let accessToken: String?
    private let currentUserID: String?
    private let refreshRevision: Int
    private let onRetryPendingRuns: () async -> Void
    private var calendar: Calendar {
        var calendar = Calendar.current
        calendar.firstWeekday = 2
        return calendar
    }

    init(
        runService: RunServiceProtocol = MockRunService(),
        historyStore: RunningHistoryStore = RunningHistoryStore(),
        accessToken: String? = nil,
        currentUserID: String? = nil,
        cache: HistoryCache = HistoryCache(),
        refreshRevision: Int = 0,
        onRetryPendingRuns: @escaping () async -> Void = {}
    ) {
        self.runService = runService
        self.historyStore = historyStore
        self.accessToken = accessToken
        self.currentUserID = currentUserID
        self.cache = cache
        self.refreshRevision = refreshRevision
        self.onRetryPendingRuns = onRetryPendingRuns
    }

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(alignment: .leading, spacing: 0) {
                header

                HStack(alignment: .lastTextBaseline, spacing: 8) {
                    Text(totalDistanceText)
                        .font(AppTheme.Typography.font(size: 55, weight: .bold))
                        .foregroundStyle(AppTheme.Colors.primary)
                    Text("KM")
                        .font(AppTheme.Typography.font(size: 18, weight: .medium))
                        .foregroundStyle(Color.gray)
                }
                .padding(.horizontal, 30)
                .padding(.top, 20)

                if cache.selectedPeriod == .week {
                    WeekDotsView(
                        week: cache.displayedWeek,
                        previousSummary: weeklySummary(for: previousWeek),
                        currentSummary: weeklySummary(for: cache.displayedWeek),
                        nextSummary: weeklySummary(for: nextWeek),
                        canMoveToNextWeek: canMoveToNextWeek,
                        onPreviousWeek: moveToPreviousWeek,
                        onNextWeek: moveToNextWeek
                    )
                        .padding(.horizontal, 30)
                        .padding(.top, 28)
                } else {
                    MonthCalendarView(
                        month: cache.displayedMonth,
                        records: visibleRecords,
                        daySummaries: visibleDaySummaries,
                        canMoveToNextMonth: canMoveToNextMonth,
                        onPreviousMonth: moveToPreviousMonth,
                        onNextMonth: moveToNextMonth
                    )
                        .padding(.horizontal, 30)
                        .padding(.top, 24)
                }

                VStack(spacing: 22) {
                    if isWaitingForRemoteRecords {
                        loadingState
                    } else if visibleSelectedRecords.isEmpty {
                        emptyState
                    } else {
                        ForEach(visibleSelectedRecords) { record in
                            Button {
                                Task {
                                    await openRecord(record)
                                }
                            } label: {
                                RunningRecordCard(
                                    record: record,
                                    thumbnailRecord: cache.thumbnailRecordsByID[record.id] ?? record
                                )
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
                .padding(.horizontal, 30)
                .padding(.top, 28)
                .padding(.bottom, 92)
            }
        }
        .background(Color.white)
        .onAppear {
            if accessToken == nil {
                cache.records = historyStore.load()
            }
            Task {
                await onRetryPendingRuns()
            }
        }
        .task(id: recordRefreshTaskIdentifier) {
            await refreshRecords()
        }
        .task(id: thumbnailRefreshIdentifier) {
            await refreshThumbnailRecords()
        }
        .runpamineFullScreenCover(item: $selectedRecord) { record in
            RunningSummaryView(record: record) {
                selectedRecord = nil
            }
            .networkErrorOverlay()
        }
    }

    private var header: some View {
        HStack {
            Text("기록")
                .font(AppTheme.Typography.header2)
                .foregroundStyle(AppTheme.Colors.primary)

            Spacer()

            HistoryPeriodControl(
                selectedPeriod: Binding(
                    get: { cache.selectedPeriod },
                    set: { cache.selectedPeriod = $0 }
                )
            )
                .frame(width: 150)
        }
        .padding(.horizontal, 30)
        .padding(.top, 34)
    }

    private var visibleSelectedRecords: [RunningRecord] {
        records(in: visibleRecords)
    }

    private var visibleRecords: [RunningRecord] {
        guard accessToken != nil else { return cache.records }
        let remoteRecords = if cache.loadedRefreshIdentifier == refreshIdentifier {
            cache.records
        } else {
            cache.summariesByIdentifier[refreshIdentifier]?.runs ?? []
        }
        return mergePendingRecords(into: remoteRecords)
    }

    private var visibleDaySummaries: [RunDaySummary] {
        guard accessToken != nil else { return cache.daySummaries }
        let remoteDays = if cache.loadedRefreshIdentifier == refreshIdentifier {
            cache.daySummaries
        } else {
            cache.summariesByIdentifier[refreshIdentifier]?.days ?? []
        }
        return mergePendingDays(into: remoteDays)
    }

    private var isWaitingForRemoteRecords: Bool {
        accessToken != nil &&
        cache.loadedRefreshIdentifier != refreshIdentifier &&
        cache.summariesByIdentifier[refreshIdentifier] == nil &&
        pendingRecordsForSelectedPeriod.isEmpty
    }

    private func records(in records: [RunningRecord]) -> [RunningRecord] {
        records.filter { record in
            switch cache.selectedPeriod {
            case .week:
                return calendar.isDate(record.startedAt, equalTo: cache.displayedWeek, toGranularity: .weekOfYear)
            case .month:
                return calendar.isDate(record.startedAt, equalTo: cache.displayedMonth, toGranularity: .month)
            }
        }
    }

    private var totalDistanceText: String {
        guard !isWaitingForRemoteRecords else { return "0.00" }

        if cache.loadedRefreshIdentifier == refreshIdentifier {
            let totalMeters: Double
            if let remoteTotalDistanceMeters = cache.totalDistanceMeters {
                let pendingDistanceMeters = pendingRecordsForSelectedPeriod.reduce(0) { $0 + $1.distanceMeters }
                totalMeters = Double(remoteTotalDistanceMeters) + pendingDistanceMeters
            } else {
                totalMeters = visibleSelectedRecords.reduce(0) { $0 + $1.distanceMeters }
            }
            let total = totalMeters / 1_000
            return total.formatted(.number.precision(.fractionLength(2)))
        }

        if let summary = cache.summariesByIdentifier[refreshIdentifier] {
            let pendingDistanceMeters = pendingRecordsForSelectedPeriod.reduce(0) { $0 + $1.distanceMeters }
            return ((Double(summary.totalDistanceMeters) + pendingDistanceMeters) / 1_000)
                .formatted(.number.precision(.fractionLength(2)))
        }

        return "0.00"
    }

    private var refreshIdentifier: String {
        switch cache.selectedPeriod {
        case .week:
            let weekStart = calendar.dateInterval(of: .weekOfYear, for: cache.displayedWeek)?.start ?? cache.displayedWeek
            return "\(cache.selectedPeriod.rawValue)-\(Int(weekStart.timeIntervalSince1970))"
        case .month:
            let components = calendar.dateComponents([.year, .month], from: cache.displayedMonth)
            return "\(cache.selectedPeriod.rawValue)-\(components.year ?? 0)-\(components.month ?? 0)"
        }
    }

    private var recordRefreshTaskIdentifier: String {
        "\(refreshIdentifier)|\(refreshRevision)"
    }

    private var thumbnailRefreshIdentifier: String {
        visibleSelectedRecords
            .map(\.id.uuidString)
            .joined(separator: "|")
    }

    private var emptyState: some View {
        VStack(spacing: 12) {
            Image(systemName: "figure.run")
                .font(.system(size: 34, weight: .semibold))
                .foregroundStyle(Color.gray)
            Text(cache.selectedPeriod == .week ? "이번 주 러닝 기록이 없어요" : "이번 달 러닝 기록이 없어요")
                .font(AppTheme.Typography.font(size: 15, weight: .bold))
                .foregroundStyle(AppTheme.Colors.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 160)
    }

    private var loadingState: some View {
        VStack(spacing: 12) {
            ProgressView()
                .tint(AppTheme.Colors.primary)
            Text("러닝 기록을 불러오는 중이에요")
                .font(AppTheme.Typography.font(size: 15, weight: .bold))
                .foregroundStyle(AppTheme.Colors.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 160)
    }

    private func moveToPreviousMonth() {
        cache.displayedMonth = calendar.date(byAdding: .month, value: -1, to: cache.displayedMonth) ?? cache.displayedMonth
    }

    private func moveToNextMonth() {
        guard canMoveToNextMonth else { return }
        cache.displayedMonth = calendar.date(byAdding: .month, value: 1, to: cache.displayedMonth) ?? cache.displayedMonth
    }

    private var canMoveToNextMonth: Bool {
        let displayedMonthStart = calendar.dateInterval(of: .month, for: cache.displayedMonth)?.start
        let currentMonthStart = calendar.dateInterval(of: .month, for: Date())?.start
        guard let displayedMonthStart, let currentMonthStart else { return false }
        return displayedMonthStart < currentMonthStart
    }

    private var canMoveToNextWeek: Bool {
        let displayedWeekStart = calendar.dateInterval(of: .weekOfYear, for: cache.displayedWeek)?.start
        let currentWeekStart = calendar.dateInterval(of: .weekOfYear, for: Date())?.start
        guard let displayedWeekStart, let currentWeekStart else { return false }
        return displayedWeekStart < currentWeekStart
    }

    private func moveToPreviousWeek() {
        cache.displayedWeek = calendar.date(byAdding: .weekOfYear, value: -1, to: cache.displayedWeek) ?? cache.displayedWeek
    }

    private func moveToNextWeek() {
        guard canMoveToNextWeek else { return }
        cache.displayedWeek = calendar.date(byAdding: .weekOfYear, value: 1, to: cache.displayedWeek) ?? cache.displayedWeek
    }

    private var previousWeek: Date {
        calendar.date(byAdding: .weekOfYear, value: -1, to: cache.displayedWeek) ?? cache.displayedWeek
    }

    private var nextWeek: Date {
        calendar.date(byAdding: .weekOfYear, value: 1, to: cache.displayedWeek) ?? cache.displayedWeek
    }

    private func weekRefreshIdentifier(for date: Date) -> String {
        let start = calendar.dateInterval(of: .weekOfYear, for: date)?.start ?? date
        return "\(HistoryPeriod.week.rawValue)-\(Int(start.timeIntervalSince1970))"
    }

    private func weeklySummary(for date: Date) -> RunPeriodSummary {
        if let summary = cache.summariesByIdentifier[weekRefreshIdentifier(for: date)] {
            return summary
        }

        if accessToken == nil {
            let weeklyRecords = cache.records.filter {
                calendar.isDate($0.startedAt, equalTo: date, toGranularity: .weekOfYear)
            }
            return RunPeriodSummary(
                totalDistanceMeters: Int(weeklyRecords.reduce(0) { $0 + $1.distanceMeters }.rounded()),
                days: [],
                runs: weeklyRecords
            )
        }

        guard calendar.isDate(date, equalTo: cache.displayedWeek, toGranularity: .weekOfYear) else {
            return RunPeriodSummary(totalDistanceMeters: 0, days: [], runs: [])
        }

        return RunPeriodSummary(
            totalDistanceMeters: cache.totalDistanceMeters ?? 0,
            days: visibleDaySummaries,
            runs: visibleRecords
        )
    }

    @MainActor
    private func refreshRecords() async {
        guard let accessToken else {
            useLocalRecords()
            return
        }

        let requestIdentifier = refreshIdentifier

        do {
            let summary: RunPeriodSummary
            switch cache.selectedPeriod {
            case .week:
                summary = try await runService.fetchWeeklyRuns(anchorDate: cache.displayedWeek, accessToken: accessToken)
            case .month:
                let components = calendar.dateComponents([.year, .month], from: cache.displayedMonth)
                summary = try await runService.fetchMonthlyRuns(
                    year: components.year ?? calendar.component(.year, from: Date()),
                    month: components.month ?? calendar.component(.month, from: Date()),
                    accessToken: accessToken
                )
            }

            guard requestIdentifier == refreshIdentifier else { return }

            if cache.records != summary.runs {
                cache.records = summary.runs
            }
            if cache.daySummaries != summary.days {
                cache.daySummaries = summary.days
            }
            if cache.totalDistanceMeters != summary.totalDistanceMeters {
                cache.totalDistanceMeters = summary.totalDistanceMeters
            }
            cache.summariesByIdentifier[requestIdentifier] = summary
            cache.loadedRefreshIdentifier = requestIdentifier
            pruneThumbnailCache(for: summary.runs)

            if cache.selectedPeriod == .week {
                await preloadPreviousWeek(accessToken: accessToken)
            }
        } catch {
            guard requestIdentifier == refreshIdentifier else { return }
            if cache.records.isEmpty {
                cache.daySummaries = []
                cache.totalDistanceMeters = 0
                cache.loadedRefreshIdentifier = requestIdentifier
                pruneThumbnailCache(for: [])
            }
        }
    }

    private func useLocalRecords() {
        cache.loadedRefreshIdentifier = nil
        cache.records = historyStore.load()
        cache.daySummaries = []
        cache.totalDistanceMeters = nil
        pruneThumbnailCache(for: cache.records)
    }

    @MainActor
    private func preloadPreviousWeek(accessToken: String) async {
        let date = previousWeek
        let key = weekRefreshIdentifier(for: date)
        guard cache.summariesByIdentifier[key] == nil else { return }

        if let summary = try? await runService.fetchWeeklyRuns(anchorDate: date, accessToken: accessToken) {
            cache.summariesByIdentifier[key] = summary
        }
    }

    @MainActor
    private func refreshThumbnailRecords() async {
        guard let accessToken else { return }

        let recordsNeedingRoute = visibleSelectedRecords.filter { record in
            let cachedRouteCount = cache.thumbnailRecordsByID[record.id]?.routeCoordinates.count ?? 0
            return record.routeCoordinates.count < 2
                && cachedRouteCount < 2
                && !cache.thumbnailFetchIDs.contains(record.id)
        }

        for record in recordsNeedingRoute {
            cache.thumbnailFetchIDs.insert(record.id)

            do {
                let detail = try await runService.fetchRunDetail(
                    runID: record.id.uuidString,
                    accessToken: accessToken
                )
                cache.thumbnailRecordsByID[record.id] = detail
            } catch {
                cache.thumbnailRecordsByID.removeValue(forKey: record.id)
            }

            cache.thumbnailFetchIDs.remove(record.id)
        }
    }

    private func pruneThumbnailCache(for records: [RunningRecord]) {
        let currentIDs = Set(records.map(\.id))
        cache.thumbnailRecordsByID = cache.thumbnailRecordsByID.filter { currentIDs.contains($0.key) }
        cache.thumbnailFetchIDs = cache.thumbnailFetchIDs.intersection(currentIDs)
    }

    private var pendingRecordsForCurrentUser: [RunningRecord] {
        guard let currentUserID else { return [] }
        return historyStore.pendingUploads()
            .filter { $0.ownerUserID == currentUserID }
            .map(\.record)
    }

    private var pendingRecordsForSelectedPeriod: [RunningRecord] {
        records(in: pendingRecordsForCurrentUser)
    }

    private func mergePendingRecords(into remoteRecords: [RunningRecord]) -> [RunningRecord] {
        let remoteIDs = Set(remoteRecords.map(\.id))
        let pendingRecords = pendingRecordsForCurrentUser.filter { !remoteIDs.contains($0.id) }
        return (remoteRecords + pendingRecords).sorted { $0.startedAt > $1.startedAt }
    }

    private func mergePendingDays(into remoteDays: [RunDaySummary]) -> [RunDaySummary] {
        var daysByStartOfDay = Dictionary(uniqueKeysWithValues: remoteDays.map { (calendar.startOfDay(for: $0.date), $0) })
        for record in pendingRecordsForCurrentUser {
            let startOfDay = calendar.startOfDay(for: record.startedAt)
            let existingDistance = daysByStartOfDay[startOfDay]?.distanceMeters ?? 0
            daysByStartOfDay[startOfDay] = RunDaySummary(
                date: startOfDay,
                distanceMeters: existingDistance + Int(record.distanceMeters.rounded()),
                hasRun: true
            )
        }
        return daysByStartOfDay.values.sorted { $0.date < $1.date }
    }

    @MainActor
    private func openRecord(_ record: RunningRecord) async {
        guard let accessToken else {
            selectedRecord = record
            return
        }

        do {
            selectedRecord = try await runService.fetchRunDetail(runID: record.id.uuidString, accessToken: accessToken)
        } catch {
            selectedRecord = record
        }
    }
}

enum HistoryPeriod: String {
    case week = "주"
    case month = "월"
}

private struct HistoryPeriodControl: View {
    @Binding var selectedPeriod: HistoryPeriod

    var body: some View {
        HStack(spacing: 0) {
            option(.week)
            option(.month)
        }
        .padding(4)
        .frame(height: 42)
        .background(Color(red: 0.94, green: 0.94, blue: 0.95))
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }

    private func option(_ period: HistoryPeriod) -> some View {
        Button {
            selectedPeriod = period
        } label: {
            Text(period.rawValue)
                .font(AppTheme.Typography.font(size: 14, weight: .semibold))
                .foregroundStyle(selectedPeriod == period ? Color.white : Color.gray)
                .frame(maxWidth: .infinity)
                .frame(height: 34)
                .background(selectedPeriod == period ? AppTheme.Colors.primary : Color.clear)
                .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
        }
    }
}

private struct WeekDotsView: View {
    let week: Date
    let previousSummary: RunPeriodSummary
    let currentSummary: RunPeriodSummary
    let nextSummary: RunPeriodSummary
    let canMoveToNextWeek: Bool
    let onPreviousWeek: () -> Void
    let onNextWeek: () -> Void

    @State private var dragOffset: CGFloat = 0
    @State private var isTransitioning = false

    private var calendar: Calendar {
        var calendar = Calendar.current
        calendar.firstWeekday = 2
        return calendar
    }

    var body: some View {
        GeometryReader { geometry in
            HStack(spacing: 0) {
                weekContent(for: previousWeek, summary: previousSummary)
                    .frame(width: geometry.size.width)
                weekContent(for: week, summary: currentSummary)
                    .frame(width: geometry.size.width)
                weekContent(for: nextWeek, summary: nextSummary)
                    .frame(width: geometry.size.width)
            }
                .offset(x: -geometry.size.width + dragOffset)
                .contentShape(Rectangle())
                .simultaneousGesture(dragGesture(containerWidth: geometry.size.width))
        }
        .frame(height: 76)
        .clipped()
    }

    private func weekContent(for week: Date, summary: RunPeriodSummary) -> some View {
        HStack(spacing: 0) {
            ForEach(weekDays(for: week, summary: summary), id: \.date) { item in
                VStack(spacing: 8) {
                    Text(item.label)
                        .font(AppTheme.Typography.font(size: 13, weight: .bold))
                        .foregroundStyle(item.hasRun ? AppTheme.Colors.primary : Color.gray)

                    Circle()
                        .fill(item.hasRun ? AppTheme.Colors.primary : Color.gray)
                        .frame(width: 36, height: 36)
                }
                .frame(maxWidth: .infinity)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 6)
    }

    private func dragGesture(containerWidth: CGFloat) -> some Gesture {
        DragGesture(minimumDistance: 12)
            .onChanged { value in
                guard !isTransitioning else { return }
                guard abs(value.translation.width) > abs(value.translation.height) else { return }

                let isDraggingTowardFuture = value.translation.width < 0
                let resistance: CGFloat = isDraggingTowardFuture && !canMoveToNextWeek ? 0.18 : 1
                dragOffset = value.translation.width * resistance
            }
            .onEnded { value in
                guard !isTransitioning else { return }
                guard abs(value.translation.width) > abs(value.translation.height) else {
                    returnToCenter()
                    return
                }

                let projectedTranslation = value.predictedEndTranslation.width
                let threshold = min(containerWidth * 0.2, 72)

                if projectedTranslation > threshold {
                    transitionWeek(exitOffset: containerWidth, onChange: onPreviousWeek)
                } else if projectedTranslation < -threshold, canMoveToNextWeek {
                    transitionWeek(exitOffset: -containerWidth, onChange: onNextWeek)
                } else {
                    returnToCenter()
                }
            }
    }

    private func transitionWeek(exitOffset: CGFloat, onChange: @escaping () -> Void) {
        isTransitioning = true

        withAnimation(.easeInOut(duration: 0.18)) {
            dragOffset = exitOffset
        }

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.18) {
            var transaction = Transaction()
            transaction.disablesAnimations = true
            withTransaction(transaction) {
                onChange()
                dragOffset = 0
            }
            isTransitioning = false
        }
    }

    private func returnToCenter() {
        withAnimation(.spring(response: 0.32, dampingFraction: 0.82)) {
            dragOffset = 0
        }
    }

    private var previousWeek: Date {
        calendar.date(byAdding: .weekOfYear, value: -1, to: week) ?? week
    }

    private var nextWeek: Date {
        calendar.date(byAdding: .weekOfYear, value: 1, to: week) ?? week
    }

    private func weekDays(
        for week: Date,
        summary: RunPeriodSummary
    ) -> [(date: Date, label: String, hasRun: Bool)] {
        let interval = calendar.dateInterval(of: .weekOfYear, for: week)
        let start = interval?.start ?? week
        let symbols = ["일", "월", "화", "수", "목", "금", "토"]

        return (0..<7).compactMap { offset in
            guard let date = calendar.date(byAdding: .day, value: offset, to: start) else { return nil }
            let day = calendar.component(.day, from: date)
            let weekday = calendar.component(.weekday, from: date)
            let hasRun = summary.days.first(where: { calendar.isDate($0.date, inSameDayAs: date) })?.hasRun
                ?? summary.runs.contains { calendar.isDate($0.startedAt, inSameDayAs: date) }
            return (date, "\(day) \(symbols[weekday - 1])", hasRun)
        }
    }
}

private struct MonthCalendarView: View {
    let month: Date
    let records: [RunningRecord]
    let daySummaries: [RunDaySummary]
    let canMoveToNextMonth: Bool
    let onPreviousMonth: () -> Void
    let onNextMonth: () -> Void

    private var calendar: Calendar {
        var calendar = Calendar.current
        calendar.firstWeekday = 1
        return calendar
    }

    var body: some View {
        VStack(spacing: 26) {
            HStack(spacing: 16) {
                Button(action: onPreviousMonth) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundStyle(Color.black)
                        .frame(width: 40, height: 40)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel("이전 달")

                Text(monthTitle)
                    .font(AppTheme.Typography.font(size: 20, weight: .bold))
                    .foregroundStyle(AppTheme.Colors.textPrimary)

                Button(action: onNextMonth) {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundStyle(canMoveToNextMonth ? Color.black : Color.gray.opacity(0.3))
                        .frame(width: 40, height: 40)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .disabled(!canMoveToNextMonth)
                .accessibilityLabel("다음 달")
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            LazyVGrid(columns: columns, spacing: 20) {
                ForEach(weekdayItems) { item in
                    Text(item.label)
                        .font(AppTheme.Typography.font(size: 14, weight: .bold))
                        .foregroundStyle(item.color)
                        .frame(maxWidth: .infinity)
                }

                ForEach(calendarDays) { day in
                    MonthCalendarDayView(day: day)
                }
            }
        }
    }

    private var monthTitle: String {
        let components = calendar.dateComponents([.year, .month], from: month)
        return "\(components.year ?? 0)년 \(components.month ?? 0)월"
    }

    private var columns: [GridItem] {
        Array(repeating: GridItem(.flexible(), spacing: 0), count: 7)
    }

    private var weekdayItems: [WeekdayItem] {
        [
            WeekdayItem(label: "일", color: .red),
            WeekdayItem(label: "월", color: Color(red: 0.58, green: 0.64, blue: 0.72)),
            WeekdayItem(label: "화", color: Color(red: 0.58, green: 0.64, blue: 0.72)),
            WeekdayItem(label: "수", color: Color(red: 0.58, green: 0.64, blue: 0.72)),
            WeekdayItem(label: "목", color: Color(red: 0.58, green: 0.64, blue: 0.72)),
            WeekdayItem(label: "금", color: Color(red: 0.58, green: 0.64, blue: 0.72)),
            WeekdayItem(label: "토", color: .blue)
        ]
    }

    private var calendarDays: [MonthCalendarDay] {
        guard
            let monthInterval = calendar.dateInterval(of: .month, for: month),
            let gridStart = calendar.dateInterval(of: .weekOfYear, for: monthInterval.start)?.start,
            let monthEnd = calendar.date(byAdding: .day, value: -1, to: monthInterval.end),
            let gridEnd = calendar.dateInterval(of: .weekOfYear, for: monthEnd)?.end
        else {
            return []
        }

        let dayCount = calendar.dateComponents([.day], from: gridStart, to: gridEnd).day ?? 0

        return (0..<dayCount).compactMap { offset in
            guard let date = calendar.date(byAdding: .day, value: offset, to: gridStart) else { return nil }
            let day = calendar.component(.day, from: date)
            let isInDisplayedMonth = calendar.isDate(date, equalTo: month, toGranularity: .month)
            let hasRun = daySummaries.first(where: { calendar.isDate($0.date, inSameDayAs: date) })?.hasRun
                ?? records.contains { calendar.isDate($0.startedAt, inSameDayAs: date) }

            return MonthCalendarDay(
                date: date,
                dayText: "\(day)",
                isInDisplayedMonth: isInDisplayedMonth,
                hasRun: hasRun
            )
        }
    }
}

private struct WeekdayItem: Identifiable {
    let id = UUID()
    let label: String
    let color: Color
}

private struct MonthCalendarDay: Identifiable {
    let date: Date
    let dayText: String
    let isInDisplayedMonth: Bool
    let hasRun: Bool

    var id: Date {
        date
    }
}

private struct MonthCalendarDayView: View {
    let day: MonthCalendarDay

    var body: some View {
        VStack(spacing: 9) {
            Text(day.dayText)
                .font(AppTheme.Typography.font(size: 20, weight: .medium))
                .foregroundStyle(day.isInDisplayedMonth ? AppTheme.Colors.textPrimary : Color(red: 0.78, green: 0.82, blue: 0.87))

            Circle()
                .fill(day.hasRun ? AppTheme.Colors.primary : Color.clear)
                .frame(width: 10, height: 10)
        }
        .frame(height: 48)
        .frame(maxWidth: .infinity)
    }
}

private struct RunningRecordCard: View {
    let record: RunningRecord
    let thumbnailRecord: RunningRecord

    var body: some View {
        HStack(spacing: 18) {
            RunningRouteThumbnailView(record: thumbnailRecord)
                .frame(width: 96, height: 96)
                .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))

            VStack(alignment: .leading, spacing: 8) {
                Text(Self.dateFormatter.string(from: record.startedAt))
                    .font(AppTheme.Typography.caption1)
                    .foregroundStyle(Color.gray)

                Text("\(record.distanceKilometers.formatted(.number.precision(.fractionLength(2))))KM")
                    .font(AppTheme.Typography.font(size: 22, weight: .bold))
                    .foregroundStyle(.black)

                HStack(spacing: 16) {
                    runningMetricLabel(
                        icon: "icon_metric_time",
                        text: RunningMetricFormatter.duration(record.elapsedTime)
                    )
                    runningMetricLabel(
                        icon: "icon_metric_pace",
                        text: "\(RunningMetricFormatter.pace(record.averagePaceSecondsPerKilometer))/km"
                    )
                }
                .font(AppTheme.Typography.font(size: 15, weight: .semibold))
                .foregroundStyle(Color.gray)
            }

            Spacer()
        }
        .padding(14)
        .frame(height: 126)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        .shadow(color: .black.opacity(0.08), radius: 16, x: 0, y: 8)
    }

    private func runningMetricLabel(icon: String, text: String) -> some View {
        HStack(spacing: 4) {
            Image(icon)
                .resizable()
                .scaledToFit()
                .frame(width: 16, height: 16)
            Text(text)
        }
    }
}

private struct RunningRouteThumbnailView: View {
    let record: RunningRecord
    @State private var cameraPosition: MapCameraPosition

    init(record: RunningRecord) {
        self.record = record
        _cameraPosition = State(initialValue: Self.initialCameraPosition(for: record.routeCoordinates))
    }

    var body: some View {
        Group {
            if record.routeCoordinates.count >= 2 {
                Map(position: $cameraPosition) {
                    MapPolyline(coordinates: record.routeCoordinates)
                        .stroke(AppTheme.Colors.primary, lineWidth: 4)
                }
                .mapStyle(.standard(elevation: .flat))
                .allowsHitTesting(false)
            } else {
                RouteThumbnailSkeletonView()
            }
        }
        .onChange(of: routeSignature) { _, _ in
            cameraPosition = Self.initialCameraPosition(for: record.routeCoordinates)
        }
        .overlay {
            RoundedRectangle(cornerRadius: 8, style: .continuous)
                .stroke(Color.black.opacity(0.05), lineWidth: 1)
        }
    }

    private static func initialCameraPosition(for coordinates: [CLLocationCoordinate2D]) -> MapCameraPosition {
        guard !coordinates.isEmpty else {
            return .region(
                MKCoordinateRegion(
                    center: CLLocationCoordinate2D(latitude: 37.5665, longitude: 126.9780),
                    span: MKCoordinateSpan(latitudeDelta: 0.02, longitudeDelta: 0.02)
                )
            )
        }

        return RunningMapView.cameraPosition(fitting: coordinates)
    }

    private var routeSignature: String {
        let first = record.routeCoordinates.first
        let last = record.routeCoordinates.last
        return [
            record.id.uuidString,
            "\(record.routeCoordinates.count)",
            first.map { "\($0.latitude),\($0.longitude)" } ?? "",
            last.map { "\($0.latitude),\($0.longitude)" } ?? ""
        ].joined(separator: "|")
    }
}

private struct RouteThumbnailSkeletonView: View {
    @State private var isPulsing = false

    var body: some View {
        RoundedRectangle(cornerRadius: 8, style: .continuous)
            .fill(Color(red: 0.92, green: 0.94, blue: 0.97))
            .overlay(alignment: .topLeading) {
                RoundedRectangle(cornerRadius: 4, style: .continuous)
                    .fill(Color.white.opacity(0.48))
                    .frame(width: 42, height: 10)
                    .padding(12)
            }
            .opacity(isPulsing ? 0.62 : 1)
            .animation(
                .easeInOut(duration: 0.85).repeatForever(autoreverses: true),
                value: isPulsing
            )
            .onAppear {
                isPulsing = true
            }
    }
}

private extension RunningRecordCard {
    static let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "yyyy. MM. dd EEEE"
        return formatter
    }()
}

private extension View {
    @ViewBuilder
    func runpamineFullScreenCover<Item: Identifiable, Content: View>(
        item: Binding<Item?>,
        @ViewBuilder content: @escaping (Item) -> Content
    ) -> some View {
        #if os(iOS)
        fullScreenCover(item: item, content: content)
        #else
        sheet(item: item, content: content)
        #endif
    }
}
