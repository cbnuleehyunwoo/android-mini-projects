import CoreLocation
import MapKit
import SwiftUI
#if os(iOS)
import UIKit
#endif

struct HomeMapView: View {
    @StateObject private var locationProvider = HomeLocationProvider()
    @State private var cameraPosition: MapCameraPosition = .region(
        MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 37.5665, longitude: 126.9780),
            span: MKCoordinateSpan(latitudeDelta: 0.012, longitudeDelta: 0.012)
        )
    )
    @State private var currentRegion = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 37.5665, longitude: 126.9780),
        span: MKCoordinateSpan(latitudeDelta: 0.012, longitudeDelta: 0.012)
    )

    var body: some View {
        Group {
            if locationProvider.hasLocationAuthorization {
                mapContent
            } else {
                HomeLocationPermissionRequestView(buttonTitle: locationProvider.permissionButtonTitle) {
                    locationProvider.requestAuthorization()
                }
            }
        }
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
    }

    private var mapContent: some View {
        ZStack(alignment: .leading) {
            Map(position: $cameraPosition) {
                UserAnnotation()
            }
            .mapStyle(.standard(elevation: .flat))
            .onMapCameraChange { context in
                currentRegion = context.region
            }
            .onAppear {
                locationProvider.requestLocation()
            }
            .onChange(of: locationProvider.currentLocation?.coordinate.latitude) { _, _ in
                centerOnUser()
            }
            .onChange(of: locationProvider.currentLocation?.coordinate.longitude) { _, _ in
                centerOnUser()
            }

            VStack(spacing: 10) {
                mapControl(assetIcon: "icon_locate_fix") {
                    locationProvider.requestLocation()
                    centerOnUser()
                }
                mapControl(systemIcon: "plus.magnifyingglass") {
                    zoom(by: 0.55)
                }
                mapControl(systemIcon: "minus.magnifyingglass") {
                    zoom(by: 1.65)
                }
            }
            .padding(.leading, 13)
        }
    }

    private func mapControl(systemIcon: String? = nil, assetIcon: String? = nil, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Group {
                if let assetIcon {
                    Image(assetIcon)
                        .resizable()
                        .renderingMode(.original)
                        .scaledToFit()
                        .frame(width: 20, height: 20)
                } else if let systemIcon {
                    Image(systemName: systemIcon)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundStyle(AppTheme.Colors.textPrimary)
                }
            }
            .frame(width: 38, height: 38)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
            .shadow(color: .black.opacity(0.10), radius: 5, x: 0, y: 2)
        }
    }

    private func centerOnUser() {
        guard let coordinate = locationProvider.currentLocation?.coordinate else { return }
        let region = MKCoordinateRegion(
            center: coordinate,
            span: MKCoordinateSpan(latitudeDelta: 0.006, longitudeDelta: 0.006)
        )
        currentRegion = region
        cameraPosition = .region(region)
    }

    private func zoom(by scale: CLLocationDegrees) {
        let region = MKCoordinateRegion(
            center: currentRegion.center,
            span: MKCoordinateSpan(
                latitudeDelta: max(0.0015, min(0.08, currentRegion.span.latitudeDelta * scale)),
                longitudeDelta: max(0.0015, min(0.08, currentRegion.span.longitudeDelta * scale))
            )
        )
        currentRegion = region
        cameraPosition = .region(region)
    }
}

private struct HomeLocationPermissionRequestView: View {
    let buttonTitle: String
    let onRequestPermission: () -> Void

    var body: some View {
        VStack(spacing: 22) {
            Text("주변 지도를 보려면 위치 권한이 필요해요.")
                .font(AppTheme.Typography.font(size: 16, weight: .medium))
                .foregroundStyle(.black)
                .multilineTextAlignment(.center)

            Button(action: onRequestPermission) {
                Text(buttonTitle)
                    .font(AppTheme.Typography.font(size: 14, weight: .bold))
                    .foregroundStyle(.white)
                    .frame(width: 170, height: 50)
                    .background(AppTheme.Colors.primary)
                    .clipShape(Capsule())
            }
            .buttonStyle(.plain)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.white)
        .accessibilityElement(children: .combine)
    }
}

@MainActor
private final class HomeLocationProvider: NSObject, ObservableObject {
    @Published private(set) var currentLocation: CLLocation?
    @Published private(set) var authorizationStatus: CLAuthorizationStatus = .notDetermined

    private let manager = CLLocationManager()

    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
        manager.distanceFilter = 10
        authorizationStatus = manager.authorizationStatus
    }

    var hasLocationAuthorization: Bool {
        #if os(iOS)
        authorizationStatus == .authorizedWhenInUse || authorizationStatus == .authorizedAlways
        #else
        authorizationStatus == .authorizedAlways
        #endif
    }

    var permissionButtonTitle: String {
        switch authorizationStatus {
        case .denied, .restricted:
            return "위치 권한 설정하기"
        default:
            return "위치 권한 허용하기"
        }
    }

    func requestAuthorization() {
        switch authorizationStatus {
        case .notDetermined:
            manager.requestWhenInUseAuthorization()
        case .denied, .restricted:
            openAppSettings()
        case .authorizedWhenInUse, .authorizedAlways:
            requestLocation()
        @unknown default:
            break
        }
    }

    func requestLocation() {
        authorizationStatus = manager.authorizationStatus

        switch authorizationStatus {
        case .notDetermined:
            break
        case .authorizedWhenInUse, .authorizedAlways:
            manager.requestLocation()
        case .denied, .restricted:
            break
        @unknown default:
            break
        }
    }

    private func openAppSettings() {
        #if os(iOS)
        guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
        UIApplication.shared.open(url)
        #endif
    }
}

extension HomeLocationProvider: CLLocationManagerDelegate {
    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            authorizationStatus = manager.authorizationStatus

            if hasLocationAuthorization {
                manager.requestLocation()
            }
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        Task { @MainActor in
            currentLocation = locations.last
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
    }
}
