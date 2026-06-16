import SwiftUI

struct RunningSummaryView: View {
    let record: RunningRecord
    let onDone: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            RunningMapView(route: record.routeCoordinates, latestLocation: nil)
                .frame(height: 330)
                .clipped()

            VStack(spacing: 20) {
                VStack(alignment: .leading, spacing: 6) {
                    Text(Self.dateFormatter.string(from: record.startedAt))
                        .font(AppTheme.Typography.font(size: 20, weight: .bold))
                        .foregroundStyle(.black)

                    Text("\(Self.timeFormatter.string(from: record.startedAt)) ~ \(Self.timeFormatter.string(from: record.endedAt))")
                        .font(AppTheme.Typography.font(size: 16, weight: .regular))
                        .foregroundStyle(AppTheme.Colors.textPrimary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 30)
                .padding(.top, 28)

                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                    SummaryMetric(title: "시간", value: RunningMetricFormatter.duration(record.elapsedTime), suffix: "")
                    SummaryMetric(title: "거리", value: RunningMetricFormatter.distanceKilometers(record.distanceMeters), suffix: "km")
                    SummaryMetric(title: "페이스", value: RunningMetricFormatter.pace(record.averagePaceSecondsPerKilometer), suffix: "/km")
                    SummaryMetric(title: "칼로리", value: "\(record.estimatedCalories)", suffix: "kcal")
                }
                .padding(.horizontal, 22)

                Spacer()

                PrimaryButton(title: "완료") {
                    onDone()
                }
                .padding(.horizontal, AppTheme.Layout.horizontalPadding)
                .padding(.bottom, 34)
            }
            .background(Color.white)
        }
        .ignoresSafeArea(edges: .top)
    }

    private static let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "yyyy년 M월 d일 EEEE"
        return formatter
    }()

    private static let timeFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "a h:mm"
        return formatter
    }()
}

private struct SummaryMetric: View {
    let title: String
    let value: String
    let suffix: String

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title)
                .font(AppTheme.Typography.font(size: 13, weight: .medium))
                .foregroundStyle(AppTheme.Colors.textPrimary)

            HStack(alignment: .lastTextBaseline, spacing: 3) {
                Text(value)
                    .font(AppTheme.Typography.font(size: 27, weight: .black))
                    .foregroundStyle(.black)
                if !suffix.isEmpty {
                    Text(suffix)
                        .font(AppTheme.Typography.font(size: 12, weight: .black))
                        .foregroundStyle(.black)
                }
            }
        }
        .padding(.horizontal, 24)
        .frame(maxWidth: .infinity, alignment: .leading)
        .frame(height: 110)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
        .shadow(color: .black.opacity(0.06), radius: 12, x: 0, y: 6)
    }
}

#Preview("러닝 요약 화면") {
    RunningSummaryView(
        record: RunningRecord(
            startedAt: Date().addingTimeInterval(-1_725),
            endedAt: Date(),
            elapsedTime: 1_725,
            distanceMeters: 5_000,
            route: []
        ),
        onDone: {}
    )
}
