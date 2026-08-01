import AVFoundation
import ImageIO
import MapKit
import Photos
import PhotosUI
import SwiftUI
import UIKit

struct RunShareCameraView: View {
    let record: RunningRecord

    @Environment(\.dismiss) private var dismiss
    @StateObject private var cameraController = RunShareCameraController()
    @State private var selectedImage: UIImage?
    @State private var photoItem: PhotosPickerItem?
    @State private var isPresentingEditor = false
    @State private var isLoadingPhoto = false
    @State private var errorMessage: String?

    var body: some View {
        ZStack {
            CameraPreview(session: cameraController.session)
                .ignoresSafeArea()
                .opacity(cameraController.isPreviewAvailable ? 1 : 0)

            LinearGradient(
                colors: [Color.black.opacity(0.48), .clear, Color.black.opacity(0.82)],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            cameraControls
                .safeAreaPadding(.top)
                .safeAreaPadding(.bottom)
        }
        .onAppear {
            cameraController.start()
        }
        .onDisappear {
            cameraController.stop()
        }
        .onChange(of: cameraController.capturedImage) { _, image in
            guard let image else { return }
            selectedImage = image
            cameraController.consumeCapturedImage()
            isPresentingEditor = true
        }
        .onChange(of: photoItem) { _, item in
            loadPhoto(item)
        }
        .onChange(of: isPresentingEditor) { _, isPresented in
            if !isPresented {
                cameraController.start()
            }
        }
        .fullScreenCover(isPresented: $isPresentingEditor, onDismiss: {
            selectedImage = nil
        }) {
            if let selectedImage {
                RunShareEditorView(
                    record: record,
                    photo: selectedImage,
                    onRetake: {
                        isPresentingEditor = false
                        cameraController.start()
                    }
                )
            }
        }
    }

    private var cameraControls: some View {
        VStack(spacing: 0) {
            HStack(spacing: 0) {
                Button(action: dismiss.callAsFunction) {
                    Image(systemName: "xmark")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(.white)
                        .frame(width: 44, height: 44)
                }
                .accessibilityLabel("닫기")

                Spacer()

                Text("사진 촬영")
                    .font(AppTheme.Typography.title2)
                    .foregroundStyle(.white)

                Spacer()

                HStack(spacing: 8) {
                    Button(action: cameraController.toggleFlash) {
                        Image(systemName: cameraController.isFlashEnabled ? "bolt.fill" : "bolt.slash")
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundStyle(.white)
                            .frame(width: 36, height: 44)
                    }
                    .disabled(!cameraController.isPreviewAvailable)
                    .accessibilityLabel(cameraController.isFlashEnabled ? "플래시 끄기" : "플래시 켜기")

                    Button(action: cameraController.switchCamera) {
                        Image(systemName: "camera.rotate")
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundStyle(.white)
                            .frame(width: 36, height: 44)
                    }
                    .disabled(!cameraController.isPreviewAvailable)
                    .accessibilityLabel("카메라 전환")
                }
                .frame(width: 80)
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)

            Spacer()

            if !cameraController.isPreviewAvailable {
                VStack(spacing: 12) {
                    Image(systemName: "camera.fill")
                        .font(.system(size: 42, weight: .medium))
                    Text(cameraController.statusMessage)
                        .font(AppTheme.Typography.font(size: 16, weight: .bold))
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                }
                .foregroundStyle(.white.opacity(0.9))
            }

            if let errorMessage = errorMessage ?? cameraController.captureErrorMessage {
                Text(errorMessage)
                    .font(AppTheme.Typography.font(size: 13, weight: .medium))
                    .foregroundStyle(Color(red: 1, green: 0.45, blue: 0.45))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 24)
                    .padding(.top, 18)
            }

            Spacer()

            if isLoadingPhoto {
                ProgressView()
                    .tint(.white)
                    .padding(.bottom, 18)
            }

            HStack(alignment: .center, spacing: 0) {
                PhotosPicker(
                    selection: $photoItem,
                    matching: .images,
                    photoLibrary: .shared()
                ) {
                    cameraControlLabel(title: "앨범", systemImage: "photo.on.rectangle")
                }
                .buttonStyle(.plain)
                .disabled(isLoadingPhoto)

                Spacer()

                Button(action: cameraController.capturePhoto) {
                    Circle()
                        .fill(.white)
                        .frame(width: 68, height: 68)
                        .overlay {
                            Circle()
                                .stroke(.black.opacity(0.22), lineWidth: 3)
                        }
                        .frame(width: 82, height: 82)
                        .overlay {
                            Circle()
                                .stroke(.white, lineWidth: 4)
                        }
                }
                .disabled(!cameraController.isPreviewAvailable || isLoadingPhoto)
                .accessibilityLabel("사진 촬영")

                Spacer()

                Button(action: cameraController.switchCamera) {
                    cameraControlLabel(title: "전환", systemImage: "camera.rotate")
                }
                .buttonStyle(.plain)
                .disabled(!cameraController.isPreviewAvailable)
            }
            .padding(.horizontal, 28)
            .padding(.bottom, 28)
        }
    }

    private func cameraControlLabel(title: String, systemImage: String) -> some View {
        VStack(spacing: 6) {
            Image(systemName: systemImage)
                .font(.system(size: 22, weight: .semibold))
            Text(title)
                .font(AppTheme.Typography.font(size: 12, weight: .bold))
        }
        .foregroundStyle(.white)
        .frame(width: 64, height: 64)
    }

    private func loadPhoto(_ item: PhotosPickerItem?) {
        guard let item else { return }

        errorMessage = nil
        isLoadingPhoto = true

        Task { @MainActor in
            do {
                guard let data = try await item.loadTransferable(type: Data.self) else {
                    throw RunShareImageError.invalidImage
                }

                let image = try await RunShareImageDecoder.decodeAsync(data)
                guard !Task.isCancelled else { return }

                selectedImage = image
                photoItem = nil
                isLoadingPhoto = false

                // Let PhotosPicker finish dismissing before presenting the editor cover.
                DispatchQueue.main.async {
                    isPresentingEditor = true
                }
            } catch {
                photoItem = nil
                isLoadingPhoto = false
                errorMessage = "사진을 불러오지 못했어요. 다시 선택해주세요."
            }
        }
    }
}

private struct RunShareEditorView: View {
    let record: RunningRecord
    let photo: UIImage
    let onRetake: () -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var selectedLayout: RunShareLayout = .current
    @State private var selectedTab: RunShareEditorTab = .layout
    @State private var selectedStickers: Set<RunShareSticker> = []
    @State private var dataOffset: CGSize = .zero
    @State private var routeOffset: CGSize = .zero
    @State private var stickerOffsets: [RunShareSticker: CGSize] = [:]
    @State private var renderedImage: UIImage?
    @State private var isShowingShareSheet = false
    @State private var saveMessage: String?

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            VStack(spacing: 0) {
                editorToolbar

                ScrollView(showsIndicators: false) {
                    VStack(spacing: 18) {
                        RunShareCanvas(
                            record: record,
                            photo: photo,
                            layout: selectedLayout,
                            visibleStickers: selectedStickers,
                            dataOffset: $dataOffset,
                            routeOffset: $routeOffset,
                            stickerOffsets: $stickerOffsets
                        )
                        .aspectRatio(9.0 / 16.0, contentMode: .fit)
                        .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
                        .padding(.horizontal, 18)

                        editorPanel
                    }
                    .padding(.bottom, 20)
                }
            }
        }
        .sheet(isPresented: $isShowingShareSheet) {
            if let renderedImage {
                RunShareActivityView(image: renderedImage)
                    .presentationDetents([.medium, .large])
            }
        }
    }

    private var editorToolbar: some View {
        HStack(spacing: 16) {
            Button(action: dismiss.callAsFunction) {
                Image(systemName: "xmark")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(.white)
                    .frame(width: 36, height: 36)
            }
            .accessibilityLabel("닫기")

            Spacer()

            Button("다시 찍기", action: onRetake)
                .font(AppTheme.Typography.font(size: 15, weight: .bold))
                .foregroundStyle(.white)

            Button("저장", action: saveToPhotos)
                .font(AppTheme.Typography.font(size: 15, weight: .bold))
                .foregroundStyle(.white)
        }
        .padding(.horizontal, 18)
        .padding(.top, 8)
        .frame(height: 58)
        .safeAreaPadding(.top)
    }

    private var editorPanel: some View {
        VStack(spacing: 16) {
            Picker("편집 종류", selection: $selectedTab) {
                ForEach(RunShareEditorTab.allCases) { tab in
                    Text(tab.title).tag(tab)
                }
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, 18)

            if selectedTab == .layout {
                layoutOptions
            } else {
                stickerOptions
            }

            if let saveMessage {
                Text(saveMessage)
                    .font(AppTheme.Typography.font(size: 13, weight: .medium))
                    .foregroundStyle(.white.opacity(0.75))
            }

            Text("러닝 데이터·경로·스티커는 카드에서 드래그해 이동할 수 있어요.")
                .font(AppTheme.Typography.font(size: 12, weight: .medium))
                .foregroundStyle(.white.opacity(0.58))
                .padding(.horizontal, 18)

            Button(action: share) {
                Text("공유하기")
                    .font(AppTheme.Typography.font(size: 17, weight: .bold))
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 56)
                    .background(AppTheme.Colors.primary)
                    .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            }
            .padding(.horizontal, 18)
        }
        .padding(.vertical, 18)
        .background(Color(red: 0.08, green: 0.08, blue: 0.08))
    }

    private var layoutOptions: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                ForEach(RunShareLayout.allCases) { layout in
                    Button {
                        selectedLayout = layout
                    } label: {
                        VStack(spacing: 8) {
                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                                .fill(Color.black.opacity(0.82))
                                .frame(width: 86, height: 86)
                                .overlay {
                                    Image(systemName: layout.systemImage)
                                        .font(.system(size: 24, weight: .semibold))
                                        .foregroundStyle(.white)
                                }
                                .overlay {
                                    RoundedRectangle(cornerRadius: 12, style: .continuous)
                                        .stroke(
                                            selectedLayout == layout ? AppTheme.Colors.primary : Color.clear,
                                            lineWidth: 2
                                        )
                                }
                            Text(layout.title)
                                .font(AppTheme.Typography.font(size: 12, weight: .medium))
                                .foregroundStyle(.white.opacity(0.75))
                        }
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 18)
        }
    }

    private var stickerOptions: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                ForEach(RunShareSticker.allCases) { sticker in
                    Button {
                        if selectedStickers.contains(sticker) {
                            selectedStickers.remove(sticker)
                        } else {
                            selectedStickers.insert(sticker)
                        }
                    } label: {
                        VStack(spacing: 8) {
                            Image(sticker.assetName)
                                .resizable()
                                .scaledToFit()
                                .padding(8)
                                .frame(width: 86, height: 86)
                                .background(Color.white.opacity(0.08))
                                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                                .overlay {
                                    RoundedRectangle(cornerRadius: 12, style: .continuous)
                                        .stroke(
                                            selectedStickers.contains(sticker) ? AppTheme.Colors.primary : Color.clear,
                                            lineWidth: 2
                                        )
                                }
                            Text(sticker.title)
                                .font(AppTheme.Typography.font(size: 12, weight: .medium))
                                .foregroundStyle(.white.opacity(0.75))
                        }
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 18)
        }
    }

    @MainActor
    private func share() {
        guard let image = renderImage() else { return }
        renderedImage = image
        isShowingShareSheet = true
    }

    @MainActor
    private func saveToPhotos() {
        guard let image = renderImage() else {
            saveMessage = "이미지를 준비하지 못했어요. 다시 시도해주세요."
            return
        }

        PHPhotoLibrary.requestAuthorization(for: .addOnly) { status in
            guard status == .authorized || status == .limited else {
                DispatchQueue.main.async {
                    saveMessage = "사진 보관함 권한이 필요해요."
                }
                return
            }

            PHPhotoLibrary.shared().performChanges {
                PHAssetChangeRequest.creationRequestForAsset(from: image)
            } completionHandler: { saved, _ in
                DispatchQueue.main.async {
                    saveMessage = saved ? "사진을 저장했어요." : "사진을 저장하지 못했어요. 다시 시도해주세요."
                }
            }
        }
    }

    @MainActor
    private func renderImage() -> UIImage? {
        let canvas = RunShareCanvas(
            record: record,
            photo: photo,
            layout: selectedLayout,
            visibleStickers: selectedStickers,
            dataOffset: .constant(dataOffset),
            routeOffset: .constant(routeOffset),
            stickerOffsets: .constant(stickerOffsets)
        )
        let renderer = ImageRenderer(content: canvas.frame(width: 1080, height: 1920))
        renderer.scale = 1
        guard let image = renderer.uiImage else { return nil }
        return Self.makeOpaqueImage(image)
    }

    private static func makeOpaqueImage(_ image: UIImage) -> UIImage {
        let format = UIGraphicsImageRendererFormat()
        format.opaque = true
        format.scale = image.scale

        return UIGraphicsImageRenderer(size: image.size, format: format).image { _ in
            image.draw(in: CGRect(origin: .zero, size: image.size))
        }
    }
}

private struct RunShareCanvas: View {
    let record: RunningRecord
    let photo: UIImage
    let layout: RunShareLayout
    let visibleStickers: Set<RunShareSticker>
    @Binding var dataOffset: CGSize
    @Binding var routeOffset: CGSize
    @Binding var stickerOffsets: [RunShareSticker: CGSize]
    @GestureState private var dataDragTranslation: CGSize = .zero
    @GestureState private var routeDragTranslation: CGSize = .zero

    var body: some View {
        GeometryReader { geometry in
            ZStack {
                Image(uiImage: photo)
                    .resizable()
                    .scaledToFill()
                    .frame(width: geometry.size.width, height: geometry.size.height)
                    .clipped()

                LinearGradient(
                    colors: [Color.black.opacity(0.72), Color.black.opacity(0.08), Color.black.opacity(0.78)],
                    startPoint: .top,
                    endPoint: .bottom
                )

                ZStack(alignment: .bottomLeading) {
                    shareDataContent
                        .padding(30)
                        .offset(
                            x: dataOffset.width + dataDragTranslation.width,
                            y: dataOffset.height + dataDragTranslation.height
                        )
                        .highPriorityGesture(
                            DragGesture(minimumDistance: 4)
                                .updating($dataDragTranslation) { value, state, _ in
                                    state = value.translation
                                }
                                .onEnded { value in
                                    dataOffset = CGSize(
                                        width: dataOffset.width + value.translation.width,
                                        height: dataOffset.height + value.translation.height
                                    )
                                }
                        )
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)

                if layout.includesRoute {
                    ZStack(alignment: .top) {
                        RunShareRouteMap(route: record.route)
                            .frame(
                                width: max(1, geometry.size.width - 60),
                                height: geometry.size.height * layout.routeHeightRatio
                            )
                            .contentShape(Rectangle())
                            .offset(
                                x: routeOffset.width + routeDragTranslation.width,
                                y: routeOffset.height + routeDragTranslation.height
                            )
                            .highPriorityGesture(
                                DragGesture(minimumDistance: 4)
                                    .updating($routeDragTranslation) { value, state, _ in
                                        state = value.translation
                                    }
                                    .onEnded { value in
                                        routeOffset = CGSize(
                                            width: routeOffset.width + value.translation.width,
                                            height: routeOffset.height + value.translation.height
                                        )
                                    }
                            )
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
                    .padding(.top, 30)
                }

                ForEach(RunShareSticker.allCases.filter(visibleStickers.contains)) { sticker in
                    RunShareStickerView(
                        sticker: sticker,
                        width: geometry.size.width * sticker.widthRatio,
                        offset: stickerOffsetBinding(for: sticker)
                    )
                    .rotationEffect(.degrees(sticker.rotationDegrees))
                    .position(
                        x: geometry.size.width * sticker.defaultX,
                        y: geometry.size.height * sticker.defaultY
                    )
                }

                VStack {
                    Spacer()
                    HStack {
                        Spacer()
                        Image("runpamine_share_logo")
                            .resizable()
                            .scaledToFit()
                            .frame(width: geometry.size.width * 0.34)
                            .shadow(color: .black.opacity(0.35), radius: 5, y: 2)
                            .padding(.trailing, 12)
                            .padding(.bottom, 12)
                    }
                }
            }
        }
        .aspectRatio(9.0 / 16.0, contentMode: .fit)
        .background(Color.black)
    }

    private func stickerOffsetBinding(for sticker: RunShareSticker) -> Binding<CGSize> {
        Binding(
            get: { stickerOffsets[sticker] ?? .zero },
            set: { stickerOffsets[sticker] = $0 }
        )
    }

    @ViewBuilder
    private var shareDataContent: some View {
        switch layout {
        case .distance, .routeDistance:
            distanceContent
        case .current, .routeCurrent:
            currentLayoutContent
        }
    }

    private var distanceContent: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(record.distanceKilometers.formatted(.number.precision(.fractionLength(2))))
                .font(AppTheme.Typography.font(size: 66, weight: .black))
                .foregroundStyle(.white)
            Text("KILOMETERS")
                .font(AppTheme.Typography.font(size: 18, weight: .bold))
                .tracking(2)
                .foregroundStyle(.white.opacity(0.9))
        }
    }

    private var currentLayoutContent: some View {
        VStack(alignment: .leading, spacing: 0) {
            distanceContent

            HStack(spacing: 28) {
                shareMetric(title: "TIME", value: RunningMetricFormatter.duration(record.elapsedTime))
                shareMetric(title: "PACE", value: RunningMetricFormatter.pace(record.averagePaceSecondsPerKilometer))
                shareMetric(title: "KCAL", value: "\(record.estimatedCalories)")
            }
            .padding(.top, 22)
        }
    }

    private func shareMetric(title: String, value: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(AppTheme.Typography.font(size: 12, weight: .medium))
                .tracking(1)
                .foregroundStyle(.white.opacity(0.7))
            Text(value)
                .font(AppTheme.Typography.font(size: 21, weight: .bold))
                .foregroundStyle(.white)
        }
    }
}

private struct RunShareRouteMap: View {
    let route: [RunningCoordinate]

    var body: some View {
        GeometryReader { geometry in
            ZStack {
                if route.count >= 2 {
                    routePath(in: geometry.size)
                        .stroke(.white, style: StrokeStyle(lineWidth: 5, lineCap: .round, lineJoin: .round))
                } else {
                    VStack(spacing: 8) {
                        Image(systemName: "map")
                            .font(.system(size: 24, weight: .semibold))
                        Text("러닝 루트 없음")
                            .font(AppTheme.Typography.font(size: 13, weight: .bold))
                    }
                    .foregroundStyle(.white.opacity(0.72))
                }
            }
        }
    }

    private func routePath(in size: CGSize) -> Path {
        let projectedRoute = route.map { MKMapPoint($0.coordinate) }
        let minX = projectedRoute.map(\.x).min() ?? 0
        let maxX = projectedRoute.map(\.x).max() ?? 1
        let minY = projectedRoute.map(\.y).min() ?? 0
        let maxY = projectedRoute.map(\.y).max() ?? 1
        let routeWidth = CGFloat(max(maxX - minX, 1))
        let routeHeight = CGFloat(max(maxY - minY, 1))
        let inset: CGFloat = 24
        let drawableSize = CGSize(
            width: max(1, size.width - inset * 2),
            height: max(1, size.height - inset * 2)
        )
        let scale = min(drawableSize.width / routeWidth, drawableSize.height / routeHeight)
        let fittedWidth = routeWidth * scale
        let fittedHeight = routeHeight * scale
        let originX = (size.width - fittedWidth) / 2
        let originY = (size.height - fittedHeight) / 2

        var path = Path()
        for (index, projectedPoint) in projectedRoute.enumerated() {
            let point = CGPoint(
                x: originX + CGFloat(projectedPoint.x - minX) * scale,
                y: originY + CGFloat(maxY - projectedPoint.y) * scale
            )

            if index == 0 {
                path.move(to: point)
            } else {
                path.addLine(to: point)
            }
        }
        return path
    }
}

private struct RunShareStickerView: View {
    let sticker: RunShareSticker
    let width: CGFloat
    @Binding var offset: CGSize
    @GestureState private var dragTranslation: CGSize = .zero

    var body: some View {
        Image(sticker.assetName)
            .resizable()
            .scaledToFit()
            .frame(width: width)
            .offset(
                x: offset.width + dragTranslation.width,
                y: offset.height + dragTranslation.height
            )
            .contentShape(Rectangle())
            .highPriorityGesture(
                DragGesture(minimumDistance: 4)
                    .updating($dragTranslation) { value, state, _ in
                        state = value.translation
                    }
                    .onEnded { value in
                        offset = CGSize(
                            width: offset.width + value.translation.width,
                            height: offset.height + value.translation.height
                        )
                    }
            )
    }
}

private enum RunShareEditorTab: String, CaseIterable, Identifiable {
    case layout
    case sticker

    var id: String { rawValue }

    var title: String {
        switch self {
        case .layout: "러닝 데이터 레이아웃"
        case .sticker: "스티커"
        }
    }
}

private enum RunShareLayout: String, CaseIterable, Identifiable {
    case distance
    case current
    case routeDistance
    case routeCurrent

    var id: String { rawValue }

    var title: String {
        switch self {
        case .distance: "러닝 거리"
        case .current: "현재 레이아웃"
        case .routeDistance: "러닝 루트 + 러닝 거리"
        case .routeCurrent: "러닝 루트 + 현재 레이아웃"
        }
    }

    var systemImage: String {
        switch self {
        case .distance: "textformat.size.larger"
        case .current: "rectangle.3.group"
        case .routeDistance: "map"
        case .routeCurrent: "map.fill"
        }
    }

    var includesRoute: Bool {
        switch self {
        case .distance, .current: false
        case .routeDistance, .routeCurrent: true
        }
    }

    var routeHeightRatio: CGFloat {
        switch self {
        case .routeDistance: 0.42
        case .routeCurrent: 0.38
        case .distance, .current: 0
        }
    }
}

private enum RunShareSticker: String, CaseIterable, Hashable, Identifiable {
    case pamin
    case cheetahPamin
    case surprisedPamin
    case hamburgerPamin
    case handstandPamin

    var id: String { rawValue }

    var title: String {
        switch self {
        case .pamin: "파민"
        case .cheetahPamin: "치타파민"
        case .surprisedPamin: "놀란 파민"
        case .hamburgerPamin: "햄버거 파민"
        case .handstandPamin: "물구나무 파민"
        }
    }

    var assetName: String {
        switch self {
        case .pamin: "pamin_sticker"
        case .cheetahPamin: "cheetah_pamin_sticker"
        case .surprisedPamin: "surprised_pamin_sticker"
        case .hamburgerPamin: "hamburger_pamin_sticker"
        case .handstandPamin: "handstand_pamin_sticker"
        }
    }

    var widthRatio: CGFloat {
        switch self {
        case .pamin: 0.24
        case .cheetahPamin: 0.44
        case .surprisedPamin: 0.26
        case .hamburgerPamin: 0.40
        case .handstandPamin: 0.46
        }
    }

    var defaultX: CGFloat {
        switch self {
        case .pamin: 0.84
        case .cheetahPamin: 0.76
        case .surprisedPamin: 0.84
        case .hamburgerPamin: 0.78
        case .handstandPamin: 0.72
        }
    }

    var defaultY: CGFloat {
        switch self {
        case .pamin: 0.77
        case .cheetahPamin: 0.75
        case .surprisedPamin: 0.73
        case .hamburgerPamin: 0.76
        case .handstandPamin: 0.73
        }
    }

    var rotationDegrees: Double {
        switch self {
        case .pamin: 8
        case .cheetahPamin, .surprisedPamin, .hamburgerPamin, .handstandPamin: 0
        }
    }
}

private struct CameraPreview: UIViewRepresentable {
    let session: AVCaptureSession

    func makeUIView(context: Context) -> CameraPreviewView {
        CameraPreviewView(session: session)
    }

    func updateUIView(_ uiView: CameraPreviewView, context: Context) {
        uiView.previewLayer.session = session
    }
}

private final class CameraPreviewView: UIView {
    override class var layerClass: AnyClass {
        AVCaptureVideoPreviewLayer.self
    }

    var previewLayer: AVCaptureVideoPreviewLayer {
        layer as! AVCaptureVideoPreviewLayer
    }

    init(session: AVCaptureSession) {
        super.init(frame: .zero)
        previewLayer.session = session
        previewLayer.videoGravity = .resizeAspectFill
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

private final class RunShareCameraController: NSObject, ObservableObject, AVCapturePhotoCaptureDelegate {
    let session = AVCaptureSession()

    @Published private(set) var isPreviewAvailable = false
    @Published private(set) var statusMessage = "카메라를 준비하고 있어요."
    @Published private(set) var captureErrorMessage: String?
    @Published private(set) var capturedImage: UIImage?
    @Published private(set) var isFlashEnabled = false

    private let sessionQueue = DispatchQueue(label: "com.runpamine.run-share-camera")
    private let photoOutput = AVCapturePhotoOutput()
    private var videoInput: AVCaptureDeviceInput?
    private var currentPosition: AVCaptureDevice.Position = .back
    private var isConfigured = false

    func start() {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            configureAndStart()
        case .notDetermined:
            DispatchQueue.main.async {
                self.statusMessage = "카메라 권한을 확인하고 있어요."
            }
            AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
                guard let self else { return }
                if granted {
                    self.configureAndStart()
                } else {
                    self.publishUnavailable("카메라 권한이 없어 촬영할 수 없어요.\n앨범에서 사진을 선택해주세요.")
                }
            }
        case .denied, .restricted:
            publishUnavailable("카메라 권한이 없어 촬영할 수 없어요.\n앨범에서 사진을 선택해주세요.")
        @unknown default:
            publishUnavailable("카메라를 사용할 수 없어요.\n앨범에서 사진을 선택해주세요.")
        }
    }

    func stop() {
        sessionQueue.async { [weak self] in
            guard let self, self.session.isRunning else { return }
            self.session.stopRunning()
        }
    }

    func toggleFlash() {
        guard isPreviewAvailable else { return }
        isFlashEnabled.toggle()
    }

    func switchCamera() {
        sessionQueue.async { [weak self] in
            guard let self,
                  self.isConfigured,
                  let currentInput = self.videoInput
            else { return }

            let nextPosition: AVCaptureDevice.Position = self.currentPosition == .back ? .front : .back
            guard let device = AVCaptureDevice.default(
                .builtInWideAngleCamera,
                for: .video,
                position: nextPosition
            ),
                  let nextInput = try? AVCaptureDeviceInput(device: device)
            else { return }

            self.session.beginConfiguration()
            self.session.removeInput(currentInput)

            if self.session.canAddInput(nextInput) {
                self.session.addInput(nextInput)
                self.videoInput = nextInput
                self.currentPosition = nextPosition
                self.session.commitConfiguration()
                DispatchQueue.main.async {
                    self.isFlashEnabled = false
                }
            } else {
                self.session.addInput(currentInput)
                self.session.commitConfiguration()
            }
        }
    }

    func capturePhoto() {
        sessionQueue.async { [weak self] in
            guard let self, self.isConfigured, self.session.isRunning else { return }

            let settings = AVCapturePhotoSettings()
            if self.isFlashEnabled,
               self.currentPosition == .back,
               self.photoOutput.supportedFlashModes.contains(.on) {
                settings.flashMode = .on
            } else {
                settings.flashMode = .off
            }

            DispatchQueue.main.async {
                self.captureErrorMessage = nil
            }
            self.photoOutput.capturePhoto(with: settings, delegate: self)
        }
    }

    func consumeCapturedImage() {
        capturedImage = nil
    }

    func photoOutput(
        _ output: AVCapturePhotoOutput,
        didFinishProcessingPhoto photo: AVCapturePhoto,
        error: Error?
    ) {
        guard error == nil,
              let data = photo.fileDataRepresentation(),
              let image = RunShareImageDecoder.decode(data)
        else {
            DispatchQueue.main.async {
                self.captureErrorMessage = "사진을 촬영하지 못했어요. 다시 시도해주세요."
            }
            return
        }

        DispatchQueue.main.async {
            self.capturedImage = image
        }
    }

    private func configureAndStart() {
        sessionQueue.async { [weak self] in
            guard let self else { return }

            if self.isConfigured {
                if !self.session.isRunning {
                    self.session.startRunning()
                }
                self.publishAvailable()
                return
            }

            guard let device = AVCaptureDevice.default(
                .builtInWideAngleCamera,
                for: .video,
                position: self.currentPosition
            ),
                  let input = try? AVCaptureDeviceInput(device: device)
            else {
                self.publishUnavailable("이 시뮬레이터에서는 카메라를 사용할 수 없어요.\n앨범에서 사진을 선택해주세요.")
                return
            }

            self.session.beginConfiguration()
            self.session.sessionPreset = .photo

            guard self.session.canAddInput(input), self.session.canAddOutput(self.photoOutput) else {
                self.session.commitConfiguration()
                self.publishUnavailable("카메라를 준비하지 못했어요.\n앨범에서 사진을 선택해주세요.")
                return
            }

            self.session.addInput(input)
            self.session.addOutput(self.photoOutput)
            self.session.commitConfiguration()
            self.videoInput = input
            self.isConfigured = true
            self.session.startRunning()
            self.publishAvailable()
        }
    }

    private func publishAvailable() {
        DispatchQueue.main.async {
            self.isPreviewAvailable = true
            self.statusMessage = ""
        }
    }

    private func publishUnavailable(_ message: String) {
        DispatchQueue.main.async {
            self.isPreviewAvailable = false
            self.statusMessage = message
        }
    }
}

private enum RunShareImageDecoder {
    static func decode(_ data: Data) -> UIImage? {
        autoreleasepool {
            guard let source = CGImageSourceCreateWithData(data as CFData, nil) else {
                return nil
            }

            let options: [CFString: Any] = [
                kCGImageSourceCreateThumbnailFromImageAlways: true,
                kCGImageSourceCreateThumbnailWithTransform: true,
                kCGImageSourceThumbnailMaxPixelSize: 2048,
            ]

            guard let image = CGImageSourceCreateThumbnailAtIndex(source, 0, options as CFDictionary) else {
                return nil
            }
            return UIImage(cgImage: image)
        }
    }

    static func decodeAsync(_ data: Data) async throws -> UIImage {
        try await withCheckedThrowingContinuation { continuation in
            DispatchQueue.global(qos: .userInitiated).async {
                guard let image = decode(data) else {
                    continuation.resume(throwing: RunShareImageError.invalidImage)
                    return
                }
                continuation.resume(returning: image)
            }
        }
    }
}

private struct RunShareActivityView: UIViewControllerRepresentable {
    let image: UIImage

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: [image], applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

private enum RunShareImageError: Error {
    case invalidImage
}
