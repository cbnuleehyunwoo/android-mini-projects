import MapKit
import SwiftUI

struct HistoryView: View {
    @State private var selectedPeriod: HistoryPeriod = .week
    @State private var displayedMonth = Date()
    @State private var records: [RunningRecord]
    @State private var daySummaries: [RunDaySummary] = []
    @State private var totalDistanceMeters: Int?
    @State private var loadedRefreshIdentifier: String?
    @State private var selectedRecord: RunningRecord?
    @State private var thumbnailRecordsByID: [UUID: RunningRecord] = [:]
    @State private var thumbnailFetchIDs: Set<UUID> = []

    private let runService: RunServiceProtocol
    private let accessToken: String?
    private let calendar = Calendar.current

    init(runService: RunServiceProtocol = MockRunService(), accessToken: String? = nil) {
        self.runService = runService
        self.accessToken = accessToken
        _records = State(initialValue: RunningHistoryStore().load())
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

                if selectedPeriod == .week {
                    WeekDotsView(records: visibleRecords, daySummaries: visibleDaySummaries)
                        .padding(.horizontal, 30)
                        .padding(.top, 28)
                } else {
                    MonthCalendarView(
                        month: displayedMonth,
                        records: visibleRecords,
                        daySummaries: visibleDaySummaries,
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
                                    thumbnailRecord: thumbnailRecordsByID[record.id] ?? record
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
                records = RunningHistoryStore().load()
            }
        }
        .task(id: refreshIdentifier) {
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

            HistoryPeriodControl(selectedPeriod: $selectedPeriod)
                .frame(width: 150)
        }
        .padding(.horizontal, 30)
        .padding(.top, 34)
    }

    private var visibleSelectedRecords: [RunningRecord] {
        records(in: visibleRecords)
    }

    private var visibleRecords: [RunningRecord] {
        guard accessToken != nil else { return records }
        return loadedRefreshIdentifier == refreshIdentifier ? records : []
    }

    private var visibleDaySummaries: [RunDaySummary] {
        guard accessToken != nil else { return daySummaries }
        return loadedRefreshIdentifier == refreshIdentifier ? daySummaries : []
    }

    private var isWaitingForRemoteRecords: Bool {
        accessToken != nil && loadedRefreshIdentifier != refreshIdentifier
    }

    private func records(in records: [RunningRecord]) -> [RunningRecord] {
        records.filter { record in
            switch selectedPeriod {
            case .week:
                return calendar.isDate(record.startedAt, equalTo: Date(), toGranularity: .weekOfYear)
            case .month:
                return calendar.isDate(record.startedAt, equalTo: displayedMonth, toGranularity: .month)
            }
        }
    }

    private var totalDistanceText: String {
        guard !isWaitingForRemoteRecords else { return "0.0" }

        let total = totalDistanceMeters.map { Double($0) / 1_000 } ?? visibleSelectedRecords.reduce(0) { $0 + $1.distanceKilometers }
        return total.formatted(.number.precision(.fractionLength(1)))
    }

    private var refreshIdentifier: String {
        let components = calendar.dateComponents([.year, .month], from: displayedMonth)
        return "\(selectedPeriod.rawValue)-\(components.year ?? 0)-\(components.month ?? 0)"
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
            Text(selectedPeriod == .week ? "이번 주 러닝 기록이 없어요" : "이번 달 러닝 기록이 없어요")
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
        displayedMonth = calendar.date(byAdding: .month, value: -1, to: displayedMonth) ?? displayedMonth
    }

    private func moveToNextMonth() {
        displayedMonth = calendar.date(byAdding: .month, value: 1, to: displayedMonth) ?? displayedMonth
    }

    @MainActor
    private func refreshRecords() async {
        guard let accessToken else {
            useLocalRecords()
            return
        }

        let requestIdentifier = refreshIdentifier
        loadedRefreshIdentifier = nil
        records = []
        daySummaries = []
        totalDistanceMeters = 0
        pruneThumbnailCache(for: [])

        do {
            let summary: RunPeriodSummary
            switch selectedPeriod {
            case .week:
                summary = try await runService.fetchWeeklyRuns(anchorDate: Date(), accessToken: accessToken)
            case .month:
                let components = calendar.dateComponents([.year, .month], from: displayedMonth)
                summary = try await runService.fetchMonthlyRuns(
                    year: components.year ?? calendar.component(.year, from: Date()),
                    month: components.month ?? calendar.component(.month, from: Date()),
                    accessToken: accessToken
                )
            }

            guard requestIdentifier == refreshIdentifier else { return }

            records = summary.runs
            daySummaries = summary.days
            totalDistanceMeters = summary.totalDistanceMeters
            loadedRefreshIdentifier = requestIdentifier
            pruneThumbnailCache(for: summary.runs)
        } catch {
            guard requestIdentifier == refreshIdentifier else { return }

            records = []
            daySummaries = []
            totalDistanceMeters = 0
            loadedRefreshIdentifier = requestIdentifier
            pruneThumbnailCache(for: [])
        }
    }

    private func useLocalRecords() {
        loadedRefreshIdentifier = nil
        records = RunningHistoryStore().load()
        daySummaries = []
        totalDistanceMeters = nil
        pruneThumbnailCache(for: records)
    }

    @MainActor
    private func refreshThumbnailRecords() async {
        guard let accessToken else { return }

        let recordsNeedingRoute = visibleSelectedRecords.filter { record in
            let cachedRouteCount = thumbnailRecordsByID[record.id]?.routeCoordinates.count ?? 0
            return record.routeCoordinates.count < 2
                && cachedRouteCount < 2
                && !thumbnailFetchIDs.contains(record.id)
        }

        for record in recordsNeedingRoute {
            thumbnailFetchIDs.insert(record.id)

            do {
                let detail = try await runService.fetchRunDetail(
                    runID: record.id.uuidString,
                    accessToken: accessToken
                )
                thumbnailRecordsByID[record.id] = detail
            } catch {
                thumbnailRecordsByID.removeValue(forKey: record.id)
            }

            thumbnailFetchIDs.remove(record.id)
        }
    }

    private func pruneThumbnailCache(for records: [RunningRecord]) {
        let currentIDs = Set(records.map(\.id))
        thumbnailRecordsByID = thumbnailRecordsByID.filter { currentIDs.contains($0.key) }
        thumbnailFetchIDs = thumbnailFetchIDs.intersection(currentIDs)
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

private enum HistoryPeriod: String {
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
    let records: [RunningRecord]
    let daySummaries: [RunDaySummary]
    private let calendar = Calendar.current

    var body: some View {
        HStack(spacing: 0) {
            ForEach(currentWeekDays, id: \.date) { item in
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
    }

    private var currentWeekDays: [(date: Date, label: String, hasRun: Bool)] {
        let now = Date()
        let interval = calendar.dateInterval(of: .weekOfYear, for: now)
        let start = interval?.start ?? now
        let symbols = ["일", "월", "화", "수", "목", "금", "토"]

        return (0..<7).compactMap { offset in
            guard let date = calendar.date(byAdding: .day, value: offset, to: start) else { return nil }
            let day = calendar.component(.day, from: date)
            let weekday = calendar.component(.weekday, from: date)
            let hasRun = daySummaries.first(where: { calendar.isDate($0.date, inSameDayAs: date) })?.hasRun
                ?? records.contains { calendar.isDate($0.startedAt, inSameDayAs: date) }
            return (date, "\(day) \(symbols[weekday - 1])", hasRun)
        }
    }
}

private struct MonthCalendarView: View {
    let month: Date
    let records: [RunningRecord]
    let daySummaries: [RunDaySummary]
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
                    Image("icon_back")
                        .resizable()
                        .renderingMode(.template)
                        .scaledToFit()
                        .foregroundStyle(Color(red: 0.45, green: 0.53, blue: 0.64))
                        .frame(width: 20, height: 20)
                }
                .buttonStyle(.plain)

                Text(monthTitle)
                    .font(AppTheme.Typography.font(size: 20, weight: .bold))
                    .foregroundStyle(AppTheme.Colors.textPrimary)

                Button(action: onNextMonth) {
                    Image("icon_back")
                        .resizable()
                        .renderingMode(.template)
                        .scaledToFit()
                        .foregroundStyle(Color(red: 0.45, green: 0.53, blue: 0.64))
                        .frame(width: 20, height: 20)
                        .rotationEffect(.degrees(180))
                }
                .buttonStyle(.plain)
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

                Text("\(record.distanceKilometers.formatted(.number.precision(.fractionLength(1))))KM")
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
