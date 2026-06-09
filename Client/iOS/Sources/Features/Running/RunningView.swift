import SwiftUI

struct RunningView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var tracker = RunningTracker()
    @State private var finishedRecord: RunningRecord?
    private let runService: RunServiceProtocol
    private let accessToken: String?

    init(runService: RunServiceProtocol = MockRunService(), accessToken: String? = nil) {
        self.runService = runService
        self.accessToken = accessToken
    }

    var body: some View {
        Group {
            if let finishedRecord {
                RunningSummaryView(record: finishedRecord) {
                    dismiss()
                }
            } else {
                activeRunningView
            }
        }
        .task {
            if tracker.trackingState == .idle {
                tracker.start()
            }
        }
    }

    private var activeRunningView: some View {
        ZStack {
            runningBackground

            VStack(spacing: 0) {
                Spacer()
                runningContentPanel
            }
        }
    }

    @ViewBuilder
    private var runningBackground: some View {
        if tracker.route.count >= 2 || tracker.latestLocation != nil {
            RunningMapView(route: tracker.route, latestLocation: tracker.latestLocation, focusesVisibleUpperArea: true)
                .opacity(0.94)
        } else {
            Color.white.ignoresSafeArea()
        }
    }

    private var runningContentPanel: some View {
        VStack(spacing: 14) {
            distanceSection
            durationSection

            HStack(spacing: 14) {
                RunningMetricCard(title: "페이스", value: RunningMetricFormatter.pace(tracker.averagePaceSecondsPerKilometer), suffix: "/km", icon: "icon_metric_pace")
                RunningMetricCard(title: "칼로리", value: "\(tracker.estimatedCalories)", suffix: "kcal", icon: "icon_metric_kcal")
            }
            .padding(.horizontal, 24)
            .padding(.top, 8)

            errorMessage

            controlButtons
        }
        .padding(.top, 30)
        .background(Color.white.opacity(tracker.route.count >= 2 ? 0.94 : 1))
    }

    private var distanceSection: some View {
        VStack(spacing: 14) {
            HStack(spacing: 6) {
                Image("icon_footprint")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 16, height: 16)
                Text("거리")
            }
            .font(AppTheme.Typography.body2)
            .foregroundStyle(.black)

            HStack(alignment: .lastTextBaseline, spacing: 7) {
                Text(RunningMetricFormatter.distanceKilometers(tracker.distanceMeters))
                    .font(AppTheme.Typography.font(size: 70, weight: .black))
                    .foregroundStyle(.black)
                Text("km")
                    .font(AppTheme.Typography.font(size: 28, weight: .black))
                    .foregroundStyle(Color.gray)
            }
        }
    }

    private var durationSection: some View {
        VStack(spacing: 14) {
            HStack(spacing: 4) {
                Image("icon_metric_time")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 16, height: 16)
                Text("시간")
            }
            .font(AppTheme.Typography.font(size: 13, weight: .medium))
            .foregroundStyle(.black)
            .padding(.top, 8)

            Text(RunningMetricFormatter.duration(tracker.elapsedTime))
                .font(AppTheme.Typography.header1)
                .foregroundStyle(.black)
        }
    }

    @ViewBuilder
    private var errorMessage: some View {
        if let error = tracker.lastError {
            Text(error)
                .font(AppTheme.Typography.font(size: 13, weight: .semibold))
                .foregroundStyle(AppTheme.Colors.danger)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 24)
                .padding(.top, 10)
        }
    }

    private var controlButtons: some View {
        HStack(spacing: 12) {
            Button {
                togglePause()
            } label: {
                Label(tracker.trackingState == .paused ? "재시작" : "일시 정지", systemImage: tracker.trackingState == .paused ? "play.fill" : "pause.fill")
                    .font(AppTheme.Typography.font(size: 13, weight: .black))
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(Color.black)
                    .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
            }

            Button {
                finishRunning()
            } label: {
                Label("종료", systemImage: "stop.fill")
                    .font(AppTheme.Typography.font(size: 13, weight: .black))
                    .foregroundStyle(Color(red: 0.72, green: 0.05, blue: 0.05))
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(Color(red: 0.98, green: 0.90, blue: 0.90))
                    .overlay {
                        RoundedRectangle(cornerRadius: 8, style: .continuous)
                            .stroke(Color(red: 0.90, green: 0.55, blue: 0.55), lineWidth: 1)
                    }
                    .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
            }
        }
        .padding(.horizontal, 24)
        .padding(.top, 36)
        .padding(.bottom, 42)
    }

    private func togglePause() {
        if tracker.trackingState == .paused {
            tracker.resume()
        } else {
            tracker.pause()
        }
    }

    private func finishRunning() {
        let record: RunningRecord
        if let completedRecord = tracker.end() {
            record = completedRecord
        } else {
            record = fallbackRunningRecord()
        }
        finishedRecord = record

        Task {
            await saveRun(record)
        }
    }

    private func fallbackRunningRecord() -> RunningRecord {
        let endedAt = Date()
        let route = tracker.route.map { coordinate in
            RunningCoordinate(coordinate)
        }

        return RunningRecord(
            startedAt: endedAt.addingTimeInterval(-tracker.elapsedTime),
            endedAt: endedAt,
            elapsedTime: tracker.elapsedTime,
            distanceMeters: tracker.distanceMeters,
            route: route
        )
    }

    private func saveRun(_ record: RunningRecord) async {
        guard let accessToken else { return }
        _ = try? await runService.createRun(record: record, accessToken: accessToken)
    }
}

private struct RunningMetricCard: View {
    let title: String
    let value: String
    let suffix: String
    let icon: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
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
                    .font(AppTheme.Typography.font(size: 28, weight: .black))
                    .foregroundStyle(.black)
                Text(suffix)
                    .font(AppTheme.Typography.font(size: 12, weight: .black))
                    .foregroundStyle(.black)
            }
        }
        .padding(.horizontal, 22)
        .frame(maxWidth: .infinity, alignment: .leading)
        .frame(height: 110)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        .shadow(color: .black.opacity(0.08), radius: 15, x: 0, y: 8)
    }
}
