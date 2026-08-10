package com.woowacourse.runpamine.presentation.share

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun RunShareFlow(
    data: RunShareData,
    onClose: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedPhoto by remember { mutableStateOf<Bitmap?>(null) }

    BackHandler {
        if (selectedPhoto == null) {
            onClose()
        } else {
            selectedPhoto = null
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Black,
    ) {
        if (selectedPhoto == null) {
            RunShareCameraStep(
                onClose = onClose,
                onPhotoSelected = { selectedPhoto = it },
            )
        } else {
            RunShareEditorScreen(
                photo = selectedPhoto ?: return@Surface,
                data = data,
                onBack = { selectedPhoto = null },
                onSaved = {
                    selectedPhoto = null
                    onSaved()
                },
            )
        }
    }
}

@Composable
private fun RunShareCameraStep(
    onClose: () -> Unit,
    onPhotoSelected: (Bitmap) -> Unit,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(context.hasCameraPermission()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoadingAlbum by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val cameraState =
        rememberRunShareCameraState(
            hasCameraPermission = hasCameraPermission,
            onPhotoSelected = onPhotoSelected,
            onReady = { errorMessage = null },
            onError = { errorMessage = it },
        )
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasCameraPermission = granted
        }
    val photoPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            isLoadingAlbum = true
            errorMessage = null
            scope.launch {
                runCatching { context.decodeBitmap(uri) }
                    .onSuccess(onPhotoSelected)
                    .onFailure { errorMessage = "사진을 불러오지 못했어요." }
                isLoadingAlbum = false
            }
        }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            RunShareCameraPreview(
                cameraState = cameraState,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            RunShareCameraPermission(
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.Black.copy(alpha = 0.55f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.82f),
                                ),
                        ),
                    ),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    enabled = hasCameraPermission && cameraState.flashAvailable,
                    onClick = { cameraState.toggleFlash() },
                ) {
                    Icon(
                        imageVector = if (cameraState.flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = if (cameraState.flashEnabled) "플래시 끄기" else "플래시 켜기",
                        tint = Color.White,
                    )
                }
                IconButton(
                    enabled = hasCameraPermission && cameraState.isReady,
                    onClick = { cameraState.switchCamera() },
                ) {
                    Icon(Icons.Default.FlipCameraAndroid, contentDescription = "카메라 전환", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (errorMessage != null || isLoadingAlbum) {
                Text(
                    text = if (isLoadingAlbum) "사진을 불러오는 중이에요." else errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                )
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RunShareCameraTextButton(
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                    text = "앨범",
                    onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                )
                ShutterButton(
                    enabled = hasCameraPermission && cameraState.isReady,
                    onClick = { cameraState.capture() },
                )
                RunShareCameraTextButton(
                    icon = { Icon(Icons.Default.FlipCameraAndroid, contentDescription = null) },
                    text = "전환",
                    enabled = hasCameraPermission && cameraState.isReady,
                    onClick = { cameraState.switchCamera() },
                )
            }
        }
    }
}

@Composable
private fun rememberRunShareCameraState(
    hasCameraPermission: Boolean,
    onPhotoSelected: (Bitmap) -> Unit,
    onReady: () -> Unit,
    onError: (String) -> Unit,
): RunShareCameraState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView =
        remember {
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashEnabled by remember { mutableStateOf(false) }
    var flashAvailable by remember { mutableStateOf(false) }
    var isReady by remember { mutableStateOf(false) }
    val imageCapture =
        remember {
            ImageCapture
                .Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
        }

    LaunchedEffect(context) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            { cameraProvider = future.get() },
            ContextCompat.getMainExecutor(context),
        )
    }

    DisposableEffect(cameraProvider, lensFacing, previewView, hasCameraPermission) {
        val provider = cameraProvider
        if (provider != null && hasCameraPermission) {
            val selector =
                CameraSelector
                    .Builder()
                    .requireLensFacing(lensFacing)
                    .build()
            val preview =
                Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            runCatching {
                provider.unbindAll()
                camera =
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageCapture,
                    )
                flashAvailable = camera?.cameraInfo?.hasFlashUnit() == true
                isReady = true
                onReady()
            }.onFailure {
                flashAvailable = false
                isReady = false
                onError("카메라를 시작하지 못했어요.")
            }
        }

        onDispose {
            provider?.unbindAll()
            isReady = false
        }
    }

    return remember(context, previewView, imageCapture, flashEnabled, flashAvailable, isReady, lensFacing) {
        RunShareCameraState(
            previewView = previewView,
            flashEnabled = flashEnabled,
            flashAvailable = flashAvailable,
            isReady = isReady,
            toggleFlash = {
                flashEnabled = !flashEnabled
            },
            switchCamera = {
                flashEnabled = false
                lensFacing =
                    if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
            },
            capture = {
                val file = File.createTempFile("run_share_", ".jpg", context.cacheDir)
                val options = ImageCapture.OutputFileOptions.Builder(file).build()
                imageCapture.flashMode =
                    if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
                imageCapture.takePicture(
                    options,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            runCatching { decodeShareBitmap(ImageDecoder.createSource(file)) }
                                .onSuccess(onPhotoSelected)
                                .onFailure { onError("사진을 불러오지 못했어요.") }
                            file.delete()
                        }

                        override fun onError(exception: ImageCaptureException) {
                            file.delete()
                            onError("사진을 촬영하지 못했어요.")
                        }
                    },
                )
            },
        )
    }
}

@Composable
private fun RunShareCameraPreview(
    cameraState: RunShareCameraState,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { cameraState.previewView },
        modifier = modifier,
    )
}

@Composable
private fun RunShareCameraPermission(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .background(Color.Black)
                .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Default.CameraAlt,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(58.dp),
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "러닝 사진을 촬영하려면 카메라 권한이 필요해요.",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = onRequestPermission) {
            Text("권한 허용")
        }
    }
}

@Composable
private fun RunShareCameraTextButton(
    icon: @Composable () -> Unit,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .width(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = enabled, onClick = onClick)
                .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides Color.White,
                content = icon,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
    }
}

@Composable
private fun ShutterButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(84.dp)
                .clip(CircleShape)
                .border(4.dp, Color.White, CircleShape)
                .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(66.dp)
                    .clip(CircleShape)
                    .background(if (enabled) Color.White else Color.White.copy(alpha = 0.45f)),
        )
    }
}

private data class RunShareCameraState(
    val previewView: PreviewView,
    val flashEnabled: Boolean,
    val flashAvailable: Boolean,
    val isReady: Boolean,
    val toggleFlash: () -> Unit,
    val switchCamera: () -> Unit,
    val capture: () -> Unit,
)

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

private suspend fun Context.decodeBitmap(uri: Uri): Bitmap =
    withContext(Dispatchers.IO) {
        decodeShareBitmap(ImageDecoder.createSource(contentResolver, uri))
    }

private fun decodeShareBitmap(source: ImageDecoder.Source): Bitmap =
    ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        val width = info.size.width
        val height = info.size.height
        val longestSide = maxOf(width, height)
        if (longestSide > MAX_SHARE_PHOTO_SIZE) {
            val scale = MAX_SHARE_PHOTO_SIZE.toFloat() / longestSide
            decoder.setTargetSize(
                (width * scale).toInt().coerceAtLeast(1),
                (height * scale).toInt().coerceAtLeast(1),
            )
        }
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
    }

private const val MAX_SHARE_PHOTO_SIZE = 2_048
