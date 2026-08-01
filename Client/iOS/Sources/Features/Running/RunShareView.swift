import AVFoundation
import ImageIO
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
                    Image(systemName: "camera.slash")
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
    @State private var selectedTheme: RunShareTheme = .midnight
    @State private var selectedTab: RunShareEditorTab = .theme
    @State private var showsCharacterSticker = false
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
                            theme: selectedTheme,
                            showsCharacterSticker: showsCharacterSticker
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

            if selectedTab == .theme {
                themeOptions
            } else {
                stickerOptions
            }

            if let saveMessage {
                Text(saveMessage)
                    .font(AppTheme.Typography.font(size: 13, weight: .medium))
                    .foregroundStyle(.white.opacity(0.75))
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

    private var themeOptions: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                ForEach(RunShareTheme.allCases) { theme in
                    Button {
                        selectedTheme = theme
                    } label: {
                        VStack(spacing: 8) {
                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                                .fill(theme.background)
                                .frame(width: 86, height: 86)
                                .overlay {
                                    Text("5.20")
                                        .font(AppTheme.Typography.font(size: 16, weight: .black))
                                        .foregroundStyle(theme.foreground)
                                }
                                .overlay {
                                    RoundedRectangle(cornerRadius: 12, style: .continuous)
                                        .stroke(
                                            selectedTheme == theme ? AppTheme.Colors.primary : Color.clear,
                                            lineWidth: 2
                                        )
                                }
                            Text(theme.title)
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
        HStack {
            Button {
                showsCharacterSticker.toggle()
            } label: {
                VStack(spacing: 8) {
                    Image("character_no_bg")
                        .resizable()
                        .scaledToFit()
                        .padding(8)
                        .frame(width: 86, height: 86)
                        .background(Color.white.opacity(0.08))
                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                        .overlay {
                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                                .stroke(
                                    showsCharacterSticker ? AppTheme.Colors.primary : Color.clear,
                                    lineWidth: 2
                                )
                        }
                    Text("캐릭터")
                        .font(AppTheme.Typography.font(size: 12, weight: .medium))
                        .foregroundStyle(.white.opacity(0.75))
                }
            }
            .buttonStyle(.plain)

            Spacer()
        }
        .padding(.horizontal, 18)
    }

    @MainActor
    private func share() {
        guard let image = renderImage() else { return }
        renderedImage = image
        isShowingShareSheet = true
    }

    @MainActor
    private func saveToPhotos() {
        guard let image = renderImage() else { return }

        PHPhotoLibrary.requestAuthorization(for: .addOnly) { status in
            DispatchQueue.main.async {
                guard status == .authorized || status == .limited else {
                    saveMessage = "사진 보관함 권한이 필요해요."
                    return
                }

                UIImageWriteToSavedPhotosAlbum(image, nil, nil, nil)
                saveMessage = "사진을 저장했어요."
            }
        }
    }

    @MainActor
    private func renderImage() -> UIImage? {
        let canvas = RunShareCanvas(
            record: record,
            photo: photo,
            theme: selectedTheme,
            showsCharacterSticker: showsCharacterSticker
        )
        let renderer = ImageRenderer(content: canvas.frame(width: 1080, height: 1920))
        renderer.scale = 1
        return renderer.uiImage
    }
}

private struct RunShareCanvas: View {
    let record: RunningRecord
    let photo: UIImage
    let theme: RunShareTheme
    let showsCharacterSticker: Bool

    var body: some View {
        GeometryReader { geometry in
            ZStack(alignment: .topLeading) {
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

                theme.tint.opacity(0.16)

                VStack(alignment: .leading, spacing: 0) {
                    HStack(alignment: .top) {
                        Text("개인 최고 기록")
                            .font(AppTheme.Typography.font(size: 19, weight: .bold))
                            .foregroundStyle(theme.foreground)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 9)
                            .background(theme.accent)
                            .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))

                        Spacer()

                        Text("\(record.estimatedCalories)\nKCAL")
                            .font(AppTheme.Typography.font(size: 15, weight: .bold))
                            .multilineTextAlignment(.center)
                            .foregroundStyle(theme.foreground)
                            .frame(width: 72, height: 72)
                            .overlay {
                                Circle().stroke(theme.foreground, lineWidth: 2)
                            }
                    }

                    Spacer()

                    Text(record.distanceKilometers.formatted(.number.precision(.fractionLength(2))))
                        .font(AppTheme.Typography.font(size: 66, weight: .black))
                        .foregroundStyle(theme.foreground)
                    Text("KILOMETERS")
                        .font(AppTheme.Typography.font(size: 18, weight: .bold))
                        .tracking(2)
                        .foregroundStyle(theme.foreground.opacity(0.9))

                    HStack(spacing: 28) {
                        shareMetric(title: "TIME", value: RunningMetricFormatter.duration(record.elapsedTime))
                        shareMetric(title: "PACE", value: RunningMetricFormatter.pace(record.averagePaceSecondsPerKilometer))
                        shareMetric(title: "KCAL", value: "\(record.estimatedCalories)")
                    }
                    .padding(.top, 22)
                }
                .padding(30)

                if showsCharacterSticker {
                    Image("character_no_bg")
                        .resizable()
                        .scaledToFit()
                        .frame(width: geometry.size.width * 0.32)
                        .rotationEffect(.degrees(10))
                        .position(
                            x: geometry.size.width * 0.82,
                            y: geometry.size.height * 0.72
                        )
                }
            }
        }
        .aspectRatio(9.0 / 16.0, contentMode: .fit)
        .background(theme.background)
    }

    private func shareMetric(title: String, value: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(AppTheme.Typography.font(size: 12, weight: .medium))
                .tracking(1)
                .foregroundStyle(theme.foreground.opacity(0.7))
            Text(value)
                .font(AppTheme.Typography.font(size: 21, weight: .bold))
                .foregroundStyle(theme.foreground)
        }
    }
}

private enum RunShareEditorTab: String, CaseIterable, Identifiable {
    case theme
    case sticker

    var id: String { rawValue }

    var title: String {
        switch self {
        case .theme: "러닝 데이터 테마"
        case .sticker: "스티커"
        }
    }
}

private enum RunShareTheme: String, CaseIterable, Identifiable {
    case midnight
    case ocean
    case paper

    var id: String { rawValue }

    var title: String {
        switch self {
        case .midnight: "기본"
        case .ocean: "블루"
        case .paper: "화이트"
        }
    }

    var background: Color {
        switch self {
        case .midnight: Color(red: 0.06, green: 0.06, blue: 0.06)
        case .ocean: Color(red: 0.02, green: 0.19, blue: 0.45)
        case .paper: Color.white
        }
    }

    var foreground: Color {
        switch self {
        case .midnight, .ocean: .white
        case .paper: AppTheme.Colors.textPrimary
        }
    }

    var accent: Color {
        switch self {
        case .midnight: AppTheme.Colors.primary
        case .ocean: Color(red: 0.30, green: 0.70, blue: 1.0)
        case .paper: AppTheme.Colors.primary
        }
    }

    var tint: Color {
        switch self {
        case .midnight: .black
        case .ocean: AppTheme.Colors.primary
        case .paper: .white
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
