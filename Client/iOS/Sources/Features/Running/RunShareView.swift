import Photos
import PhotosUI
import SwiftUI
import UIKit

struct RunShareCameraView: View {
    let record: RunningRecord

    @Environment(\.dismiss) private var dismiss
    @State private var selectedImage: UIImage?
    @State private var photoItem: PhotosPickerItem?
    @State private var isPresentingCamera = false
    @State private var isPresentingEditor = false
    @State private var errorMessage: String?

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            VStack(spacing: 0) {
                HStack {
                    Button(action: dismiss.callAsFunction) {
                        Image(systemName: "xmark")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundStyle(.white)
                            .frame(width: 44, height: 44)
                    }
                    .accessibilityLabel("닫기")

                    Spacer()

                    Text("사진 선택")
                        .font(AppTheme.Typography.title2)
                        .foregroundStyle(.white)

                    Spacer()

                    Color.clear
                        .frame(width: 44, height: 44)
                }
                .padding(.horizontal, 12)
                .padding(.top, 8)

                Spacer()

                Image(systemName: "camera.fill")
                    .font(.system(size: 44, weight: .medium))
                    .foregroundStyle(.white.opacity(0.9))
                Text("러닝 사진을 선택해주세요")
                    .font(AppTheme.Typography.font(size: 18, weight: .bold))
                    .foregroundStyle(.white)
                    .padding(.top, 20)
                Text("사진 위에 러닝 데이터를 꾸며 공유할 수 있어요.")
                    .font(AppTheme.Typography.font(size: 14, weight: .medium))
                    .foregroundStyle(.white.opacity(0.65))
                    .padding(.top, 8)

                if let errorMessage {
                    Text(errorMessage)
                        .font(AppTheme.Typography.font(size: 13, weight: .medium))
                        .foregroundStyle(Color(red: 1, green: 0.45, blue: 0.45))
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 24)
                        .padding(.top, 20)
                }

                Spacer()

                HStack(spacing: 14) {
                    if UIImagePickerController.isSourceTypeAvailable(.camera) {
                        actionButton(title: "카메라", systemImage: "camera") {
                            isPresentingCamera = true
                        }
                    }

                    PhotosPicker(
                        selection: $photoItem,
                        matching: .images,
                        photoLibrary: .shared()
                    ) {
                        actionButtonLabel(title: "앨범", systemImage: "photo.on.rectangle")
                    }
                    .buttonStyle(.plain)
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 28)
            }
        }
        .onAppear {
            if UIImagePickerController.isSourceTypeAvailable(.camera) {
                isPresentingCamera = true
            }
        }
        .fullScreenCover(isPresented: $isPresentingCamera) {
            CameraImagePicker(
                onImagePicked: { image in
                    selectedImage = image
                    isPresentingCamera = false
                    presentEditorOnNextRunLoop()
                },
                onCancel: {
                    isPresentingCamera = false
                }
            )
            .ignoresSafeArea()
        }
        .fullScreenCover(isPresented: $isPresentingEditor) {
            if let selectedImage {
                RunShareEditorView(
                    record: record,
                    photo: selectedImage,
                    onRetake: {
                        isPresentingEditor = false
                        isPresentingCamera = UIImagePickerController.isSourceTypeAvailable(.camera)
                    }
                )
            }
        }
        .onChange(of: photoItem) { _, item in
            guard let item else { return }
            Task {
                do {
                    guard let data = try await item.loadTransferable(type: Data.self),
                          let image = UIImage(data: data)
                    else {
                        throw RunShareImageError.invalidImage
                    }
                    await MainActor.run {
                        selectedImage = image
                        presentEditorOnNextRunLoop()
                    }
                } catch {
                    await MainActor.run {
                        errorMessage = "사진을 불러오지 못했어요. 다시 선택해주세요."
                    }
                }
            }
        }
    }

    private func presentEditorOnNextRunLoop() {
        DispatchQueue.main.async {
            isPresentingEditor = selectedImage != nil
        }
    }

    @ViewBuilder
    private func actionButton(title: String, systemImage: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            actionButtonLabel(title: title, systemImage: systemImage)
        }
        .buttonStyle(.plain)
    }

    private func actionButtonLabel(title: String, systemImage: String) -> some View {
        Label(title, systemImage: systemImage)
            .font(AppTheme.Typography.font(size: 16, weight: .bold))
            .foregroundStyle(.black)
            .frame(maxWidth: .infinity)
            .frame(height: 56)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
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

private struct CameraImagePicker: UIViewControllerRepresentable {
    let onImagePicked: (UIImage) -> Void
    let onCancel: () -> Void

    func makeCoordinator() -> Coordinator {
        Coordinator(onImagePicked: onImagePicked, onCancel: onCancel)
    }

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let controller = UIImagePickerController()
        controller.sourceType = .camera
        controller.mediaTypes = ["public.image"]
        controller.allowsEditing = false
        controller.delegate = context.coordinator
        return controller
    }

    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}

    final class Coordinator: NSObject, UINavigationControllerDelegate, UIImagePickerControllerDelegate {
        let onImagePicked: (UIImage) -> Void
        let onCancel: () -> Void

        init(onImagePicked: @escaping (UIImage) -> Void, onCancel: @escaping () -> Void) {
            self.onImagePicked = onImagePicked
            self.onCancel = onCancel
        }

        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            onCancel()
        }

        func imagePickerController(
            _ picker: UIImagePickerController,
            didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]
        ) {
            guard let image = info[.originalImage] as? UIImage else {
                onCancel()
                return
            }
            onImagePicked(image)
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
