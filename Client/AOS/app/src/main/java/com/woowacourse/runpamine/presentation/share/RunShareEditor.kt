package com.woowacourse.runpamine.presentation.share

import android.content.Context
import android.graphics.Bitmap
import android.location.Geocoder
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

private val ShareBlue = Color(0xFF0058FF)
private val EditorPanel = Color(0xFF141414)
private val TextSelectionHandleInset = 6.dp

private enum class RunShareEditorTab(
    val title: String,
) {
    Layout("러닝 요소 조합"),
    Sticker("스티커"),
}

@Composable
internal fun RunShareEditorScreen(
    photo: Bitmap,
    data: RunShareData,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(RunShareEditorTab.Layout) }
    var layout by remember { mutableStateOf(RunShareLayout.AllMetrics) }
    var selectedStickers by remember { mutableStateOf(emptySet<RunShareSticker>()) }
    var selectedElement by remember { mutableStateOf<RunShareElement?>(null) }
    var selectedSticker by remember { mutableStateOf<RunShareSticker?>(null) }
    var elementTransforms by remember { mutableStateOf(emptyMap<RunShareElement, RunShareItemTransform>()) }
    var stickerTransforms by remember { mutableStateOf(emptyMap<RunShareSticker, RunShareItemTransform>()) }
    var darkElements by remember { mutableStateOf(emptySet<RunShareElement>()) }
    var darkTextStickers by remember { mutableStateOf(emptySet<RunShareSticker>()) }
    var regionName by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    LaunchedEffect(data.routePoints) {
        regionName = withContext(Dispatchers.IO) { context.resolveRunShareRegion(data) }
        if (regionName == null && RunShareSticker.Region in selectedStickers) {
            selectedStickers = selectedStickers - RunShareSticker.Region
            selectedSticker = null
        }
    }

    fun render(): Bitmap =
        RunShareRenderer.render(
            context = context,
            photo = photo,
            data = data,
            layout = layout,
            regionName = regionName,
            stickers = selectedStickers,
            elementTransforms = elementTransforms,
            stickerTransforms = stickerTransforms,
            darkElements = darkElements,
            darkTextStickers = darkTextStickers,
        )

    fun save() {
        if (isSaving) return
        scope.launch {
            isSaving = true
            message = null
            runCatching {
                val bitmap = withContext(Dispatchers.Default) { render() }
                saveRunShareBitmap(context, bitmap)
            }.onSuccess {
                message = "갤러리에 저장했어요."
                onSaved()
            }.onFailure {
                message = "사진을 저장하지 못했어요."
            }
            isSaving = false
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(
                        state = scrollState,
                        enabled = selectedElement == null && selectedSticker == null,
                    ),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            RunShareEditableCanvas(
                photo = photo,
                data = data,
                layout = layout,
                regionName = regionName,
                stickers = selectedStickers,
                selectedElement = selectedElement,
                selectedSticker = selectedSticker,
                elementTransforms = elementTransforms,
                stickerTransforms = stickerTransforms,
                darkElements = darkElements,
                darkTextStickers = darkTextStickers,
                onSelectElement = { element ->
                    if (selectedElement == element) darkElements = darkElements.toggle(element)
                    selectedElement = element
                    selectedSticker = null
                },
                onSelectSticker = { sticker ->
                    if (selectedSticker == sticker && sticker.supportsColorToggle) {
                        darkTextStickers = darkTextStickers.toggle(sticker)
                    }
                    selectedSticker = sticker
                    selectedElement = null
                },
                onElementTransform = { element, transform ->
                    elementTransforms = elementTransforms + (element to transform)
                },
                onStickerTransform = { sticker, transform ->
                    stickerTransforms = stickerTransforms + (sticker to transform)
                },
                onDeleteSticker = { sticker ->
                    selectedStickers = selectedStickers - sticker
                    stickerTransforms = stickerTransforms - sticker
                    darkTextStickers = darkTextStickers - sticker
                    selectedSticker = null
                },
                onClearSelection = {
                    selectedElement = null
                    selectedSticker = null
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(9f / 16.15f)
                        .clip(RoundedCornerShape(22.dp)),
            )

            Spacer(modifier = Modifier.height(18.dp))

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(EditorPanel)
                        .navigationBarsPadding()
                        .padding(vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                RunShareSegmentedControl(
                    selected = tab,
                    onSelected = { tab = it },
                    modifier = Modifier.padding(horizontal = 18.dp),
                )

                when (tab) {
                    RunShareEditorTab.Layout ->
                        RunShareLayoutGrid(
                            selected = layout,
                            onSelected = { option ->
                                layout = option
                                if (selectedElement !in option.activeElements()) selectedElement = null
                            },
                        )

                    RunShareEditorTab.Sticker ->
                        RunShareStickerGrid(
                            selected = selectedStickers,
                            canvasSelected = selectedSticker,
                            regionEnabled = regionName != null,
                            onSelected = { sticker ->
                                when {
                                    sticker !in selectedStickers -> {
                                        selectedStickers = selectedStickers + sticker
                                        selectedSticker = sticker
                                    }

                                    selectedSticker == sticker -> {
                                        selectedStickers = selectedStickers - sticker
                                        stickerTransforms = stickerTransforms - sticker
                                        darkTextStickers = darkTextStickers - sticker
                                        selectedSticker = null
                                    }

                                    else -> selectedSticker = sticker
                                }
                                selectedElement = null
                            },
                        )
                }

                Button(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 18.dp),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShareBlue),
                    onClick = {
                        scope.launch {
                            isSaving = true
                            message = null
                            runCatching {
                                val bitmap = withContext(Dispatchers.Default) { render() }
                                cacheRunShareBitmap(context, bitmap)
                            }.onSuccess { shareRunShareImage(context, it) }
                                .onFailure { message = "공유할 사진을 만들지 못했어요." }
                            isSaving = false
                        }
                    },
                ) {
                    Text("공유하기", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }

                if (message != null) {
                    Text(
                        text = message.orEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White.copy(alpha = 0.82f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        RunShareEditorHeader(
            isSaving = isSaving,
            onBack = onBack,
            onSave = ::save,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun RunShareEditorHeader(
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier =
                Modifier
                    .height(44.dp)
                    .width(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = !isSaving, onClick = onSave),
            contentAlignment = Alignment.Center,
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("저장", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RunShareSegmentedControl(
    selected: RunShareEditorTab,
    onSelected: (RunShareEditorTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(3.dp),
    ) {
        RunShareEditorTab.entries.forEach { tab ->
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected == tab) Color.White else Color.Transparent)
                        .clickable { onSelected(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.title,
                    color = if (selected == tab) Color.Black else Color.White.copy(alpha = 0.72f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun RunShareLayoutGrid(
    selected: RunShareLayout,
    onSelected: (RunShareLayout) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier =
            Modifier
                .fillMaxWidth()
                .height(104.dp),
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        userScrollEnabled = false,
    ) {
        items(RunShareLayout.entries) { layout ->
            RunShareOptionCard(
                title = layout.title,
                selected = selected == layout,
                onClick = { onSelected(layout) },
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (layout.showsRoute) Icons.Outlined.Map else Icons.Outlined.Straighten,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                    if (layout.showsDetails) {
                        Icon(
                            imageVector = Icons.Outlined.ViewModule,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.72f),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RunShareStickerGrid(
    selected: Set<RunShareSticker>,
    canvasSelected: RunShareSticker?,
    regionEnabled: Boolean,
    onSelected: (RunShareSticker) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier =
            Modifier
                .fillMaxWidth()
                .height(184.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false,
    ) {
        items(RunShareSticker.entries) { sticker ->
            val enabled = sticker != RunShareSticker.Region || regionEnabled
            RunShareOptionCard(
                title = sticker.title,
                selected = sticker in selected,
                enabled = enabled,
                previewBackground = Color.White.copy(alpha = 0.08f),
                onClick = { onSelected(sticker) },
                showCanvasSelection = canvasSelected == sticker,
            ) {
                RunShareStickerThumbnail(sticker = sticker)
            }
        }
    }
}

@Composable
private fun RunShareOptionCard(
    title: String,
    selected: Boolean,
    enabled: Boolean = true,
    showCanvasSelection: Boolean = false,
    previewBackground: Color = Color.Black.copy(alpha = 0.82f),
    onClick: () -> Unit,
    preview: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(previewBackground)
                    .then(
                        if (selected) Modifier.border(2.dp, ShareBlue, RoundedCornerShape(12.dp)) else Modifier,
                    ).graphicsLayer(alpha = if (enabled) 1f else 0.35f),
            contentAlignment = Alignment.Center,
        ) {
            preview()
            if (showCanvasSelection) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(6.dp)
                            .background(ShareBlue, CircleShape),
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            color = Color.White.copy(alpha = if (enabled) 0.75f else 0.32f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RunShareStickerThumbnail(sticker: RunShareSticker) {
    when (sticker) {
        RunShareSticker.Date ->
            Icon(
                Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )

        RunShareSticker.Region ->
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )

        else ->
            Image(
                painter = runShareStickerPainter(sticker),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(52.dp).padding(4.dp),
            )
    }
}

@Composable
private fun RunShareEditableCanvas(
    photo: Bitmap,
    data: RunShareData,
    layout: RunShareLayout,
    regionName: String?,
    stickers: Set<RunShareSticker>,
    selectedElement: RunShareElement?,
    selectedSticker: RunShareSticker?,
    elementTransforms: Map<RunShareElement, RunShareItemTransform>,
    stickerTransforms: Map<RunShareSticker, RunShareItemTransform>,
    darkElements: Set<RunShareElement>,
    darkTextStickers: Set<RunShareSticker>,
    onSelectElement: (RunShareElement) -> Unit,
    onSelectSticker: (RunShareSticker) -> Unit,
    onElementTransform: (RunShareElement, RunShareItemTransform) -> Unit,
    onStickerTransform: (RunShareSticker, RunShareItemTransform) -> Unit,
    onDeleteSticker: (RunShareSticker) -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier =
            modifier
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClearSelection,
                ),
    ) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()
        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()

        Image(
            bitmap = photo.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.72f),
                            0.50f to Color.Black.copy(alpha = 0.08f),
                            1f to Color.Black.copy(alpha = 0.78f),
                        ),
                    ),
        )

        if (layout.showsRoute) {
            val transform = elementTransforms[RunShareElement.Route] ?: RunShareItemTransform()
            RunShareTransformableOverlay(
                selected = selectedElement == RunShareElement.Route,
                transform = transform,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                onSelect = { onSelectElement(RunShareElement.Route) },
                onTransform = { onElementTransform(RunShareElement.Route, it) },
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 52.dp)
                        .fillMaxWidth(0.84f)
                        .height(maxHeight * 0.30f),
            ) {
                RunShareRoute(
                    data = data,
                    color = if (RunShareElement.Route in darkElements) Color.Black else Color.White,
                )
            }
        }

        val dataTransform = elementTransforms[RunShareElement.Data] ?: RunShareItemTransform()
        RunShareTransformableOverlay(
            selected = selectedElement == RunShareElement.Data,
            transform = dataTransform,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            onSelect = { onSelectElement(RunShareElement.Data) },
            onTransform = { onElementTransform(RunShareElement.Data, it) },
            selectionHandleInset = TextSelectionHandleInset,
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 82.dp),
        ) {
            RunShareDataContent(
                data = data,
                showDetails = layout.showsDetails,
                color = if (RunShareElement.Data in darkElements) Color.Black else Color.White,
            )
        }

        stickers.forEach { sticker ->
            val transform = stickerTransforms[sticker] ?: RunShareItemTransform()
            val assetBounds = sticker.assetBounds
            val (width, height) =
                when (sticker) {
                    RunShareSticker.Date -> {
                        val measured =
                            textMeasurer
                                .measure(
                                    text = data.date,
                                    style = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                ).size
                        with(density) { (measured.width.toDp() + 4.dp) to (measured.height.toDp() + 2.dp) }
                    }

                    RunShareSticker.Region -> {
                        val measured =
                            textMeasurer
                                .measure(
                                    text = regionName.orEmpty(),
                                    style = TextStyle(fontSize = 21.sp, fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                ).size
                        with(density) {
                            (28.dp + measured.width.toDp()) to maxOf(22.dp, measured.height.toDp() + 2.dp)
                        }
                    }

                    else -> {
                        val bitmapWidth = maxWidth * sticker.widthRatio
                        bitmapWidth to bitmapWidth / requireNotNull(assetBounds).aspectRatio
                    }
                }
            val baseX = canvasWidth * sticker.defaultX
            val baseY = canvasHeight * sticker.defaultY
            RunShareTransformableOverlay(
                selected = selectedSticker == sticker,
                transform = transform,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                onSelect = { onSelectSticker(sticker) },
                onTransform = { onStickerTransform(sticker, it) },
                onDelete = { onDeleteSticker(sticker) },
                selectionHandleInset = if (sticker.assetBounds == null) TextSelectionHandleInset else 0.dp,
                modifier =
                    Modifier
                        .offset {
                            IntOffset(
                                x = (baseX - with(density) { width.toPx() } / 2f).roundToInt(),
                                y = (baseY - with(density) { height.toPx() } / 2f).roundToInt(),
                            )
                        }.width(width)
                        .height(height),
            ) {
                RunShareStickerContent(
                    sticker = sticker,
                    data = data,
                    regionName = regionName,
                    color = if (sticker in darkTextStickers) Color.Black else Color.White,
                )
            }
        }
    }
}

@Composable
private fun RunShareTransformableOverlay(
    selected: Boolean,
    transform: RunShareItemTransform,
    canvasWidth: Float,
    canvasHeight: Float,
    onSelect: () -> Unit,
    onTransform: (RunShareItemTransform) -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
    selectionHandleInset: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    val latestTransform by rememberUpdatedState(transform)
    val density = LocalDensity.current
    val handleOffset = with(density) { (15.dp + selectionHandleInset).toPx() }
    val rotationOffset = 34.dp + selectionHandleInset
    val deleteOffset = with(density) { (34.dp + selectionHandleInset).toPx() }
    Box(
        modifier =
            modifier
                .graphicsLayer(
                    translationX = transform.offsetXFraction * canvasWidth,
                    translationY = transform.offsetYFraction * canvasHeight,
                    scaleX = transform.scale,
                    scaleY = transform.scale,
                    rotationZ = transform.rotationDegrees,
                ).clickable(onClick = onSelect)
                .pointerInput(canvasWidth, canvasHeight) {
                    var workingTransform = RunShareItemTransform()
                    detectDragGestures(
                        onDragStart = {
                            workingTransform = latestTransform
                        },
                        onDrag = { change, pan ->
                            change.consume()
                            workingTransform =
                                workingTransform.copy(
                                    offsetXFraction = workingTransform.offsetXFraction + pan.x / canvasWidth,
                                    offsetYFraction = workingTransform.offsetYFraction + pan.y / canvasHeight,
                                )
                            onTransform(workingTransform)
                        },
                    )
                },
    ) {
        content()
        if (selected) {
            listOf(Alignment.TopStart, Alignment.TopEnd, Alignment.BottomStart, Alignment.BottomEnd).forEach { alignment ->
                Box(
                    modifier =
                        Modifier
                            .align(alignment)
                            .size(30.dp)
                            .graphicsLayer(
                                translationX =
                                    if (alignment == Alignment.TopStart ||
                                        alignment == Alignment.BottomStart
                                    ) {
                                        -handleOffset
                                    } else {
                                        handleOffset
                                    },
                                translationY =
                                    if (alignment == Alignment.TopStart ||
                                        alignment == Alignment.TopEnd
                                    ) {
                                        -handleOffset
                                    } else {
                                        handleOffset
                                    },
                            ).pointerInput(alignment) {
                                var workingTransform = RunShareItemTransform()
                                detectDragGestures(
                                    onDragStart = { workingTransform = latestTransform },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val signedDelta =
                                            when (alignment) {
                                                Alignment.TopStart -> -(dragAmount.x + dragAmount.y) / 2f
                                                Alignment.TopEnd -> (dragAmount.x - dragAmount.y) / 2f
                                                Alignment.BottomStart -> (-dragAmount.x + dragAmount.y) / 2f
                                                else -> (dragAmount.x + dragAmount.y) / 2f
                                            }
                                        workingTransform =
                                            workingTransform.copy(
                                                scale =
                                                    (workingTransform.scale + signedDelta / 200.dp.toPx())
                                                        .coerceIn(0.4f, 2.5f),
                                            )
                                        onTransform(workingTransform)
                                    },
                                )
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(14.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, Color.Black.copy(alpha = 0.55f), CircleShape),
                    )
                }
            }
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = -rotationOffset)
                        .size(34.dp)
                        .pointerInput(Unit) {
                            var workingTransform = RunShareItemTransform()
                            detectDragGestures(
                                onDragStart = { workingTransform = latestTransform },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    workingTransform =
                                        workingTransform.copy(
                                            rotationDegrees =
                                                normalizeDegrees(workingTransform.rotationDegrees + dragAmount.x / 2f),
                                        )
                                    onTransform(workingTransform)
                                },
                            )
                        },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.size(18.dp).background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
            if (onDelete != null) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(36.dp)
                            .graphicsLayer(
                                translationX = deleteOffset,
                                translationY = -deleteOffset,
                            ).clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier.size(22.dp).background(Color(0xFFEB333D), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "스티커 삭제", tint = Color.White, modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RunShareDataContent(
    data: RunShareData,
    showDetails: Boolean,
    color: Color,
) {
    val density = LocalDensity.current
    var distanceRowWidthPx by remember(data.distance) { mutableIntStateOf(0) }
    val distanceRowWidth = with(density) { distanceRowWidthPx.toDp() }

    Column {
        Column {
            Image(
                painter = painterResource(R.drawable.runpamine_share_logo),
                contentDescription = "런파민",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.width(if (distanceRowWidthPx > 0) distanceRowWidth else 1.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.onSizeChanged { distanceRowWidthPx = it.width },
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(data.distance, color = color, fontSize = 66.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    "KM",
                    color = color.copy(alpha = 0.88f),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                )
            }
        }
        if (showDetails) {
            Row(
                modifier = Modifier.padding(top = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                RunShareMetricContent("TIME", data.time, color)
                RunShareMetricContent("PACE", data.pace, color)
                RunShareMetricContent("KCAL", data.calories, color)
            }
        }
    }
}

@Composable
private fun RunShareMetricContent(
    label: String,
    value: String,
    color: Color,
) {
    Column {
        Text(label, color = color.copy(alpha = 0.70f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(value, color = color, fontSize = 21.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RunShareRoute(
    data: RunShareData,
    color: Color,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val polyline = runShareRoutePolyline(data.routePoints, size.width, size.height, 24.dp.toPx())
        if (polyline.size < 2) return@Canvas
        val path = Path()
        polyline.forEachIndexed { index, point ->
            if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
        }
        drawPath(path, color, style = Stroke(width = 5.dp.toPx()))
    }
}

@Composable
private fun RunShareStickerContent(
    sticker: RunShareSticker,
    data: RunShareData,
    regionName: String?,
    color: Color,
) {
    when (sticker) {
        RunShareSticker.Date ->
            Text(
                text = data.date,
                color = color,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )

        RunShareSticker.Region ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = color, modifier = Modifier.size(18.dp, 22.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(regionName.orEmpty(), color = color, fontSize = 21.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }

        else ->
            Image(
                painter = runShareStickerPainter(sticker),
                contentDescription = sticker.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
    }
}

@Composable
private fun runShareStickerPainter(sticker: RunShareSticker): Painter {
    val bounds = requireNotNull(sticker.assetBounds)
    val resources = LocalContext.current.resources
    val image =
        remember(sticker, resources) {
            ImageBitmap.imageResource(resources, sticker.drawableResource())
        }
    return remember(image, bounds) {
        BitmapPainter(
            image = image,
            srcOffset = IntOffset(bounds.left, bounds.top),
            srcSize = IntSize(bounds.width, bounds.height),
        )
    }
}

private fun RunShareSticker.drawableResource(): Int =
    when (this) {
        RunShareSticker.Pamin -> R.drawable.pamin_sticker
        RunShareSticker.Cheetah -> R.drawable.cheetah_pamin_sticker
        RunShareSticker.Surprised -> R.drawable.surprised_pamin_sticker
        RunShareSticker.Hamburger -> R.drawable.hamburger_pamin_sticker
        RunShareSticker.Handstand -> R.drawable.handstand_pamin_sticker
        RunShareSticker.Date,
        RunShareSticker.Region,
        -> error("Text stickers do not have drawable resources")
    }

private fun normalizeDegrees(value: Float): Float {
    var normalized = value % 360f
    if (normalized > 180f) normalized -= 360f
    if (normalized < -180f) normalized += 360f
    return normalized
}

private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value

@Suppress("DEPRECATION")
private fun Context.resolveRunShareRegion(data: RunShareData): String? {
    val point = data.routePoints.minByOrNull { it.sequence } ?: return null
    return runCatching {
        val address = Geocoder(this, Locale.KOREAN).getFromLocation(point.latitude, point.longitude, 1)?.firstOrNull()
        sequenceOf(address?.subAdminArea, address?.locality, address?.adminArea)
            .filterNotNull()
            .map { it.substringAfterLast(' ') }
            .firstOrNull { value -> value.endsWith("시") || value.endsWith("군") || value.endsWith("구") }
            ?: address?.locality
            ?: address?.adminArea
    }.getOrNull()
}
