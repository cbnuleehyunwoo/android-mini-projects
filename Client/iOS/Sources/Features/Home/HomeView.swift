import CoreLocation
import SwiftUI
#if os(iOS)
import UIKit
#endif

struct HomeView: View {
    let nickname: String
    let team: RunningTeam?
    let teamProgress: HomeTeamProgress?
    let onCreateTeam: () -> Void
    let onJoinTeam: () -> Void
    let onOpenTeam: () -> Void
    let onOpenMyPage: () -> Void
    let onStartRunning: () -> Void
    @StateObject private var runningPermissionRequester = RunningStartPermissionRequester()
    @State private var isShowingStartDialog = false

    var body: some View {
        VStack(spacing: 0) {
            HomeHeaderView(nickname: nickname, onOpenMyPage: onOpenMyPage)
                .padding(.trailing, 20)

            TeamStatusCard(team: team, teamProgress: teamProgress, onCreateTeam: onCreateTeam, onJoinTeam: onJoinTeam, onOpenTeam: onOpenTeam)
                .padding(.horizontal, HomeLayout.contentHorizontalPadding)

            ZStack(alignment: .bottom) {
                HomeMapView()
                    .frame(maxWidth: .infinity)
                    .frame(maxHeight: .infinity)
                    .padding(.horizontal, HomeLayout.contentHorizontalPadding)

                if runningPermissionRequester.isStartButtonVisible {
                    Button {
                        runningPermissionRequester.requestStart {
                            isShowingStartDialog = true
                        }
                    } label: {
                        Text("시작")
                            .font(AppTheme.Typography.font(size: 24, weight: .bold))
                            .foregroundStyle(.white)
                            .frame(width: 100, height: 100)
                            .background(AppTheme.Colors.primary)
                            .clipShape(Circle())
                    }
                    .padding(.bottom, 16)
                }
            }
            .padding(.top, 8)
            .frame(maxHeight: .infinity)
        }
        .background(Color.white)
        .onReceive(NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification)) { _ in
            runningPermissionRequester.refreshAuthorizationStatus()
        }
        .overlay {
            if isShowingStartDialog {
                RunpamineConfirmationDialog(
                    title: "러닝 시작",
                    message: "러닝을 시작하시겠습니까?",
                    dismissText: "취소",
                    confirmText: "시작",
                    onDismiss: {
                        isShowingStartDialog = false
                    },
                    onConfirm: {
                        isShowingStartDialog = false
                        onStartRunning()
                    }
                )
            }

        }
    }

}

struct HomeTeamProgress: Equatable {
    let completedMemberCount: Int
    let totalMemberCount: Int
}

private enum HomeLayout {
    static let contentHorizontalPadding: CGFloat = 20
}

@MainActor
private final class RunningStartPermissionRequester: NSObject, ObservableObject {
    @Published private(set) var authorizationStatus: CLAuthorizationStatus = .notDetermined

    var isStartButtonVisible: Bool {
        switch authorizationStatus {
        case .authorizedWhenInUse, .authorizedAlways:
            return true
        default:
            return false
        }
    }

    private let manager = CLLocationManager()
    private var onAuthorized: (() -> Void)?

    override init() {
        super.init()
        manager.delegate = self
        authorizationStatus = manager.authorizationStatus
    }

    func refreshAuthorizationStatus() {
        authorizationStatus = manager.authorizationStatus
    }

    func requestStart(onAuthorized: @escaping () -> Void) {
        self.onAuthorized = onAuthorized
        authorizationStatus = manager.authorizationStatus

        switch authorizationStatus {
        case .authorizedWhenInUse, .authorizedAlways:
            onAuthorized()
            self.onAuthorized = nil
        default:
            self.onAuthorized = nil
        }
    }
}

extension RunningStartPermissionRequester: CLLocationManagerDelegate {
    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            authorizationStatus = manager.authorizationStatus

            switch authorizationStatus {
            case .authorizedWhenInUse, .authorizedAlways:
                onAuthorized?()
                onAuthorized = nil
            case .denied, .restricted:
                onAuthorized = nil
            case .notDetermined:
                break
            @unknown default:
                isShowingPermissionDialog = true
                onAuthorized = nil
            }
        }
    }
}

private struct HomeHeaderView: View {
    let nickname: String
    let onOpenMyPage: () -> Void

    var body: some View {
        HStack(spacing: 1) {
            Image("app_logo_face")
                .resizable()
                .scaledToFit()
                .frame(width: 90, height: 90)
                .padding(.trailing, -12)
                .accessibilityHidden(true)

            VStack(alignment: .leading, spacing: 0) {
                Text("안녕하세요")
                    .font(AppTheme.Typography.font(size: 12, weight: .medium))
                    .foregroundStyle(Color(red: 0.45, green: 0.48, blue: 0.56))
                Text("\(nickname) 님")
                    .font(AppTheme.Typography.font(size: 17, weight: .bold))
                    .foregroundStyle(Color(red: 0.06, green: 0.09, blue: 0.16))
                    .lineLimit(1)
                    .minimumScaleFactor(0.75)
            }

            Spacer()

            Button(action: onOpenMyPage) {
                Image(systemName: "person")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundStyle(AppTheme.Colors.textPrimary)
                    .frame(width: 36, height: 36)
            }
            .accessibilityLabel("마이페이지")
        }
    }
}

private struct TeamStatusCard: View {
    let team: RunningTeam?
    let teamProgress: HomeTeamProgress?
    let onCreateTeam: () -> Void
    let onJoinTeam: () -> Void
    let onOpenTeam: () -> Void

    var body: some View {
        HStack(alignment: .center, spacing: 8) {
            VStack(alignment: .leading, spacing: 0) {
                if let team {
                    Text(team.name)
                        .font(AppTheme.Typography.font(size: 17, weight: .bold))
                        .foregroundStyle(.white)
                        .lineLimit(1)
                        .minimumScaleFactor(0.75)

                    Text("오늘 달린 인원 \(teamProgressText(for: team))")
                        .font(AppTheme.Typography.font(size: 13, weight: .medium))
                        .foregroundStyle(.white.opacity(0.72))
                        .lineLimit(1)
                        .minimumScaleFactor(0.82)
                        .padding(.top, 6)

                    Button(action: onOpenTeam) {
                        Text("팀 정보보기")
                            .font(AppTheme.Typography.font(size: 12, weight: .bold))
                            .foregroundStyle(.white)
                            .frame(width: 88, height: 30)
                            .overlay {
                                Capsule()
                                    .stroke(.white, lineWidth: 1.5)
                            }
                    }
                    .padding(.top, 7)
                } else {
                    Text("참여한 팀이 없어요!")
                        .font(AppTheme.Typography.font(size: 17, weight: .bold))
                        .foregroundStyle(.white)
                    Text("팀에 참여하면 함께 달릴 수 있어요")
                        .font(AppTheme.Typography.font(size: 13, weight: .medium))
                        .foregroundStyle(.white.opacity(0.72))
                        .lineLimit(1)
                        .minimumScaleFactor(0.82)
                        .padding(.top, 6)

                    HStack(spacing: 8) {
                        Button(action: onCreateTeam) {
                            Text("팀 생성하기")
                                .font(AppTheme.Typography.font(size: 12, weight: .bold))
                                .foregroundStyle(AppTheme.Colors.primary)
                                .frame(width: 86, height: 30)
                                .background(.white)
                                .clipShape(Capsule())
                        }

                        Button(action: onJoinTeam) {
                            Text("팀 참가하기")
                                .font(AppTheme.Typography.font(size: 12, weight: .bold))
                                .foregroundStyle(.white)
                                .frame(width: 86, height: 30)
                                .overlay {
                                    Capsule()
                                        .stroke(.white, lineWidth: 1.5)
                                }
                        }
                    }
                    .padding(.top, 7)
                }
            }

            Spacer(minLength: 0)

            Image("ic_team")
                .resizable()
                .scaledToFit()
                .frame(width: 32, height: 32)
                .accessibilityHidden(true)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(AppTheme.Colors.primary)
        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
    }

    private func teamProgressText(for team: RunningTeam) -> String {
        guard let teamProgress else {
            return "- / -"
        }

        return "\(teamProgress.completedMemberCount) / \(teamProgress.totalMemberCount)"
    }
}

#Preview {
    HomeView(
        nickname: "러너",
        team: nil,
        teamProgress: nil,
        onCreateTeam: {},
        onJoinTeam: {},
        onOpenTeam: {},
        onOpenMyPage: {},
        onStartRunning: {}
    )
}
