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
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 2) {
                    SummaryMetric(title: "시간", value: RunningMetricFormatter.duration(record.elapsedTime), suffix: "", icon: "icon_metric_time")
                    SummaryMetric(title: "거리", value: RunningMetricFormatter.distanceKilometers(record.distanceMeters), suffix: "km", icon: "icon_footprint")
                    SummaryMetric(title: "페이스", value: RunningMetricFormatter.pace(record.averagePaceSecondsPerKilometer), suffix: "/km", icon: "icon_metric_pace")
                    SummaryMetric(title: "칼로리", value: "\(record.estimatedCalories)", suffix: "kcal", icon: "icon_metric_kcal")
                }
                .padding(.horizontal, 22)
                .padding(.top, 110)

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
}

private struct SummaryMetric: View {
    let title: String
    let value: String
    let suffix: String
    let icon: String

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 6) {
                Image(icon)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 16, height: 16)
                Text(title)
                    .font(AppTheme.Typography.font(size: 13, weight: .medium))
                    .foregroundStyle(AppTheme.Colors.textPrimary)
            }
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
