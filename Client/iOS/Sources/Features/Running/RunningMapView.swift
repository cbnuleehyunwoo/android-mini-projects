import CoreLocation
import MapKit
import SwiftUI

struct RunningMapView: View {
    let route: [CLLocationCoordinate2D]
    let latestLocation: CLLocation?
    var focusesVisibleUpperArea = false

    @State private var cameraPosition: MapCameraPosition = .automatic

    var body: some View {
        Map(position: $cameraPosition) {
            if latestLocation != nil {
                UserAnnotation()
            }

            if route.count >= 2 {
                MapPolyline(coordinates: route)
                    .stroke(AppTheme.Colors.primary, lineWidth: 5)
            }
        }
        .mapStyle(.standard(elevation: .flat))
        .ignoresSafeArea()
        .onAppear {
            updateCamera()
        }
        .onChange(of: latestLocation?.coordinate.latitude) { _, _ in
            updateCamera()
        }
        .onChange(of: latestLocation?.coordinate.longitude) { _, _ in
            updateCamera()
        }
        .onChange(of: route.count) { _, _ in
            updateCamera()
        }
    }

    private func updateCamera() {
        if let coordinate = latestLocation?.coordinate {
            let latitudeDelta = 0.0025
            let adjustedCenter = CLLocationCoordinate2D(
                latitude: focusesVisibleUpperArea ? coordinate.latitude - latitudeDelta * 0.28 : coordinate.latitude,
                longitude: coordinate.longitude
            )
            cameraPosition = .region(
                MKCoordinateRegion(
                    center: adjustedCenter,
                    span: MKCoordinateSpan(latitudeDelta: latitudeDelta, longitudeDelta: 0.0025)
                )
            )
            return
        }

        guard !route.isEmpty else { return }
        cameraPosition = Self.cameraPosition(fitting: route)
    }

    static func cameraPosition(fitting coordinates: [CLLocationCoordinate2D]) -> MapCameraPosition {
        guard !coordinates.isEmpty else { return .automatic }

        let minLatitude = coordinates.map(\.latitude).min() ?? coordinates[0].latitude
        let maxLatitude = coordinates.map(\.latitude).max() ?? coordinates[0].latitude
        let minLongitude = coordinates.map(\.longitude).min() ?? coordinates[0].longitude
        let maxLongitude = coordinates.map(\.longitude).max() ?? coordinates[0].longitude
        let center = CLLocationCoordinate2D(
            latitude: (minLatitude + maxLatitude) / 2,
            longitude: (minLongitude + maxLongitude) / 2
        )

        return .region(
            MKCoordinateRegion(
                center: center,
                span: MKCoordinateSpan(
                    latitudeDelta: max(0.0025, (maxLatitude - minLatitude) * 1.5),
                    longitudeDelta: max(0.0025, (maxLongitude - minLongitude) * 1.5)
                )
            )
        )
    }
}
