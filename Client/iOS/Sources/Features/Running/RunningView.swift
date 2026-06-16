import SwiftUI

struct RunningView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var tracker = RunningTracker()
    @State private var finishedRecord: RunningRecord?
    @State private var isShowingStopDialog = false
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
        .interactiveDismissDisabled(finishedRecord == nil)
        .overlay {
            if isShowingStopDialog {
                RunpamineConfirmationDialog(
                    title: "러닝 종료",
                    message: "러닝을 종료하시겠습니까?",
                    dismissText: "취소",
                    confirmText: "종료",
                    onDismiss: {
                        isShowingStopDialog = false
                    },
                    onConfirm: finishRunning
                )
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
            Color.white.ignoresSafeArea()

            VStack(spacing: 0) {
                Spacer(minLength: 0)
                runningContentPanel
                Spacer(minLength: 0)
            }
        }
        .runpamineBackSwipe {
            requestFinishRunning()
        }
    }

    private var runningContentPanel: some View {
        VStack(spacing: 2) {
            distanceSection
            durationSection

            HStack(spacing: 14) {
                RunningMetricCard(title: "페이스", value: RunningMetricFormatter.pace(tracker.averagePaceSecondsPerKilometer), suffix: "/km")
                RunningMetricCard(title: "칼로리", value: "\(tracker.estimatedCalories)", suffix: "kcal")
            }
            .padding(.horizontal, 24)
            .padding(.top, 8)

            errorMessage

            controlButtons
        }
        .padding(.top, 30)
        .background(Color.white)
    }

    private var distanceSection: some View {
        VStack(spacing: 2) {
            Text("거리")
            .font(AppTheme.Typography.body2)
            .foregroundStyle(.black)

            HStack(alignment: .lastTextBaseline, spacing: 7) {
                Text(RunningMetricFormatter.distanceKilometers(tracker.distanceMeters))
                    .font(AppTheme.Typography.font(size: 80, weight: .extraBold))
                    .foregroundStyle(.black)
                Text("km")
                    .font(AppTheme.Typography.font(size: 28, weight: .black))
                    .foregroundStyle(Color.gray)
            }
        }
    }

    private var durationSection: some View {
        VStack(spacing: 14) {
            Text("시간")
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
        RunningControlButtons(
            isPaused: isPaused,
            onStop: finishRunning,
            onTogglePause: togglePause
        )
        .padding(.horizontal, 24)
        .padding(.top, 16)
        .padding(.bottom, 42)
    }

    private var isPaused: Bool {
        tracker.trackingState == .paused
    }

    private func togglePause() {
        withAnimation(.spring(response: 0.32, dampingFraction: 0.82)) {
            if tracker.trackingState == .paused {
                tracker.resume()
            } else {
                tracker.pause()
            }
        }
    }

    private func requestFinishRunning() {
        guard finishedRecord == nil else { return }
        isShowingStopDialog = true
    }

    private func finishRunning() {
        isShowingStopDialog = false

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
            averagePaceSecondsPerKilometer: tracker.averagePaceSecondsPerKilometer,
            route: route
        )
    }

    private func saveRun(_ record: RunningRecord) async {
        guard let accessToken else { return }
        _ = try? await runService.createRun(record: record, accessToken: accessToken)
    }
}

private struct RunningControlButtons: View {
    let isPaused: Bool
    let onStop: () -> Void
    let onTogglePause: () -> Void

    var body: some View {
        ZStack {
            RunningControlButton(
                systemImage: "stop.fill",
                accessibilityLabel: "러닝 중지",
                foregroundColor: .white,
                backgroundColor: .black,
                iconSize: 31,
                action: onStop
            )
            .offset(x: isPaused ? -51 : 0)
            .opacity(isPaused ? 1 : 0)
            .allowsHitTesting(isPaused)

            RunningControlButton(
                systemImage: isPaused ? "play.fill" : "pause.fill",
                accessibilityLabel: isPaused ? "러닝 재개" : "러닝 일시 정지",
                foregroundColor: isPaused ? .black : .white,
                backgroundColor: isPaused ? AppTheme.Colors.success : .black,
                iconSize: isPaused ? 34 : 32,
                action: onTogglePause
            )
            .offset(x: isPaused ? 51 : 0)
        }
        .frame(height: 70)
        .animation(.spring(response: 0.32, dampingFraction: 0.82), value: isPaused)
    }
}

private struct RunningControlButton: View {
    let systemImage: String
    let accessibilityLabel: String
    let foregroundColor: Color
    let backgroundColor: Color
    let iconSize: CGFloat
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: systemImage)
                .font(.system(size: iconSize, weight: .black))
                .foregroundStyle(foregroundColor)
                .frame(width: 70, height: 70)
                .background(backgroundColor)
                .clipShape(Circle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(accessibilityLabel)
    }
}

private struct RunningMetricCard: View {
    let title: String
    let value: String
    let suffix: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(AppTheme.Typography.font(size: 14, weight: .medium))
                .foregroundStyle(AppTheme.Colors.textPrimary)

            HStack(alignment: .lastTextBaseline, spacing: 3) {
                Text(value)
                    .font(AppTheme.Typography.font(size: 28, weight: .bold))
                    .foregroundStyle(.black)
                Text(suffix)
                    .font(AppTheme.Typography.font(size: 12, weight: .medium))
                    .foregroundStyle(.gray)
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

#Preview("러닝 화면") {
    RunningView()
}
