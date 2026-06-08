import CoreLocation
import MapKit
import SwiftUI

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
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
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

@MainActor
private final class HomeLocationProvider: NSObject, ObservableObject {
    @Published private(set) var currentLocation: CLLocation?

    private let manager = CLLocationManager()

    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
        manager.distanceFilter = 10
    }

    func requestLocation() {
        switch manager.authorizationStatus {
        case .notDetermined:
            manager.requestWhenInUseAuthorization()
        case .authorizedAlways:
            manager.requestLocation()
        case .denied, .restricted:
            break
        @unknown default:
            break
        }

        #if os(iOS)
        if manager.authorizationStatus == .authorizedWhenInUse {
            manager.requestLocation()
        }
        #endif
    }
}

extension HomeLocationProvider: CLLocationManagerDelegate {
    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            #if os(iOS)
            if manager.authorizationStatus == .authorizedWhenInUse || manager.authorizationStatus == .authorizedAlways {
                manager.requestLocation()
            }
            #else
            if manager.authorizationStatus == .authorizedAlways {
                manager.requestLocation()
            }
            #endif
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
