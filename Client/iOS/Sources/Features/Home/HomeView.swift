import CoreLocation
import SwiftUI
#if os(iOS)
import UIKit
#endif

struct HomeView: View {
    let nickname: String
    let team: RunningTeam?
    let onCreateTeam: () -> Void
    let onJoinTeam: () -> Void
    let onOpenMyPage: () -> Void
    let onStartRunning: () -> Void
    @StateObject private var runningPermissionRequester = RunningStartPermissionRequester()
    @State private var isShowingStartDialog = false

    var body: some View {
        VStack(spacing: 0) {
            HomeHeaderView(nickname: nickname, onOpenMyPage: onOpenMyPage)
                .padding(.trailing, 20)

            TeamStatusCard(team: team, onCreateTeam: onCreateTeam, onJoinTeam: onJoinTeam)
                .padding(.horizontal, 20)

            ZStack(alignment: .bottom) {
                HomeMapView()
                    .frame(maxWidth: .infinity)
                    .frame(maxHeight: .infinity)
                    .padding(.horizontal, 8)

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
                .padding(.bottom, -32)
            }
            .padding(.top, 8)
            .padding(.bottom, 55)
            .frame(maxHeight: .infinity)
        }
        .background(Color.white)
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

            if runningPermissionRequester.isShowingPermissionDialog {
                RunpamineConfirmationDialog(
                    title: "위치 권한 필요",
                    message: "러닝을 시작하려면 위치 권한이 필요해요.",
                    dismissText: "취소",
                    confirmText: "설정",
                    onDismiss: {
                        runningPermissionRequester.dismissPermissionDialog()
                    },
                    onConfirm: {
                        runningPermissionRequester.openAppSettings()
                    }
                )
            }
        }
    }

}

@MainActor
private final class RunningStartPermissionRequester: NSObject, ObservableObject {
    @Published var isShowingPermissionDialog = false
    @Published private(set) var authorizationStatus: CLAuthorizationStatus = .notDetermined

    private let manager = CLLocationManager()
    private var onAuthorized: (() -> Void)?

    override init() {
        super.init()
        manager.delegate = self
        authorizationStatus = manager.authorizationStatus
    }

    func requestStart(onAuthorized: @escaping () -> Void) {
        self.onAuthorized = onAuthorized
        authorizationStatus = manager.authorizationStatus

        guard CLLocationManager.locationServicesEnabled() else {
            isShowingPermissionDialog = true
            return
        }

        switch authorizationStatus {
        case .notDetermined:
            manager.requestWhenInUseAuthorization()
        case .authorizedWhenInUse, .authorizedAlways:
            onAuthorized()
            self.onAuthorized = nil
        case .denied, .restricted:
            isShowingPermissionDialog = true
        @unknown default:
            isShowingPermissionDialog = true
        }
    }

    func dismissPermissionDialog() {
        isShowingPermissionDialog = false
        onAuthorized = nil
    }

    func openAppSettings() {
        isShowingPermissionDialog = false
        onAuthorized = nil

        #if os(iOS)
        guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
        UIApplication.shared.open(url)
        #endif
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
                isShowingPermissionDialog = true
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

            VStack(alignment: .leading, spacing: 2) {
                Text("안녕하세요, \(nickname)님!")
                    .font(AppTheme.Typography.font(size: 12, weight: .semibold))
                    .foregroundStyle(AppTheme.Colors.textPrimary)
                Text("오늘은 뛰기 좋은 날씨네요!")
                    .font(AppTheme.Typography.caption1)
                    .foregroundStyle(AppTheme.Colors.textPrimary)
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
    let onCreateTeam: () -> Void
    let onJoinTeam: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            if let team {
                Text(team.name)
                    .font(AppTheme.Typography.font(size: 14, weight: .bold))
                    .foregroundStyle(.white)
                    .lineLimit(1)
            } else {
                Text("참여한 팀이 없어요!")
                    .font(AppTheme.Typography.font(size: 14, weight: .bold))
                    .foregroundStyle(.white)
                Text("팀에 참여하거나 팀을 만들어보세요.")
                    .font(AppTheme.Typography.font(size: 12, weight: .semibold))
                    .foregroundStyle(.white)
            }

            if team == nil {
                HStack(spacing: 48) {
                    Button("팀 생성하기 >", action: onCreateTeam)
                    Button("팀 참가하기 >", action: onJoinTeam)
                }
                .font(AppTheme.Typography.font(size: 12, weight: .black))
                .foregroundStyle(AppTheme.Colors.success)
                .padding(.top, 6)
            }
        }
        .padding(.horizontal, 40)
        .frame(maxWidth: .infinity, alignment: .leading)
        .frame(height: 88)
        .background(AppTheme.Colors.primary)
        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
    }
}

#Preview {
    HomeView(
        nickname: "러너",
        team: nil,
        onCreateTeam: {},
        onJoinTeam: {},
        onOpenMyPage: {},
        onStartRunning: {}
    )
}
