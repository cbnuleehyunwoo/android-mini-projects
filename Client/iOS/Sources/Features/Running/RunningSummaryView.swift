import SwiftUI
import UIKit

struct RunningSummaryView: View {
    let record: RunningRecord
    var isSaving = false
    var saveErrorMessage: String?
    var onRetry: (() -> Void)?
    let onDone: () -> Void
    var onShare: (() -> Void)?

    @State private var isShowingShareFlow = false

    var body: some View {
        VStack(spacing: 0) {
            navigationBar

            ScrollView(showsIndicators: false) {
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

                    if record.distanceMeters < Self.minimumRecordedDistanceMeters {
                        HStack(spacing: 0) {
                            errorCharacterImage
                                .resizable()
                                .scaledToFit()
                                .frame(width: 48, height: 48)
                                .accessibilityHidden(true)

                            Text("100미터 미만의 러닝은 기록되지 않습니다.")
                                .font(AppTheme.Typography.font(size: 13, weight: .semibold))
                                .foregroundStyle(AppTheme.Colors.textSecondary)
                                .multilineTextAlignment(.center)
                        }
                        .padding(.horizontal, 24)
                        .accessibilityIdentifier("short-run-recording-notice")
                    }

                    if !isSaving, let saveErrorMessage {
                        VStack(spacing: 8) {
                            Text(saveErrorMessage)
                                .font(AppTheme.Typography.font(size: 13, weight: .semibold))
                                .foregroundStyle(AppTheme.Colors.danger)
                                .multilineTextAlignment(.center)

                            if let onRetry {
                                Button("다시 저장") {
                                    onRetry()
                                }
                                .font(AppTheme.Typography.font(size: 14, weight: .bold))
                                .foregroundStyle(AppTheme.Colors.primary)
                            }
                        }
                        .padding(.horizontal, 24)
                    }

                    PrimaryButton(title: "완료", isLoading: isSaving, isDisabled: isSaving) {
                        onDone()
                    }
                    .padding(.horizontal, AppTheme.Layout.horizontalPadding)
                    .padding(.bottom, 34)
                }
                .background(Color.white)
            }
        }
        .background(Color.white)
        .fullScreenCover(isPresented: $isShowingShareFlow) {
            RunShareCameraView(record: record)
        }
    }

    private var navigationBar: some View {
        HStack(spacing: 0) {
            Button(action: onDone) {
                Image(systemName: "arrow.left")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(AppTheme.Colors.textPrimary)
                    .frame(width: 44, height: 44)
                    .contentShape(Rectangle())
            }
            .accessibilityLabel("뒤로가기")

            Spacer()

            Text("기록")
                .font(AppTheme.Typography.title2)
                .foregroundStyle(AppTheme.Colors.textPrimary)

            Spacer()

            Button {
                if let onShare {
                    onShare()
                } else {
                    isShowingShareFlow = true
                }
            } label: {
                Image(systemName: "square.and.arrow.up")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(AppTheme.Colors.textPrimary)
                    .frame(width: 44, height: 44)
                    .contentShape(Rectangle())
            }
            .accessibilityLabel("러닝 기록 공유")
        }
        .padding(.horizontal, 12)
        .frame(height: 60)
        .background(Color.white)
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

    private static let minimumRecordedDistanceMeters: Double = 100

    private var errorCharacterImage: Image {
        guard let imageURL = Bundle.main.url(forResource: "error", withExtension: "png"),
              let image = UIImage(contentsOfFile: imageURL.path)
        else {
            return Image(systemName: "info.circle.fill")
        }
        return Image(uiImage: image)
    }
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
