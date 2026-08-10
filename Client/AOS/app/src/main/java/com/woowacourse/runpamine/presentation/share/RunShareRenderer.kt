package com.woowacourse.runpamine.presentation.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import com.woowacourse.runpamine.R
import kotlin.math.max

internal object RunShareRenderer {
    private const val CANVAS_WIDTH = 1080
    private const val CANVAS_HEIGHT = 1938
    private const val MIN_SCALE = 0.4f
    private const val MAX_SCALE = 2.5f
    private const val SHARE_LOGO_HEIGHT_RATIO = 74f / 368f

    fun render(
        context: Context,
        photo: Bitmap,
        data: RunShareData,
        layout: RunShareLayout,
        regionName: String?,
        stickers: Set<RunShareSticker>,
        elementTransforms: Map<RunShareElement, RunShareItemTransform> = emptyMap(),
        stickerTransforms: Map<RunShareSticker, RunShareItemTransform> = emptyMap(),
        darkElements: Set<RunShareElement> = emptySet(),
        darkTextStickers: Set<RunShareSticker> = emptySet(),
    ): Bitmap =
        renderInternal(
            context = context,
            photo = photo,
            data = data,
            layout = layout,
            regionName = regionName,
            stickers = stickers,
            elementTransforms = elementTransforms,
            stickerTransforms = stickerTransforms,
            darkElements = darkElements,
            darkTextStickers = darkTextStickers,
        )

    fun render(
        photo: Bitmap,
        data: RunShareData,
        layout: RunShareLayout,
        stickers: Set<RunShareSticker>,
    ): Bitmap =
        renderInternal(
            context = null,
            photo = photo,
            data = data,
            layout = layout,
            regionName = null,
            stickers = stickers,
            elementTransforms = emptyMap(),
            stickerTransforms = emptyMap(),
            darkElements = emptySet(),
            darkTextStickers = emptySet(),
        )

    private fun renderInternal(
        context: Context?,
        photo: Bitmap,
        data: RunShareData,
        layout: RunShareLayout,
        regionName: String?,
        stickers: Set<RunShareSticker>,
        elementTransforms: Map<RunShareElement, RunShareItemTransform>,
        stickerTransforms: Map<RunShareSticker, RunShareItemTransform>,
        darkElements: Set<RunShareElement>,
        darkTextStickers: Set<RunShareSticker>,
    ): Bitmap {
        val output = createBitmap(CANVAS_WIDTH, CANVAS_HEIGHT)
        val canvas = Canvas(output)

        drawPhoto(canvas, photo)
        drawScrim(canvas)
        layout.activeElements().forEach { element ->
            drawElement(
                context = context,
                canvas = canvas,
                data = data,
                layout = layout,
                element = element,
                transform = elementTransforms[element].orIdentity(),
                color = if (darkElements.contains(element)) Color.BLACK else Color.WHITE,
            )
        }
        RunShareSticker.entries.filter(stickers::contains).forEach { sticker ->
            drawSticker(
                context = context,
                canvas = canvas,
                data = data,
                regionName = regionName,
                sticker = sticker,
                transform = stickerTransforms[sticker].orIdentity(),
                color = if (darkTextStickers.contains(sticker)) Color.BLACK else Color.WHITE,
            )
        }
        return output
    }

    private fun drawPhoto(
        canvas: Canvas,
        photo: Bitmap,
    ) {
        val srcRatio = photo.width.toFloat() / photo.height.toFloat()
        val dstRatio = CANVAS_WIDTH.toFloat() / CANVAS_HEIGHT.toFloat()
        val src =
            if (srcRatio > dstRatio) {
                val cropWidth = (photo.height * dstRatio).toInt()
                val left = (photo.width - cropWidth) / 2
                Rect(left, 0, left + cropWidth, photo.height)
            } else {
                val cropHeight = (photo.width / dstRatio).toInt()
                val top = (photo.height - cropHeight) / 2
                Rect(0, top, photo.width, top + cropHeight)
            }
        canvas.drawBitmap(photo, src, Rect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT), null)
    }

    private fun drawScrim(canvas: Canvas) {
        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader =
                    LinearGradient(
                        0f,
                        0f,
                        0f,
                        CANVAS_HEIGHT.toFloat(),
                        intArrayOf(Color.argb(184, 0, 0, 0), Color.argb(20, 0, 0, 0), Color.argb(199, 0, 0, 0)),
                        floatArrayOf(0f, 0.5f, 1f),
                        Shader.TileMode.CLAMP,
                    )
            }
        canvas.drawRect(0f, 0f, CANVAS_WIDTH.toFloat(), CANVAS_HEIGHT.toFloat(), paint)
    }

    private fun drawElement(
        context: Context?,
        canvas: Canvas,
        data: RunShareData,
        layout: RunShareLayout,
        element: RunShareElement,
        transform: RunShareItemTransform,
        color: Int,
    ) {
        val size =
            when (element) {
                RunShareElement.Data -> dataGroupSize(data, layout)
                RunShareElement.Route -> routeElementSize(data)
            }
        val center =
            when (element) {
                RunShareElement.Data -> dataGroupCenter(size, transform.coercedScale())
                RunShareElement.Route -> RunShareOffset(x = CANVAS_WIDTH / 2f, y = 90f + size.y / 2f)
            }

        drawTransformed(canvas, center, size, transform) {
            when (element) {
                RunShareElement.Data -> drawMetrics(context, canvas, data, layout, color)
                RunShareElement.Route -> drawRoute(canvas, data, size, color)
            }
        }
    }

    private fun drawMetrics(
        context: Context?,
        canvas: Canvas,
        data: RunShareData,
        layout: RunShareLayout,
        color: Int,
    ) {
        val distancePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                textSize = 198f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        val unitPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = withAlpha(color, 0.88f)
                textSize = 66f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        val distanceWidth = distancePaint.measureText(data.distance)
        val suffixLeft = distanceWidth + 24f
        val unitWidth = unitPaint.measureText("KM")
        val distanceRowWidth = suffixLeft + unitWidth
        val logoBlockHeight = distanceRowWidth * SHARE_LOGO_HEIGHT_RATIO + 12f
        val distanceBaseline = logoBlockHeight + 198f
        drawDataBrand(context, canvas, 0f, 0f, distanceRowWidth, color)
        canvas.drawText(data.distance, 0f, distanceBaseline, distancePaint)
        canvas.drawText("KM", suffixLeft, distanceBaseline - 10f, unitPaint)

        if (!layout.showsDetails) return

        val labelPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = withAlpha(color, 0.70f)
                textSize = 36f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                letterSpacing = 0.08f
            }
        val valuePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                textSize = 63f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        var x = 0f
        listOf("TIME" to data.time, "PACE" to data.pace, "KCAL" to data.calories).forEach { (label, value) ->
            canvas.drawText(label, x, logoBlockHeight + 294f, labelPaint)
            canvas.drawText(value, x, logoBlockHeight + 369f, valuePaint)
            x += max(valuePaint.measureText(value), labelPaint.measureText(label)) + 84f
        }
    }

    private fun drawRoute(
        canvas: Canvas,
        data: RunShareData,
        size: RunShareOffset,
        color: Int,
    ) {
        val points = runShareRoutePolyline(data.routePoints, size.x, size.y, 72f)
        if (points.isEmpty()) {
            val textPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = withAlpha(color, 0.72f)
                    textSize = 39f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
            val iconPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = withAlpha(color, 0.72f)
                    style = Paint.Style.STROKE
                    strokeWidth = 5f
                }
            val centerX = size.x / 2f
            canvas.drawRect(centerX - 30f, size.y / 2f - 66f, centerX + 30f, size.y / 2f - 18f, iconPaint)
            canvas.drawText("러닝 루트 없음", centerX, size.y / 2f + 42f, textPaint)
            return
        }

        val path = Path()
        points.forEachIndexed { index, point ->
            if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
        }
        val routePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                strokeWidth = 15f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                style = Paint.Style.STROKE
            }
        canvas.drawPath(path, routePaint)
    }

    private fun drawSticker(
        context: Context?,
        canvas: Canvas,
        data: RunShareData,
        regionName: String?,
        sticker: RunShareSticker,
        transform: RunShareItemTransform,
        color: Int,
    ) {
        if (sticker == RunShareSticker.Region && regionName.isNullOrBlank()) return

        val size = stickerSize(sticker, data, regionName)
        val center = stickerCenter(sticker, size)
        drawTransformed(canvas, center, size, transform) {
            when (sticker) {
                RunShareSticker.Date -> drawDateSticker(canvas, data.date, color)
                RunShareSticker.Region -> drawRegionSticker(canvas, regionName.orEmpty(), color, size)
                else -> drawImageSticker(context, canvas, sticker, size)
            }
        }
    }

    private fun drawDateSticker(
        canvas: Canvas,
        date: String,
        color: Int,
    ) {
        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                textSize = 57f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        canvas.drawText(date, 0f, 62f, paint)
    }

    private fun drawRegionSticker(
        canvas: Canvas,
        regionName: String,
        color: Int,
        size: RunShareOffset,
    ) {
        val iconPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                style = Paint.Style.STROKE
                strokeWidth = 6f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
        val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                textSize = 63f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        val iconLeft = 0f
        val iconTop = (size.y - 66f) / 2f
        val pin =
            Path().apply {
                moveTo(iconLeft + 27f, iconTop + 66f)
                cubicTo(iconLeft + 18f, iconTop + 53f, iconLeft + 3f, iconTop + 40f, iconLeft + 3f, iconTop + 26f)
                cubicTo(iconLeft + 3f, iconTop + 9f, iconLeft + 14f, iconTop + 3f, iconLeft + 27f, iconTop + 3f)
                cubicTo(iconLeft + 40f, iconTop + 3f, iconLeft + 51f, iconTop + 9f, iconLeft + 51f, iconTop + 26f)
                cubicTo(iconLeft + 51f, iconTop + 40f, iconLeft + 36f, iconTop + 53f, iconLeft + 27f, iconTop + 66f)
            }
        canvas.drawPath(pin, iconPaint)
        canvas.drawCircle(iconLeft + 27f, iconTop + 24f, 9f, iconPaint)
        canvas.drawText(regionName, 72f, 65f, textPaint)
    }

    private fun drawImageSticker(
        context: Context?,
        canvas: Canvas,
        sticker: RunShareSticker,
        size: RunShareOffset,
    ) {
        val bitmap = context?.let { BitmapFactory.decodeResource(it.resources, sticker.drawableResId()) }
        if (bitmap == null) {
            val fallbackPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 42f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
            canvas.drawText(sticker.title, size.x / 2f, size.y / 2f, fallbackPaint)
            return
        }
        val bounds = requireNotNull(sticker.assetBounds)
        canvas.drawBitmap(
            bitmap,
            android.graphics.Rect(bounds.left, bounds.top, bounds.left + bounds.width, bounds.top + bounds.height),
            RectF(0f, 0f, size.x, size.y),
            Paint(Paint.ANTI_ALIAS_FLAG),
        )
    }

    private fun drawDataBrand(
        context: Context?,
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        color: Int,
    ) {
        val textLogo =
            context?.let { BitmapFactory.decodeResource(it.resources, R.drawable.runpamine_share_logo_text) }
        if (textLogo == null) {
            val paint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = color
                    textSize = 30f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
            canvas.drawText("RUNPAMINE", left + width / 2f, top + 38f, paint)
            return
        }

        val height = width * textLogo.height / textLogo.width
        val dst = RectF(left, top, left + width, top + height)
        // 글자는 데이터 색상 토글에 맞춰 틴트하고, 러너 캐릭터는 원색을 유지한다.
        val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        canvas.drawBitmap(textLogo, null, dst, textPaint)

        val runnerLogo =
            context.let { BitmapFactory.decodeResource(it.resources, R.drawable.runpamine_share_logo_runner) }
        if (runnerLogo != null) {
            canvas.drawBitmap(runnerLogo, null, dst, Paint(Paint.ANTI_ALIAS_FLAG))
        }
    }

    private fun drawTransformed(
        canvas: Canvas,
        center: RunShareOffset,
        size: RunShareOffset,
        transform: RunShareItemTransform,
        drawContent: () -> Unit,
    ) {
        val scale = transform.coercedScale()
        canvas.save()
        canvas.translate(
            center.x + transform.offsetXFraction * CANVAS_WIDTH,
            center.y + transform.offsetYFraction * CANVAS_HEIGHT,
        )
        canvas.rotate(transform.rotationDegrees)
        canvas.scale(scale, scale)
        canvas.translate(-size.x / 2f, -size.y / 2f)
        drawContent()
        canvas.restore()
    }

    private fun dataGroupSize(
        data: RunShareData,
        layout: RunShareLayout,
    ): RunShareOffset {
        val distancePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 198f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        val unitPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 66f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        val distanceWidth = distancePaint.measureText(data.distance) + 24f + unitPaint.measureText("KM")
        val logoBlockHeight = distanceWidth * SHARE_LOGO_HEIGHT_RATIO + 12f
        if (!layout.showsDetails) return RunShareOffset(x = distanceWidth, y = 216f + logoBlockHeight)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 36f }
        val valuePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 63f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        val dataWidth =
            listOf("TIME" to data.time, "PACE" to data.pace, "KCAL" to data.calories)
                .sumOf { (label, value) -> max(labelPaint.measureText(label), valuePaint.measureText(value)).toDouble() }
                .toFloat() + 168f
        return RunShareOffset(x = max(distanceWidth, dataWidth), y = 384f + logoBlockHeight)
    }

    private fun dataGroupCenter(
        size: RunShareOffset,
        savedScale: Float,
    ): RunShareOffset {
        val top = CANVAS_HEIGHT - 246f - size.y * savedScale
        return RunShareOffset(
            x = 60f + size.x * savedScale / 2f,
            y = top + size.y * savedScale / 2f,
        )
    }

    private fun routeElementSize(data: RunShareData): RunShareOffset {
        val maxWidth = CANVAS_WIDTH - 180f
        val maxHeight = CANVAS_HEIGHT * 0.38f
        if (data.routePoints.size < 2) return RunShareOffset(maxWidth, maxHeight)

        val points = runShareRoutePolyline(data.routePoints, maxWidth, maxHeight, 72f)
        if (points.isEmpty()) return RunShareOffset(maxWidth, maxHeight)

        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }
        return RunShareOffset(
            x = maxX - minX + 144f,
            y = maxY - minY + 144f,
        )
    }

    private fun stickerSize(
        sticker: RunShareSticker,
        data: RunShareData,
        regionName: String?,
    ): RunShareOffset =
        when (sticker) {
            RunShareSticker.Date -> {
                val paint =
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        textSize = 57f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                RunShareOffset(x = minOf(paint.measureText(data.date) + 12f, CANVAS_WIDTH * 0.55f), y = 78f)
            }
            RunShareSticker.Region -> {
                val paint =
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        textSize = 63f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                RunShareOffset(x = 72f + paint.measureText(regionName.orEmpty()) + 12f, y = 78f)
            }
            else -> {
                val width = CANVAS_WIDTH * sticker.widthRatio
                RunShareOffset(width, width / requireNotNull(sticker.assetBounds).aspectRatio)
            }
        }

    private fun stickerCenter(
        sticker: RunShareSticker,
        size: RunShareOffset,
    ): RunShareOffset =
        when (sticker) {
            RunShareSticker.Date -> RunShareOffset(x = 60f + size.x / 2f, y = CANVAS_HEIGHT - 90f)
            RunShareSticker.Region -> RunShareOffset(x = CANVAS_WIDTH - 96f - size.x / 2f, y = CANVAS_HEIGHT * 0.40f)
            else -> RunShareOffset(x = CANVAS_WIDTH * sticker.defaultX, y = CANVAS_HEIGHT * sticker.defaultY)
        }

    private fun RunShareItemTransform?.orIdentity(): RunShareItemTransform = this ?: RunShareItemTransform()

    private fun RunShareItemTransform.coercedScale(): Float = scale.coerceIn(MIN_SCALE, MAX_SCALE)

    private fun withAlpha(
        color: Int,
        alpha: Float,
    ): Int = Color.argb((255 * alpha).toInt(), Color.red(color), Color.green(color), Color.blue(color))

    private fun RunShareSticker.drawableResId(): Int =
        when (this) {
            RunShareSticker.Pamin -> R.drawable.pamin_sticker
            RunShareSticker.Cheetah -> R.drawable.cheetah_pamin_sticker
            RunShareSticker.Surprised -> R.drawable.surprised_pamin_sticker
            RunShareSticker.Hamburger -> R.drawable.hamburger_pamin_sticker
            RunShareSticker.Handstand -> R.drawable.handstand_pamin_sticker
            RunShareSticker.Date,
            RunShareSticker.Region,
            -> error("$name does not have a bitmap asset")
        }
}
