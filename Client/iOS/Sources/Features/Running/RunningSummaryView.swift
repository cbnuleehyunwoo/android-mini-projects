import SwiftUI

struct RunningSummaryView: View {
    let record: RunningRecord
    let onDone: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            if record.routeCoordinates.count >= 2 {
                RunningMapView(route: record.routeCoordinates, latestLocation: nil)
                    .frame(height: 330)
                    .clipped()
            } else {
                MapPlaceholderView()
                    .frame(height: 330)
                    .overlay(Color.black.opacity(0.6))
            }

            VStack(spacing: 20) {
                Label("시간", systemImage: "stopwatch")
                    .font(AppTheme.Typography.body2)
                    .foregroundStyle(.black)
                    .padding(.top, 64)

                Text(RunningMetricFormatter.duration(record.elapsedTime))
                    .font(AppTheme.Typography.font(size: 31, weight: .black))
                    .foregroundStyle(.black)

                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 2) {
                    SummaryMetric(title: "거리", value: RunningMetricFormatter.distanceKilometers(record.distanceMeters), suffix: "km", icon: "speedometer")
                    SummaryMetric(title: "칼로리", value: "\(record.estimatedCalories)", suffix: "kcal", icon: "flame")
                    SummaryMetric(title: "페이스", value: RunningMetricFormatter.pace(record.averagePaceSecondsPerKilometer), suffix: "/km", icon: "speedometer")
                    SummaryMetric(title: "칼로리", value: "\(record.estimatedCalories)", suffix: "kcal", icon: "flame")
                }
                .padding(.horizontal, 22)
                .padding(.top, 26)

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
            Label(title, systemImage: icon)
                .font(AppTheme.Typography.font(size: 13, weight: .medium))
                .foregroundStyle(AppTheme.Colors.textPrimary)
            HStack(alignment: .lastTextBaseline, spacing: 3) {
                Text(value)
                    .font(AppTheme.Typography.font(size: 27, weight: .black))
                    .foregroundStyle(.black)
                Text(suffix)
                    .font(AppTheme.Typography.font(size: 12, weight: .black))
                    .foregroundStyle(.black)
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
