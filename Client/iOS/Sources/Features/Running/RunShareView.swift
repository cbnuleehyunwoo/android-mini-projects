import AVFoundation
import ImageIO
import MapKit
import Photos
import PhotosUI
import SwiftUI
import UIKit

private enum RunShareCanvasMetrics {
    static let aspectRatio: CGFloat = 9.0 / 16.15
}

private enum RunShareScreenInsets {
    @MainActor
    static var top: CGFloat {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .filter { $0.activationState == .foregroundActive }
            .flatMap(\.windows)
            .first(where: \.isKeyWindow)?
            .safeAreaInsets.top ?? 0
    }
}

struct RunShareCameraView: View {
    let record: RunningRecord

    @Environment(\.dismiss) private var dismiss
    @StateObject private var cameraController = RunShareCameraController()
    @State private var selectedImage: UIImage?
    @State private var photoItem: PhotosPickerItem?
    @State private var isShowingPhotoPicker = false
    @State private var isPresentingEditor = false
    @State private var editorPresentationTask: Task<Void, Never>?
    @State private var editorTopSafeAreaInset: CGFloat = 0
    @State private var shouldReturnToRecord = false
    @State private var isLoadingPhoto = false
    @State private var errorMessage: String?
    @State private var lastZoomScale: CGFloat = 1

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
        .simultaneousGesture(cameraZoomGesture)
        .onAppear {
            cacheEditorTopSafeAreaInset()
            cameraController.start()
        }
        .onDisappear {
            lastZoomScale = 1
            cameraController.stop()
        }
        .onChange(of: cameraController.capturedImage) { _, image in
            guard let image else { return }
            cacheEditorTopSafeAreaInset()
            selectedImage = image
            cameraController.consumeCapturedImage()
            isPresentingEditor = true
        }
        .onChange(of: photoItem) { _, item in
            loadPhoto(item)
        }
        .onChange(of: isShowingPhotoPicker) { _, isPresented in
            guard !isPresented else { return }
            scheduleEditorPresentationIfReady()
        }
        .onChange(of: isPresentingEditor) { _, isPresented in
            if !isPresented {
                cameraController.start()
            }
        }
        .fullScreenCover(isPresented: $isPresentingEditor, onDismiss: {
            editorPresentationTask?.cancel()
            editorPresentationTask = nil
            selectedImage = nil
            if shouldReturnToRecord {
                shouldReturnToRecord = false
                dismiss()
            }
        }) {
            if let selectedImage {
                RunShareEditorView(
                    record: record,
                    photo: selectedImage,
                    topSafeAreaInset: editorTopSafeAreaInset,
                    onSaved: {
                        shouldReturnToRecord = true
                        isPresentingEditor = false
                    }
                )
            }
        }
    }

    private var cameraZoomGesture: some Gesture {
        MagnificationGesture()
            .onChanged { scale in
                cameraController.zoom(by: scale / lastZoomScale)
                lastZoomScale = scale
            }
            .onEnded { _ in
                lastZoomScale = 1
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
                Button {
                    cacheEditorTopSafeAreaInset()
                    isShowingPhotoPicker = true
                } label: {
                    cameraControlLabel(title: "앨범", systemImage: "photo.on.rectangle")
                }
                .buttonStyle(.plain)
                .disabled(isLoadingPhoto)
                .photosPicker(
                    isPresented: $isShowingPhotoPicker,
                    selection: $photoItem,
                    matching: .images,
                    photoLibrary: .shared()
                )

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
                scheduleEditorPresentationIfReady()
            } catch {
                photoItem = nil
                isLoadingPhoto = false
                errorMessage = "사진을 불러오지 못했어요. 다시 선택해주세요."
            }
        }
    }

    @MainActor
    private func scheduleEditorPresentationIfReady() {
        guard selectedImage != nil, !isShowingPhotoPicker else { return }

        editorPresentationTask?.cancel()
        editorPresentationTask = Task { @MainActor in
            try? await Task.sleep(for: .milliseconds(350))
            guard !Task.isCancelled,
                  selectedImage != nil,
                  !isShowingPhotoPicker else { return }

            isPresentingEditor = true
            editorPresentationTask = nil
        }
    }

    @MainActor
    private func cacheEditorTopSafeAreaInset() {
        let inset = RunShareScreenInsets.top
        guard inset > 0 else { return }
        editorTopSafeAreaInset = inset
    }
}

private struct RunShareEditorView: View {
    let record: RunningRecord
    let photo: UIImage
    let topSafeAreaInset: CGFloat
    let onSaved: () -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var selectedLayout: RunShareLayout = .current
    @State private var selectedTab: RunShareEditorTab = .layout
    @State private var selectedStickers: Set<RunShareSticker> = []
    @State private var selectedSticker: RunShareSticker?
    @State private var selectedElement: RunShareElement?
    @State private var elementTransforms: [RunShareElement: RunShareCanvasItemTransform] = [:]
    @State private var stickerTransforms: [RunShareSticker: RunShareCanvasItemTransform] = [:]
    @State private var darkElements: Set<RunShareElement> = []
    @State private var darkTextStickers: Set<RunShareSticker> = []
    @State private var canvasSize = CGSize(width: 360, height: 640)
    @State private var renderedImage: UIImage?
    @State private var isShowingShareSheet = false
    @State private var isSaving = false
    @State private var saveErrorMessage: String?
    @State private var regionName: String?

    var body: some View {
        ZStack(alignment: .top) {
            Color.black.ignoresSafeArea()

            ScrollView(showsIndicators: false) {
                VStack(spacing: 18) {
                    RunShareCanvas(
                        record: record,
                        photo: photo,
                        layout: selectedLayout,
                        regionName: regionName,
                        visibleStickers: selectedStickers,
                        elementTransforms: $elementTransforms,
                        stickerTransforms: $stickerTransforms,
                        darkElements: $darkElements,
                        darkTextStickers: $darkTextStickers,
                        selectedElement: $selectedElement,
                        selectedSticker: $selectedSticker,
                        showsEditingControls: true,
                        onDeleteElement: removeElement,
                        onDeleteSticker: removeSticker,
                        onSizeChange: { canvasSize = $0 }
                    )
                    .aspectRatio(RunShareCanvasMetrics.aspectRatio, contentMode: .fit)
                    .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))

                    editorPanel
                }
                .padding(.bottom, 20)
            }
            .padding(.top, topSafeAreaInset)

            editorToolbar
                .padding(.top, topSafeAreaInset)
                .zIndex(30)
        }
        .ignoresSafeArea(.container, edges: .top)
        .task(id: record.id) {
            await loadRegionName()
        }
        .sheet(isPresented: $isShowingShareSheet) {
            if let renderedImage {
                RunShareActivityView(image: renderedImage)
                    .presentationDetents([.medium, .large])
            }
        }
        .alert("사진을 저장하지 못했어요", isPresented: saveErrorIsPresented) {
            Button("확인", role: .cancel) {}
        } message: {
            Text(saveErrorMessage ?? "잠시 후 다시 시도해주세요.")
        }
    }

    private var editorToolbar: some View {
        HStack {
            Button(action: dismiss.callAsFunction) {
                Image(systemName: "xmark")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundStyle(.white)
                    .frame(width: 44, height: 44)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel("닫기")

            Spacer()

            Button(action: save) {
                Group {
                    if isSaving {
                        ProgressView()
                            .tint(.white)
                    } else {
                        Text("저장")
                            .font(AppTheme.Typography.font(size: 17, weight: .bold))
                            .foregroundStyle(.white)
                    }
                }
                .frame(minWidth: 56, minHeight: 44)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .disabled(isSaving)
            .accessibilityLabel(isSaving ? "사진 저장 중" : "사진 저장")
        }
        .padding(.horizontal, 16)
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
        ScrollView(.vertical, showsIndicators: false) {
            LazyVGrid(
                columns: Array(repeating: GridItem(.flexible(), spacing: 10), count: 4),
                spacing: 12
            ) {
                ForEach(RunShareLayout.allCases) { layout in
                    Button {
                        selectedLayout = layout
                        if !layout.includesRoute, selectedElement == .route {
                            selectedElement = nil
                        }
                    } label: {
                        VStack(spacing: 8) {
                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                                .fill(Color.black.opacity(0.82))
                                .frame(maxWidth: .infinity)
                                .frame(height: 76)
                                .overlay {
                                    if let iconText = layout.iconText {
                                        Text(iconText)
                                            .font(AppTheme.Typography.font(size: 22, weight: .extraBold))
                                            .foregroundStyle(.white)
                                    } else {
                                        Image(systemName: layout.systemImage)
                                            .font(.system(size: 24, weight: .semibold))
                                            .foregroundStyle(.white)
                                    }
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
                                .lineLimit(1)
                                .minimumScaleFactor(0.7)
                        }
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 18)
        }
        .frame(maxHeight: 190)
    }

    private var stickerOptions: some View {
        ScrollView(.vertical, showsIndicators: false) {
            LazyVGrid(
                columns: Array(repeating: GridItem(.flexible(), spacing: 10), count: 4),
                spacing: 12
            ) {
                ForEach(RunShareSticker.allCases, id: \.self) { sticker in
                    Button {
                        if selectedStickers.contains(sticker) {
                            if selectedSticker == sticker {
                                removeSticker(sticker)
                            } else {
                                selectedSticker = sticker
                            }
                        } else {
                            selectedStickers.insert(sticker)
                            selectedSticker = sticker
                        }
                    } label: {
                        VStack(spacing: 8) {
                            stickerOptionPreview(sticker)
                                .padding(8)
                                .frame(maxWidth: .infinity)
                                .frame(height: 76)
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
                                .lineLimit(1)
                                .minimumScaleFactor(0.7)
                        }
                    }
                    .buttonStyle(.plain)
                    .disabled(sticker == .region && regionName == nil)
                    .opacity(sticker == .region && regionName == nil ? 0.4 : 1)
                }
            }
            .padding(.horizontal, 18)
        }
        .frame(maxHeight: 190)
    }

    @ViewBuilder
    private func stickerOptionPreview(_ sticker: RunShareSticker) -> some View {
        switch sticker {
        case .date:
            Image(systemName: "calendar")
                .font(.system(size: 24, weight: .semibold))
                .foregroundStyle(.white)
        case .region:
            RunShareLocationIcon(color: .white)
                .frame(width: 24, height: 29)
        default:
            if let assetName = sticker.assetName {
                Image(assetName)
                    .resizable()
                    .scaledToFit()
            }
        }
    }

    @MainActor
    private func share() {
        guard let image = renderImage() else { return }
        renderedImage = image
        isShowingShareSheet = true
    }

    @MainActor
    private func save() {
        guard !isSaving else { return }
        guard let image = renderImage() else {
            saveErrorMessage = "편집한 사진을 만들지 못했어요."
            return
        }

        isSaving = true
        Task {
            do {
                try await RunSharePhotoLibrary.save(image)
                isSaving = false
                onSaved()
            } catch RunSharePhotoLibraryError.accessDenied {
                isSaving = false
                saveErrorMessage = "사진 보관함 추가 권한을 허용한 뒤 다시 시도해주세요."
            } catch {
                isSaving = false
                saveErrorMessage = "갤러리에 저장하지 못했어요. 잠시 후 다시 시도해주세요."
            }
        }
    }

    private var saveErrorIsPresented: Binding<Bool> {
        Binding(
            get: { saveErrorMessage != nil },
            set: { isPresented in
                if !isPresented {
                    saveErrorMessage = nil
                }
            }
        )
    }

    @MainActor
    private func renderImage() -> UIImage? {
        let canvas = RunShareCanvas(
            record: record,
            photo: photo,
            layout: selectedLayout,
            regionName: regionName,
            visibleStickers: selectedStickers,
            elementTransforms: .constant(elementTransforms),
            stickerTransforms: .constant(stickerTransforms),
            darkElements: .constant(darkElements),
            darkTextStickers: .constant(darkTextStickers),
            selectedElement: .constant(selectedElement),
            selectedSticker: .constant(selectedSticker),
            showsEditingControls: false,
            onDeleteElement: nil,
            onDeleteSticker: nil,
            onSizeChange: nil
        )
        let logicalSize = canvasSize.width > 0 ? canvasSize : CGSize(width: 360, height: 640)
        let renderer = ImageRenderer(
            content: canvas.frame(width: logicalSize.width, height: logicalSize.height)
        )
        renderer.scale = 1080 / logicalSize.width
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

    private func removeSticker(_ sticker: RunShareSticker) {
        selectedStickers.remove(sticker)
        stickerTransforms[sticker] = nil
        darkTextStickers.remove(sticker)
        if selectedSticker == sticker {
            selectedSticker = nil
        }
    }

    private func removeElement(_ element: RunShareElement) {
        elementTransforms[element] = nil
        darkElements.remove(element)
        if element == .route {
            switch selectedLayout {
            case .routeDistance:
                selectedLayout = .distance
            case .routeCurrent:
                selectedLayout = .current
            case .distance, .current:
                break
            }
        }
        if selectedElement == element {
            selectedElement = nil
        }
    }

    @MainActor
    private func loadRegionName() async {
        regionName = nil

        guard let coordinate = record.route.first?.coordinate else {
            removeSticker(.region)
            return
        }

        do {
            let placemarks = try await CLGeocoder().reverseGeocodeLocation(
                CLLocation(latitude: coordinate.latitude, longitude: coordinate.longitude)
            )
            guard let placemark = placemarks.first else {
                removeSticker(.region)
                return
            }
            guard let cityOrCounty = cityOrCountyName(from: [
                placemark.subAdministrativeArea,
                placemark.locality,
                placemark.administrativeArea,
                placemark.subLocality,
                placemark.name
            ]) else {
                removeSticker(.region)
                return
            }

            regionName = cityOrCounty
        } catch {
            removeSticker(.region)
        }
    }

    private func cityOrCountyName(from names: [String?]) -> String? {
        let normalizedNames = names.compactMap { name in
            let normalizedName = name?.trimmingCharacters(in: .whitespacesAndNewlines)
            return normalizedName?.isEmpty == false ? normalizedName : nil
        }

        for name in normalizedNames {
            if let range = name.range(of: #"[가-힣]+(?:시|군)"#, options: .regularExpression) {
                return String(name[range])
            }
        }

        return normalizedNames.first { name in
            !name.hasSuffix("읍") &&
            !name.hasSuffix("면") &&
            !name.hasSuffix("리") &&
            !name.hasSuffix("동")
        }
    }
}

private enum RunSharePhotoLibrary {
    static func save(_ image: UIImage) async throws {
        let currentStatus = PHPhotoLibrary.authorizationStatus(for: .addOnly)
        let authorizationStatus: PHAuthorizationStatus

        if currentStatus == .notDetermined {
            authorizationStatus = await PHPhotoLibrary.requestAuthorization(for: .addOnly)
        } else {
            authorizationStatus = currentStatus
        }

        guard authorizationStatus == .authorized || authorizationStatus == .limited else {
            throw RunSharePhotoLibraryError.accessDenied
        }

        try await PHPhotoLibrary.shared().performChanges {
            PHAssetChangeRequest.creationRequestForAsset(from: image)
        }
    }
}

private enum RunSharePhotoLibraryError: Error {
    case accessDenied
}

private struct RunShareCanvas: View {
    let record: RunningRecord
    let photo: UIImage
    let layout: RunShareLayout
    let regionName: String?
    let visibleStickers: Set<RunShareSticker>
    @Binding var elementTransforms: [RunShareElement: RunShareCanvasItemTransform]
    @Binding var stickerTransforms: [RunShareSticker: RunShareCanvasItemTransform]
    @Binding var darkElements: Set<RunShareElement>
    @Binding var darkTextStickers: Set<RunShareSticker>
    @Binding var selectedElement: RunShareElement?
    @Binding var selectedSticker: RunShareSticker?
    let showsEditingControls: Bool
    let onDeleteElement: ((RunShareElement) -> Void)?
    let onDeleteSticker: ((RunShareSticker) -> Void)?
    let onSizeChange: ((CGSize) -> Void)?

    /// 로고와 러닝 거리 사이 간격.
    private let shareLogoSpacing: CGFloat = 2

    var body: some View {
        GeometryReader { geometry in
            ZStack {
                Image(uiImage: photo)
                    .resizable()
                    .scaledToFill()
                    .frame(width: geometry.size.width, height: geometry.size.height)
                    .clipped()
                    .contentShape(Rectangle())
                    .onTapGesture {
                        guard showsEditingControls else { return }
                        selectedElement = nil
                        selectedSticker = nil
                    }
                    .onAppear {
                        onSizeChange?(geometry.size)
                    }
                    .onChange(of: geometry.size) { _, size in
                        onSizeChange?(size)
                    }

                LinearGradient(
                    colors: [Color.black.opacity(0.72), Color.black.opacity(0.08), Color.black.opacity(0.78)],
                    startPoint: .top,
                    endPoint: .bottom
                )

                ForEach(activeElements) { element in
                    let size = elementSize(for: element, in: geometry.size)
                    let center = elementCenter(for: element, size: size, in: geometry.size)

                    RunShareElementView(
                        element: element,
                        layout: layout,
                        record: record,
                        regionName: regionName,
                        size: size,
                        center: center,
                        logoSize: shareLogoSize(in: geometry.size),
                        dataColor: darkElements.contains(.dataGroup) ? .black : .white,
                        routeColor: darkElements.contains(.route) ? .black : .white,
                        transform: elementTransformBinding(for: element),
                        isSelected: selectedElement == element,
                        showsEditingControls: showsEditingControls,
                        onSelect: {
                            selectedElement = element
                            selectedSticker = nil
                        },
                        onTapSelected: { toggleElementColor(element) },
                        onDelete: { onDeleteElement?(element) }
                    )
                    .position(x: center.x, y: center.y)
                    .zIndex(
                        selectedElement == element
                            ? 10
                            : element == .route ? 1 : 2
                    )
                }

                ForEach(RunShareSticker.allCases.filter(visibleStickers.contains)) { sticker in
                    let size = stickerSize(for: sticker, in: geometry.size)
                    let center = stickerCenter(for: sticker, size: size, in: geometry.size)

                    RunShareStickerView(
                        sticker: sticker,
                        record: record,
                        regionName: regionName,
                        textColor: darkTextStickers.contains(sticker) ? .black : .white,
                        size: size,
                        center: center,
                        transform: stickerTransformBinding(for: sticker),
                        isSelected: selectedSticker == sticker,
                        showsEditingControls: showsEditingControls,
                        onSelect: {
                            selectedSticker = sticker
                            selectedElement = nil
                        },
                        onTapSelected: { toggleStickerColor(sticker) },
                        onDelete: { onDeleteSticker?(sticker) }
                    )
                    .position(x: center.x, y: center.y)
                    .zIndex(selectedSticker == sticker ? 11 : 3)
                }
            }
        }
        .aspectRatio(RunShareCanvasMetrics.aspectRatio, contentMode: .fit)
        .background(Color.black)
        .coordinateSpace(name: RunShareCoordinateSpace.canvas)
    }

    private func elementTransformBinding(for element: RunShareElement) -> Binding<RunShareCanvasItemTransform> {
        Binding(
            get: { elementTransforms[element] ?? RunShareCanvasItemTransform() },
            set: { elementTransforms[element] = $0 }
        )
    }

    private func stickerTransformBinding(for sticker: RunShareSticker) -> Binding<RunShareCanvasItemTransform> {
        Binding(
            get: { stickerTransforms[sticker] ?? RunShareCanvasItemTransform() },
            set: { stickerTransforms[sticker] = $0 }
        )
    }

    private var activeElements: [RunShareElement] {
        layout.includesRoute ? [.dataGroup, .route] : [.dataGroup]
    }

    private func toggleElementColor(_ element: RunShareElement) {
        guard showsEditingControls else { return }
        if darkElements.contains(element) {
            darkElements.remove(element)
        } else {
            darkElements.insert(element)
        }
    }

    private func toggleStickerColor(_ sticker: RunShareSticker) {
        guard showsEditingControls, sticker.supportsColorToggle else { return }
        if darkTextStickers.contains(sticker) {
            darkTextStickers.remove(sticker)
        } else {
            darkTextStickers.insert(sticker)
        }
    }

    private func elementSize(for element: RunShareElement, in canvasSize: CGSize) -> CGSize {
        switch element {
        case .dataGroup:
            dataGroupSize(showsDetails: layout.showsDetails, in: canvasSize)
        case .route:
            routeElementSize(in: canvasSize)
        }
    }

    /// 러닝 데이터(거리·상세)와 그 위에 올라가는 런파민 로고를 하나의 이동/확대 단위로 묶은 크기.
    private func dataGroupSize(showsDetails: Bool, in canvasSize: CGSize) -> CGSize {
        let base = showsDetails
            ? detailedDataGroupSize(in: canvasSize)
            : distanceElementSize(in: canvasSize)
        let logo = shareLogoSize(in: canvasSize)
        return CGSize(
            width: max(base.width, logo.width),
            height: logo.height + shareLogoSpacing + base.height
        )
    }

    /// 로고 너비를 러닝 거리 라인("0.16 KM") 너비에 맞춰, 왼쪽 끝은 TIME·오른쪽 끝은 KM에 정렬되도록 한다.
    private func shareLogoSize(in canvasSize: CGSize) -> CGSize {
        let width = distanceLineWidth(in: canvasSize)
        let aspectRatio = UIImage(named: "runpamine_share_logo")
            .map { $0.size.height / max($0.size.width, 1) } ?? 0.4
        return CGSize(width: width, height: width * aspectRatio)
    }

    private func distanceLineWidth(in canvasSize: CGSize) -> CGFloat {
        let distanceFont = UIFont(name: "Pretendard-ExtraBold", size: 66)
            ?? UIFont.systemFont(ofSize: 66, weight: .heavy)
        let unitFont = UIFont(name: "Pretendard-Bold", size: 22)
            ?? UIFont.systemFont(ofSize: 22, weight: .bold)
        let distance = record.distanceKilometers.formatted(
            .number.precision(.fractionLength(2))
        )
        let distanceWidth = distance.size(withAttributes: [.font: distanceFont]).width
        let unitWidth = "KM".size(withAttributes: [.font: unitFont]).width
        return distanceWidth + 8 + unitWidth
    }

    private func routeElementSize(in canvasSize: CGSize) -> CGSize {
        let maxSize = CGSize(
            width: max(1, canvasSize.width - 60),
            height: canvasSize.height * RunShareElement.routeHeightRatio
        )
        guard record.route.count >= 2 else { return maxSize }

        let projectedRoute = record.route.map { MKMapPoint($0.coordinate) }
        let routeWidth = CGFloat(
            max(
                (projectedRoute.map(\.x).max() ?? 0) - (projectedRoute.map(\.x).min() ?? 0),
                1
            )
        )
        let routeHeight = CGFloat(
            max(
                (projectedRoute.map(\.y).max() ?? 0) - (projectedRoute.map(\.y).min() ?? 0),
                1
            )
        )
        let pathInset: CGFloat = 24
        let maximumDrawableSize = CGSize(
            width: max(1, maxSize.width - pathInset * 2),
            height: max(1, maxSize.height - pathInset * 2)
        )
        let scale = min(
            maximumDrawableSize.width / routeWidth,
            maximumDrawableSize.height / routeHeight
        )

        return CGSize(
            width: routeWidth * scale + pathInset * 2,
            height: routeHeight * scale + pathInset * 2
        )
    }

    private func detailedDataGroupSize(in canvasSize: CGSize) -> CGSize {
        let distanceSize = distanceElementSize(in: canvasSize)
        let dataSize = dataElementSize(in: canvasSize)
        return CGSize(
            width: max(distanceSize.width, dataSize.width),
            height: distanceSize.height + 11 + dataSize.height
        )
    }

    private func distanceElementSize(in canvasSize: CGSize) -> CGSize {
        let distanceFont = UIFont(name: "Pretendard-ExtraBold", size: 66)
            ?? UIFont.systemFont(ofSize: 66, weight: .heavy)
        let unitFont = UIFont(name: "Pretendard-Bold", size: 22)
            ?? UIFont.systemFont(ofSize: 22, weight: .bold)
        let distance = record.distanceKilometers.formatted(
            .number.precision(.fractionLength(2))
        )
        let distanceWidth = distance.size(withAttributes: [.font: distanceFont]).width
        let unitWidth = "KM".size(withAttributes: [.font: unitFont]).width
        let contentWidth = distanceWidth + 8 + unitWidth
        let contentHeight = max(distanceFont.lineHeight, unitFont.lineHeight)

        return fittedSelectionSize(
            contentWidth: contentWidth,
            contentHeight: contentHeight,
            in: canvasSize
        )
    }

    private func dataElementSize(in canvasSize: CGSize) -> CGSize {
        let titleFont = UIFont(name: "Pretendard-Medium", size: 12)
            ?? UIFont.systemFont(ofSize: 12, weight: .medium)
        let valueFont = UIFont(name: "Pretendard-Bold", size: 21)
            ?? UIFont.systemFont(ofSize: 21, weight: .bold)
        let titles = ["TIME", "PACE", "KCAL"]
        let values = [
            RunningMetricFormatter.duration(record.elapsedTime),
            RunningMetricFormatter.pace(record.averagePaceSecondsPerKilometer),
            "\(record.estimatedCalories)"
        ]
        let metricWidths = zip(titles, values).map { title, value in
            let titleWidth = title.size(withAttributes: [.font: titleFont]).width + CGFloat(max(title.count - 1, 0))
            let valueWidth = value.size(withAttributes: [.font: valueFont]).width
            return max(titleWidth, valueWidth)
        }
        let horizontalSpacing: CGFloat = 28
        let contentWidth = metricWidths.reduce(0, +) + horizontalSpacing * 2
        let contentHeight = titleFont.lineHeight + 4 + valueFont.lineHeight
        let horizontalPadding: CGFloat = 4
        let verticalPadding: CGFloat = 4

        return CGSize(
            width: min(contentWidth + horizontalPadding, max(canvasSize.width - 40, 1)),
            height: contentHeight + verticalPadding
        )
    }

    private func textElementSize(
        text: String,
        fontName: String,
        fontSize: CGFloat,
        fallbackWeight: UIFont.Weight,
        in canvasSize: CGSize
    ) -> CGSize {
        let font = UIFont(name: fontName, size: fontSize)
            ?? UIFont.systemFont(ofSize: fontSize, weight: fallbackWeight)
        let measuredSize = text.size(withAttributes: [.font: font])

        return fittedSelectionSize(
            contentWidth: measuredSize.width,
            contentHeight: font.lineHeight,
            in: canvasSize
        )
    }

    private func fittedSelectionSize(
        contentWidth: CGFloat,
        contentHeight: CGFloat,
        in canvasSize: CGSize
    ) -> CGSize {
        let selectionPadding: CGFloat = 4
        return CGSize(
            width: min(
                contentWidth.rounded(.up) + selectionPadding,
                max(canvasSize.width - 40, 1)
            ),
            height: contentHeight.rounded(.up) + selectionPadding
        )
    }

    private func elementCenter(
        for element: RunShareElement,
        size: CGSize,
        in canvasSize: CGSize
    ) -> CGPoint {
        switch element {
        case .route:
            return CGPoint(x: canvasSize.width / 2, y: 30 + size.height / 2)
        case .dataGroup:
            let fullGroupHeight = dataGroupSize(showsDetails: true, in: canvasSize).height
            let bottomReserve: CGFloat = 82
            let top = canvasSize.height - bottomReserve - fullGroupHeight
            let savedScale = elementTransforms[.dataGroup]?.scale ?? 1
            return CGPoint(
                x: 20 + size.width * savedScale / 2,
                y: top + size.height * savedScale / 2
            )
        }
    }

    private func stickerSize(for sticker: RunShareSticker, in canvasSize: CGSize) -> CGSize {
        switch sticker {
        case .date:
            let measuredSize = textElementSize(
                text: dateText,
                fontName: "Pretendard-Bold",
                fontSize: 19,
                fallbackWeight: .bold,
                in: canvasSize
            )
            return CGSize(
                width: min(measuredSize.width, canvasSize.width * 0.55),
                height: measuredSize.height
            )
        case .region:
            let font = UIFont(name: "Pretendard-Bold", size: 21)
                ?? UIFont.systemFont(ofSize: 21, weight: .bold)
            let textWidth = (regionName ?? "").size(withAttributes: [.font: font]).width
            let iconWidth: CGFloat = 18
            let spacing: CGFloat = 6
            return fittedSelectionSize(
                contentWidth: iconWidth + spacing + textWidth,
                contentHeight: max(font.lineHeight, iconWidth),
                in: canvasSize
            )
        default:
            let width = canvasSize.width * sticker.widthRatio
            return CGSize(width: width, height: width)
        }
    }

    private func stickerCenter(
        for sticker: RunShareSticker,
        size: CGSize,
        in canvasSize: CGSize
    ) -> CGPoint {
        switch sticker {
        case .date:
            return CGPoint(x: 20 + size.width / 2, y: canvasSize.height - 30)
        case .region:
            return CGPoint(
                x: canvasSize.width - 32 - size.width / 2,
                y: canvasSize.height * 0.40
            )
        default:
            return CGPoint(
                x: canvasSize.width * sticker.defaultX,
                y: canvasSize.height * sticker.defaultY
            )
        }
    }

    private var dateText: String {
        RunShareElementFormatters.date.string(from: record.startedAt)
    }
}

private struct RunShareElementView: View {
    let element: RunShareElement
    let layout: RunShareLayout
    let record: RunningRecord
    let regionName: String?
    let size: CGSize
    let center: CGPoint
    let logoSize: CGSize
    let dataColor: Color
    let routeColor: Color
    @Binding var transform: RunShareCanvasItemTransform
    let isSelected: Bool
    let showsEditingControls: Bool
    let onSelect: () -> Void
    let onTapSelected: () -> Void
    let onDelete: () -> Void

    var body: some View {
        RunShareTransformableView(
            contentSize: size,
            center: center,
            accessibilityName: element.title,
            transform: $transform,
            isSelected: isSelected,
            showsEditingControls: showsEditingControls,
            showsDeleteControl: false,
            onSelect: onSelect,
            onTapSelected: onTapSelected,
            onDelete: onDelete
        ) {
            elementContent
        }
    }

    @ViewBuilder
    private var elementContent: some View {
        switch element {
        case .dataGroup:
            VStack(alignment: .leading, spacing: 0) {
                Image("runpamine_share_logo")
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: logoSize.width, height: logoSize.height)
                    .foregroundStyle(dataColor)
                    .shadow(color: .black.opacity(0.35), radius: 5, y: 2)
                    .padding(.bottom, 2)

                HStack(alignment: .firstTextBaseline, spacing: 8) {
                    Text(record.distanceKilometers.formatted(.number.precision(.fractionLength(2))))
                        .font(AppTheme.Typography.font(size: 66, weight: .extraBold))
                    Text("KM")
                        .font(AppTheme.Typography.font(size: 22, weight: .bold))
                        .foregroundStyle(dataColor.opacity(0.88))
                }
                .foregroundStyle(dataColor)

                if layout.showsDetails {
                    HStack(spacing: 28) {
                        metric(title: "TIME", value: RunningMetricFormatter.duration(record.elapsedTime))
                        metric(title: "PACE", value: RunningMetricFormatter.pace(record.averagePaceSecondsPerKilometer))
                        metric(title: "KCAL", value: "\(record.estimatedCalories)")
                    }
                    .padding(.top, 11)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)

        case .route:
            RunShareRouteMap(route: record.route, color: routeColor)
                .frame(maxWidth: .infinity, maxHeight: .infinity)

        }
    }

    private func metric(title: String, value: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(AppTheme.Typography.font(size: 12, weight: .medium))
                .tracking(1)
                .foregroundStyle(dataColor.opacity(0.7))
            Text(value)
                .font(AppTheme.Typography.font(size: 21, weight: .bold))
                .foregroundStyle(dataColor)
        }
    }
}

private enum RunShareElementFormatters {
    static let date: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "yyyy.MM.dd"
        return formatter
    }()

    static let timeRange: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "a h:mm"
        return formatter
    }()
}

private struct RunShareRouteMap: View {
    let route: [RunningCoordinate]
    let color: Color

    var body: some View {
        GeometryReader { geometry in
            ZStack {
                if route.count >= 2 {
                    routePath(in: geometry.size)
                        .stroke(
                            Color.clear,
                            style: StrokeStyle(lineWidth: 18, lineCap: .round, lineJoin: .round)
                        )

                    routePath(in: geometry.size)
                        .stroke(
                            color,
                            style: StrokeStyle(lineWidth: 5, lineCap: .round, lineJoin: .round)
                        )
                } else {
                    VStack(spacing: 8) {
                        Image(systemName: "map")
                            .font(.system(size: 24, weight: .semibold))
                        Text("러닝 루트 없음")
                            .font(AppTheme.Typography.font(size: 13, weight: .bold))
                    }
                    .foregroundStyle(color.opacity(0.72))
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

private struct RunShareTransformableView<Content: View>: View {
    let contentSize: CGSize
    let center: CGPoint
    let accessibilityName: String
    @Binding var transform: RunShareCanvasItemTransform
    let isSelected: Bool
    let showsEditingControls: Bool
    let showsDeleteControl: Bool
    let onSelect: () -> Void
    let onTapSelected: () -> Void
    let onDelete: () -> Void
    @ViewBuilder let content: () -> Content
    @GestureState private var dragTranslation: CGSize = .zero
    @GestureState private var resizeState: RunShareCanvasItemResizeState?
    @GestureState private var rotationDeltaDegrees: Double = 0

    var body: some View {
        ZStack {
            content()
                .frame(width: contentSize.width, height: contentSize.height)
                .contentShape(Rectangle())
                .highPriorityGesture(moveGesture)
                .overlay {
                    if showsEditingControls && isSelected {
                        Rectangle()
                            .stroke(.white, style: StrokeStyle(lineWidth: 1.5, dash: [6, 4]))
                            .allowsHitTesting(false)
                    }
                }
                .scaleEffect(displayedScale)
                .rotationEffect(.degrees(displayedRotationDegrees))

            ForEach(RunShareCanvasItemResizeCorner.allCases, id: \.self) { corner in
                resizeHandle(for: corner)
                    .offset(transformedCornerOffset(for: corner))
            }

            rotationHandle
                .offset(rotationHandleOffset)

            deleteButton
                .offset(deleteButtonOffset)
        }
            .frame(width: contentSize.width, height: contentSize.height)
            .offset(
                x: transform.offset.width + dragTranslation.width,
                y: transform.offset.height + dragTranslation.height
            )
    }

    private var displayedScale: CGFloat {
        let resizeDelta = resizeState.map {
            scaleDelta(for: $0.translation, corner: $0.corner)
        } ?? 0
        return min(max(transform.scale + resizeDelta, 0.4), 2.5)
    }

    private var displayedRotationDegrees: Double {
        transform.rotationDegrees + rotationDeltaDegrees
    }

    @ViewBuilder
    private var deleteButton: some View {
        if showsEditingControls && showsDeleteControl && isSelected {
            Button(action: onDelete) {
                ZStack {
                    Color.clear
                        .frame(width: 36, height: 36)

                    Circle()
                        .fill(Color(red: 0.92, green: 0.20, blue: 0.24))
                        .frame(width: 22, height: 22)
                        .overlay {
                            Image(systemName: "xmark")
                                .font(.system(size: 10, weight: .bold))
                                .foregroundStyle(.white)
                        }
                }
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel("\(accessibilityName) 삭제")
        }
    }

    @ViewBuilder
    private var rotationHandle: some View {
        if showsEditingControls && isSelected {
            VStack(spacing: 0) {
                ZStack {
                    Color.clear
                        .frame(width: 34, height: 34)

                    Circle()
                        .fill(.white)
                        .frame(width: 18, height: 18)
                        .overlay {
                            Image(systemName: "arrow.clockwise")
                                .font(.system(size: 9, weight: .bold))
                                .foregroundStyle(.black.opacity(0.72))
                        }
                }

                Rectangle()
                    .fill(.white.opacity(0.9))
                    .frame(width: 1.5, height: 10)
                    .allowsHitTesting(false)
            }
            .contentShape(Rectangle())
            .highPriorityGesture(rotationGesture)
            .accessibilityLabel("\(accessibilityName) 회전")
        }
    }

    @ViewBuilder
    private func resizeHandle(for corner: RunShareCanvasItemResizeCorner) -> some View {
        if showsEditingControls && isSelected {
            ZStack {
                Color.clear
                    .frame(width: 30, height: 30)

                Circle()
                    .fill(.white)
                    .frame(width: 14, height: 14)
                    .overlay {
                        Circle()
                            .stroke(Color.black.opacity(0.55), lineWidth: 1)
                    }
            }
            .contentShape(Rectangle())
            .highPriorityGesture(resizeGesture(for: corner))
            .accessibilityLabel("\(accessibilityName) 크기 조절")
        }
    }

    private var moveGesture: some Gesture {
        DragGesture(
            minimumDistance: 0,
            coordinateSpace: .named(RunShareCoordinateSpace.canvas)
        )
            .updating($dragTranslation) { value, state, _ in
                state = value.translation
            }
            .onEnded { value in
                let isTap = hypot(value.translation.width, value.translation.height) < 3
                var updatedTransform = transform
                updatedTransform.offset = CGSize(
                    width: updatedTransform.offset.width + value.translation.width,
                    height: updatedTransform.offset.height + value.translation.height
                )
                transform = updatedTransform
                if isTap && isSelected {
                    onTapSelected()
                } else if !isSelected {
                    onSelect()
                }
            }
    }

    private func resizeGesture(for corner: RunShareCanvasItemResizeCorner) -> some Gesture {
        DragGesture(
            minimumDistance: 0,
            coordinateSpace: .named(RunShareCoordinateSpace.canvas)
        )
            .updating($resizeState) { value, state, _ in
                state = RunShareCanvasItemResizeState(
                    corner: corner,
                    translation: value.translation
                )
            }
            .onEnded { value in
                var updatedTransform = transform
                updatedTransform.scale = min(
                    max(updatedTransform.scale + scaleDelta(for: value.translation, corner: corner), 0.4),
                    2.5
                )
                transform = updatedTransform
            }
    }

    private var rotationGesture: some Gesture {
        DragGesture(
            minimumDistance: 0,
            coordinateSpace: .named(RunShareCoordinateSpace.canvas)
        )
            .updating($rotationDeltaDegrees) { value, state, _ in
                state = rotationDelta(for: value)
            }
            .onEnded { value in
                var updatedTransform = transform
                updatedTransform.rotationDegrees = normalizedDegrees(
                    updatedTransform.rotationDegrees + rotationDelta(for: value)
                )
                transform = updatedTransform
            }
    }

    private func scaleDelta(
        for translation: CGSize,
        corner: RunShareCanvasItemResizeCorner
    ) -> CGFloat {
        let radians = transform.rotationDegrees * .pi / 180
        let localTranslation = CGSize(
            width: translation.width * cos(radians) + translation.height * sin(radians),
            height: -translation.width * sin(radians) + translation.height * cos(radians)
        )
        let cornerVector = CGVector(
            dx: corner.horizontalDirection * contentSize.width / 2,
            dy: corner.verticalDirection * contentSize.height / 2
        )
        let squaredLength = cornerVector.dx * cornerVector.dx
            + cornerVector.dy * cornerVector.dy
        guard squaredLength > 0 else { return 0 }

        return (
            localTranslation.width * cornerVector.dx
                + localTranslation.height * cornerVector.dy
        ) / squaredLength
    }

    private func transformedCornerOffset(
        for corner: RunShareCanvasItemResizeCorner
    ) -> CGSize {
        transformedOffset(
            x: corner.horizontalDirection * contentSize.width * displayedScale / 2,
            y: corner.verticalDirection * contentSize.height * displayedScale / 2
        )
    }

    private var rotationHandleOffset: CGSize {
        transformedOffset(
            x: 0,
            y: -(contentSize.height * displayedScale / 2 + 34)
        )
    }

    private var deleteButtonOffset: CGSize {
        let corner = transformedCornerOffset(for: .topTrailing)
        return CGSize(width: corner.width + 24, height: corner.height - 24)
    }

    private func transformedOffset(x: CGFloat, y: CGFloat) -> CGSize {
        let radians = displayedRotationDegrees * .pi / 180
        return CGSize(
            width: x * cos(radians) - y * sin(radians),
            height: x * sin(radians) + y * cos(radians)
        )
    }

    private func rotationDelta(for value: DragGesture.Value) -> Double {
        let rotationCenter = CGPoint(
            x: center.x + transform.offset.width,
            y: center.y + transform.offset.height
        )
        let startAngle = atan2(
            value.startLocation.y - rotationCenter.y,
            value.startLocation.x - rotationCenter.x
        )
        let currentAngle = atan2(
            value.location.y - rotationCenter.y,
            value.location.x - rotationCenter.x
        )
        return normalizedDegrees((currentAngle - startAngle) * 180 / .pi)
    }

    private func normalizedDegrees(_ degrees: Double) -> Double {
        var normalized = degrees.truncatingRemainder(dividingBy: 360)
        if normalized > 180 {
            normalized -= 360
        } else if normalized < -180 {
            normalized += 360
        }
        return normalized
    }
}

private struct RunShareLocationIcon: View {
    let color: Color

    var body: some View {
        GeometryReader { geometry in
            let size = geometry.size
            ZStack {
                Path { path in
                    path.move(to: CGPoint(x: size.width / 2, y: size.height))
                    path.addCurve(
                        to: CGPoint(x: size.width * 0.08, y: size.height * 0.40),
                        control1: CGPoint(x: size.width * 0.36, y: size.height * 0.80),
                        control2: CGPoint(x: size.width * 0.08, y: size.height * 0.60)
                    )
                    path.addCurve(
                        to: CGPoint(x: size.width / 2, y: size.height * 0.04),
                        control1: CGPoint(x: size.width * 0.08, y: size.height * 0.18),
                        control2: CGPoint(x: size.width * 0.26, y: size.height * 0.04)
                    )
                    path.addCurve(
                        to: CGPoint(x: size.width * 0.92, y: size.height * 0.40),
                        control1: CGPoint(x: size.width * 0.74, y: size.height * 0.04),
                        control2: CGPoint(x: size.width * 0.92, y: size.height * 0.18)
                    )
                    path.addCurve(
                        to: CGPoint(x: size.width / 2, y: size.height),
                        control1: CGPoint(x: size.width * 0.92, y: size.height * 0.60),
                        control2: CGPoint(x: size.width * 0.64, y: size.height * 0.80)
                    )
                }
                .stroke(color, style: StrokeStyle(lineWidth: 2, lineCap: .round, lineJoin: .round))

                Circle()
                    .stroke(color, lineWidth: 2)
                    .frame(width: size.width * 0.34, height: size.width * 0.34)
                    .offset(y: -size.height * 0.18)
            }
        }
    }
}

private struct RunShareStickerView: View {
    let sticker: RunShareSticker
    let record: RunningRecord
    let regionName: String?
    let textColor: Color
    let size: CGSize
    let center: CGPoint
    @Binding var transform: RunShareCanvasItemTransform
    let isSelected: Bool
    let showsEditingControls: Bool
    let onSelect: () -> Void
    let onTapSelected: () -> Void
    let onDelete: () -> Void

    var body: some View {
        RunShareTransformableView(
            contentSize: size,
            center: center,
            accessibilityName: "스티커",
            transform: $transform,
            isSelected: isSelected,
            showsEditingControls: showsEditingControls,
            showsDeleteControl: true,
            onSelect: onSelect,
            onTapSelected: onTapSelected,
            onDelete: onDelete
        ) {
            stickerContent
        }
    }

    @ViewBuilder
    private var stickerContent: some View {
        switch sticker {
        case .date:
            Text(RunShareElementFormatters.date.string(from: record.startedAt))
            .font(AppTheme.Typography.font(size: 19, weight: .bold))
            .foregroundStyle(textColor)
            .lineLimit(1)
            .minimumScaleFactor(0.65)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
        case .region:
            if let regionName {
                HStack(spacing: 6) {
                    RunShareLocationIcon(color: textColor)
                        .frame(width: 18, height: 22)
                    Text(regionName)
                        .font(AppTheme.Typography.font(size: 21, weight: .bold))
                        .lineLimit(1)
                        .minimumScaleFactor(0.7)
                }
                .foregroundStyle(textColor)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .trailing)
            }
        default:
            if let assetName = sticker.assetName {
                Image(assetName)
                    .resizable()
                    .scaledToFit()
            }
        }
    }
}

private struct RunShareCanvasItemTransform: Equatable {
    var offset: CGSize = .zero
    var scale: CGFloat = 1
    var rotationDegrees: Double = 0
}

private struct RunShareCanvasItemResizeState {
    let corner: RunShareCanvasItemResizeCorner
    let translation: CGSize
}

private enum RunShareCanvasItemResizeCorner: CaseIterable, Hashable {
    case topLeading
    case topTrailing
    case bottomLeading
    case bottomTrailing

    var horizontalDirection: CGFloat {
        switch self {
        case .topLeading, .bottomLeading: -1
        case .topTrailing, .bottomTrailing: 1
        }
    }

    var verticalDirection: CGFloat {
        switch self {
        case .topLeading, .topTrailing: -1
        case .bottomLeading, .bottomTrailing: 1
        }
    }
}

private enum RunShareCoordinateSpace {
    static let canvas = "runShareCanvas"
}

private enum RunShareEditorTab: String, CaseIterable, Identifiable {
    case layout
    case sticker

    var id: String { rawValue }

    var title: String {
        switch self {
        case .layout: "러닝 요소 조합"
        case .sticker: "스티커"
        }
    }
}

private enum RunShareElement: String, CaseIterable, Hashable, Identifiable {
    case dataGroup
    case route

    var id: String { rawValue }

    var title: String {
        switch self {
        case .dataGroup: "러닝 데이터"
        case .route: "러닝 루트"
        }
    }

    static let routeHeightRatio: CGFloat = 0.38
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
        case .current: "러닝 데이터"
        case .routeDistance: "러닝 거리 + 루트"
        case .routeCurrent: "러닝 데이터 + 루트"
        }
    }

    var iconText: String? {
        switch self {
        case .distance: "KM"
        case .current, .routeDistance, .routeCurrent: nil
        }
    }

    var systemImage: String {
        switch self {
        case .distance: "ruler"
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

    var showsDetails: Bool {
        switch self {
        case .distance, .routeDistance: false
        case .current, .routeCurrent: true
        }
    }
}

private enum RunShareSticker: String, CaseIterable, Hashable, Identifiable {
    case date
    case region
    case pamin
    case cheetahPamin
    case surprisedPamin
    case hamburgerPamin
    case handstandPamin

    var id: String { rawValue }

    var supportsColorToggle: Bool {
        self == .date || self == .region
    }

    var title: String {
        switch self {
        case .date: "러닝 날짜"
        case .region: "러닝 위치"
        case .pamin: "파민"
        case .cheetahPamin: "치타파민"
        case .surprisedPamin: "놀란 파민"
        case .hamburgerPamin: "햄버거 파민"
        case .handstandPamin: "물구나무 파민"
        }
    }

    var assetName: String? {
        switch self {
        case .date, .region: nil
        case .pamin: "pamin_sticker"
        case .cheetahPamin: "cheetah_pamin_sticker"
        case .surprisedPamin: "surprised_pamin_sticker"
        case .hamburgerPamin: "hamburger_pamin_sticker"
        case .handstandPamin: "handstand_pamin_sticker"
        }
    }

    var widthRatio: CGFloat {
        switch self {
        case .date, .region: 0
        case .pamin: 0.24
        case .cheetahPamin: 0.44
        case .surprisedPamin: 0.26
        case .hamburgerPamin: 0.40
        case .handstandPamin: 0.46
        }
    }

    var defaultX: CGFloat {
        switch self {
        case .date: 0.30
        case .region: 0.76
        case .pamin: 0.84
        case .cheetahPamin: 0.76
        case .surprisedPamin: 0.84
        case .hamburgerPamin: 0.78
        case .handstandPamin: 0.72
        }
    }

    var defaultY: CGFloat {
        switch self {
        case .date: 0.88
        case .region: 0.26
        case .pamin: 0.77
        case .cheetahPamin: 0.75
        case .surprisedPamin: 0.73
        case .hamburgerPamin: 0.76
        case .handstandPamin: 0.73
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

    func zoom(by scale: CGFloat) {
        guard isPreviewAvailable, scale.isFinite, scale > 0 else { return }

        sessionQueue.async { [weak self] in
            guard let device = self?.videoInput?.device else { return }

            do {
                try device.lockForConfiguration()
                defer { device.unlockForConfiguration() }

                let maximumZoomFactor = min(device.maxAvailableVideoZoomFactor, 5)
                let zoomFactor = device.videoZoomFactor * scale
                device.videoZoomFactor = min(
                    max(zoomFactor, device.minAvailableVideoZoomFactor),
                    maximumZoomFactor
                )
            } catch {
                return
            }
        }
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
