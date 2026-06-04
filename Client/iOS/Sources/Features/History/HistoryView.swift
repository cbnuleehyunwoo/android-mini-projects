import MapKit
import SwiftUI

struct HistoryView: View {
    @State private var selectedPeriod: HistoryPeriod = .week
    @State private var displayedMonth = Date()
    @State private var records: [RunningRecord] = RunningHistoryStore().load()

    private let calendar = Calendar.current

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(alignment: .leading, spacing: 0) {
                header

                HStack(alignment: .lastTextBaseline, spacing: 8) {
                    Text(totalDistanceText)
                        .font(AppTheme.Typography.font(size: 68, weight: .black))
                        .foregroundStyle(AppTheme.Colors.primary)
                    Text("KM")
                        .font(AppTheme.Typography.font(size: 18, weight: .black))
                        .foregroundStyle(Color.gray)
                }
                .padding(.horizontal, 30)
                .padding(.top, 20)

                if selectedPeriod == .week {
                    WeekDotsView(records: records)
                        .padding(.horizontal, 30)
                        .padding(.top, 28)
                } else {
                    MonthCalendarView(
                        month: displayedMonth,
                        records: records,
                        onPreviousMonth: moveToPreviousMonth,
                        onNextMonth: moveToNextMonth
                    )
                        .padding(.horizontal, 30)
                        .padding(.top, 24)
                }

                VStack(spacing: 22) {
                    if selectedRecords.isEmpty {
                        emptyState
                    } else {
                        ForEach(selectedRecords) { record in
                            RunningRecordCard(record: record)
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
            records = RunningHistoryStore().load()
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

    private var selectedRecords: [RunningRecord] {
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
        let total = selectedRecords.reduce(0) { $0 + $1.distanceKilometers }
        return total.formatted(.number.precision(.fractionLength(1)))
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

    private func moveToPreviousMonth() {
        displayedMonth = calendar.date(byAdding: .month, value: -1, to: displayedMonth) ?? displayedMonth
    }

    private func moveToNextMonth() {
        displayedMonth = calendar.date(byAdding: .month, value: 1, to: displayedMonth) ?? displayedMonth
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
                .font(AppTheme.Typography.font(size: 16, weight: .black))
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
                        .frame(width: 42, height: 42)
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
            let hasRun = records.contains { calendar.isDate($0.startedAt, inSameDayAs: date) }
            return (date, "\(day) \(symbols[weekday - 1])", hasRun)
        }
    }
}

private struct MonthCalendarView: View {
    let month: Date
    let records: [RunningRecord]
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
                        .font(.system(size: 20, weight: .bold))
                        .foregroundStyle(Color(red: 0.45, green: 0.53, blue: 0.64))
                }
                .buttonStyle(.plain)

                Text(monthTitle)
                    .font(AppTheme.Typography.font(size: 28, weight: .black))
                    .foregroundStyle(AppTheme.Colors.textPrimary)

                Button(action: onNextMonth) {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundStyle(Color(red: 0.45, green: 0.53, blue: 0.64))
                }
                .buttonStyle(.plain)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            LazyVGrid(columns: columns, spacing: 20) {
                ForEach(weekdayItems) { item in
                    Text(item.label)
                        .font(AppTheme.Typography.font(size: 16, weight: .black))
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
            let hasRun = records.contains { calendar.isDate($0.startedAt, inSameDayAs: date) }

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

    var body: some View {
        HStack(spacing: 18) {
            RunningRouteThumbnailView(record: record)
                .frame(width: 96, height: 96)
                .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))

            VStack(alignment: .leading, spacing: 8) {
                Text(record.startedAt.formatted(.dateTime.year().month().day().weekday(.wide)))
                    .font(AppTheme.Typography.caption1)
                    .foregroundStyle(Color.gray)

                Text("\(record.distanceKilometers.formatted(.number.precision(.fractionLength(1))))KM")
                    .font(AppTheme.Typography.font(size: 24, weight: .black))
                    .foregroundStyle(.black)

                HStack(spacing: 16) {
                    Label(RunningMetricFormatter.duration(record.elapsedTime), systemImage: "clock")
                    Label("\(RunningMetricFormatter.pace(record.averagePaceSecondsPerKilometer))/km", systemImage: "speedometer")
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
}

private struct RunningRouteThumbnailView: View {
    let record: RunningRecord
    @State private var cameraPosition: MapCameraPosition

    init(record: RunningRecord) {
        self.record = record
        _cameraPosition = State(initialValue: Self.initialCameraPosition(for: record.routeCoordinates))
    }

    var body: some View {
        Map(position: $cameraPosition) {
            if record.routeCoordinates.count >= 2 {
                MapPolyline(coordinates: record.routeCoordinates)
                    .stroke(AppTheme.Colors.primary, lineWidth: 4)
            }
        }
        .mapStyle(.standard(elevation: .flat))
        .allowsHitTesting(false)
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
}
