package com.harborreel.app.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.harborreel.app.R
import com.harborreel.engine.CruiseLines

private val WordmarkRed = Color(0xFFEE2E24)
private val ScriptRed = Color(0xFFE10A17)
private val WordmarkIvory = Color(0xFFF6F1E4)

/** Room above the cap line for rounded and italic overshoot, as a fraction of cap height. */
private const val OVERSHOOT = 0.10f

/** Room below the baseline for descenders, as a fraction of cap height. */
private const val DESCENDER = 0.50f

/** Lowercase height relative to the capital, so the C is only slightly taller. */
private const val CARNIVORE_LOWER_OF_CAP = 0.90f

/** Martel lowercase x-height, baseline to the top of "x", as a fraction of the em. */
private const val MARTEL_X_HEIGHT = 0.546f

private data class LineFace(
    val font: Int,
    val color: Color,
    /** Baseline to the flat top of the capitals, as a fraction of the em. */
    val capToEm: Float,
    val bold: Boolean = false,
)

private val faces: Map<String, LineFace> = mapOf(
    "carnival" to LineFace(R.font.martel_heavy, WordmarkRed, capToEm = 0.765f),
    "royal" to LineFace(R.font.eb_garamond_regular, WordmarkIvory, capToEm = 0.663f, bold = true),
    "norwegian" to LineFace(R.font.jost_bold_italic, Color.White, capToEm = 0.700f),
    "princess" to LineFace(R.font.inter_bold, Color.White, capToEm = 0.737f),
    "celebrity" to LineFace(R.font.roboto_regular, Color.White, capToEm = 0.721f, bold = true),
    "holland" to LineFace(R.font.playfair_display_bold, WordmarkIvory, capToEm = 0.722f),
    "msc" to LineFace(R.font.dm_sans_semibold, Color.White, capToEm = 0.700f),
    "virgin" to LineFace(R.font.caveat_semibold, ScriptRed, capToEm = 0.688f),
)

@Composable
fun CruiseLineName(
    lineId: String,
    capHeight: Dp,
    emojiBox: Dp,
    centered: Boolean,
    modifier: Modifier = Modifier,
) {
    val spoken = CruiseLines.nameOf(lineId)
    val emoji = spoken.substringBefore(' ')
    val label = spoken.substringAfter(' ')
    val face = faces[lineId] ?: faces.getValue(CruiseLines.defaultId)
    val context = LocalContext.current
    val typeface = remember(face.font) { context.resources.getFont(face.font) }
    val above = maxOf(capHeight * (OVERSHOOT + 0.5f), emojiBox / 2)
    val below = maxOf(capHeight * (0.5f + DESCENDER), emojiBox / 2)
    val block = above + below
    Canvas(
        modifier
            .fillMaxWidth()
            .height(block)
            .semantics { contentDescription = spoken },
    ) {
        val capPx = capHeight.toPx()
        val emojiPx = emojiBox.toPx()
        val gapPx = 6.dp.toPx()
        val capCenter = above.toPx()
        val baseline = capCenter + capPx / 2f

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            this.typeface = typeface
            this.color = face.color.toArgb()
            isFakeBoldText = face.bold
            textSize = capPx / face.capToEm
        }
        val lowerPaint = if (lineId == "carnival") {
            Paint(textPaint).apply {
                textSize = capPx * CARNIVORE_LOWER_OF_CAP / MARTEL_X_HEIGHT
            }
        } else {
            null
        }
        val natural = textWidth(lineId, label, textPaint, lowerPaint)
        val available = (size.width - emojiPx - gapPx).coerceAtLeast(0f)
        val scale = if (natural > available && natural > 0f) available / natural else 1f
        val textW = natural * scale
        val group = emojiPx + gapPx + textW
        val left = if (centered) ((size.width - group) / 2f).coerceAtLeast(0f) else 0f

        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = emojiPx * 0.92f
        }
        val emojiMetrics = emojiPaint.fontMetrics
        val emojiTop = capCenter - emojiPx / 2f
        val emojiBaseline = emojiTop + emojiPx / 2f - (emojiMetrics.ascent + emojiMetrics.descent) / 2f
        val native = drawContext.canvas.nativeCanvas
        native.save()
        native.clipRect(left, emojiTop, left + emojiPx, emojiTop + emojiPx)
        native.drawText(emoji, left + emojiPx / 2f, emojiBaseline, emojiPaint)
        native.restore()

        native.save()
        native.translate(left + emojiPx + gapPx, baseline)
        native.scale(scale, 1f)
        drawLabel(lineId, label, textPaint, lowerPaint, capPx, native)
        native.restore()
    }
}

private fun textWidth(lineId: String, label: String, text: Paint, lower: Paint?): Float {
    if (lineId != "carnival" || lower == null || label.isEmpty()) return text.measureText(label)
    val rest = label.drop(1).replace("i", "\u0131")
    return text.measureText(label.take(1)) + lower.measureText(rest)
}

private fun drawLabel(
    lineId: String,
    label: String,
    text: Paint,
    lower: Paint?,
    capPx: Float,
    canvas: android.graphics.Canvas,
) {
    if (lineId != "carnival" || lower == null || label.isEmpty()) {
        canvas.drawText(label, 0f, 0f, text)
        return
    }
    val capital = label.take(1)
    val rest = label.drop(1).replace("i", "\u0131")
    canvas.drawText(capital, 0f, 0f, text)
    val capitalWidth = text.measureText(capital)
    canvas.drawText(rest, capitalWidth, 0f, lower)
    val stem = "\u0131"
    val before = rest.substringBefore(stem)
    if (before.length < rest.length) {
        val stemStart = lower.measureText(before)
        val stemWidth = lower.measureText(stem)
        val xTop = capPx * CARNIVORE_LOWER_OF_CAP
        val gap = capPx - xTop
        val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = text.color }
        canvas.drawCircle(
            capitalWidth + stemStart + stemWidth / 2f,
            -(xTop + gap / 2f),
            gap * 0.42f,
            dot,
        )
    }
}
