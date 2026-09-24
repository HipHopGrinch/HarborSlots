package com.harborreel.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.harborreel.app.R
import com.harborreel.engine.WHEEL_WEDGES
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ChaseLights(modifier: Modifier = Modifier) {
    val phase by rememberInfiniteTransition(label = "marquee").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label = "phase",
    )
    Canvas(modifier) {
        val count = 26
        val inset = 7.dp.toPx()
        val radius = 4.5.dp.toPx()
        val width = size.width - inset * 2
        val height = size.height - inset * 2
        val perimeter = 2f * (width + height)
        repeat(count) { index ->
            val distance = ((index.toFloat() / count) + phase) % 1f * perimeter
            val point = pointOnRect(distance, width, height, inset)
            val lamp = ((index + (phase * count).toInt()) % 3)
            val color = when (lamp) {
                0 -> Gold
                1 -> Color(0xFFFFF6C8)
                else -> Teal
            }
            val bright = lamp != 2 || (index % 2 == 0)
            drawCircle(color.copy(alpha = if (bright) 0.95f else 0.28f), radius, point)
        }
    }
}

private fun pointOnRect(distance: Float, width: Float, height: Float, inset: Float): Offset {
    val d = distance
    return when {
        d <= width -> Offset(inset + d, inset)
        d <= width + height -> Offset(inset + width, inset + (d - width))
        d <= width * 2 + height -> Offset(inset + width - (d - width - height), inset + height)
        else -> Offset(inset, inset + height - (d - width * 2 - height))
    }
}

@Composable
fun WinCelebration(amount: Long, modifier: Modifier = Modifier) {
    val drift by rememberInfiniteTransition(label = "win").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "drift",
    )
    val pulse by rememberInfiniteTransition(label = "banner").animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(420), RepeatMode.Reverse),
        label = "pulse",
    )
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            repeat(22) { index ->
                val angle = index * (PI.toFloat() * 2f / 22f) + drift * PI.toFloat() * 2f
                val radius = size.minDimension * (0.22f + (index % 5) * 0.06f)
                val center = Offset(size.width / 2f, size.height / 2f)
                val spark = Offset(center.x + cos(angle) * radius, center.y + sin(angle) * radius * 0.72f)
                val color = if (index % 2 == 0) Gold else Color(0xFFFFF4C2)
                drawCircle(color.copy(alpha = 0.85f), 3.5.dp.toPx() + (index % 3), spark)
            }
        }
        Text(
            "WIN  ${amount.money()}",
            color = Color(0xFF1A1204),
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                }
                .background(Gold, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
fun SceneLife(gameId: String, modifier: Modifier = Modifier) {
    val clock by rememberInfiniteTransition(label = "scene").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing)),
        label = "clock",
    )
    Box(modifier) {
        when (gameId) {
            "harbor" -> {
                val flight by rememberInfiniteTransition(label = "flight").animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)),
                    label = "flight",
                )
                FlyingGull(flight, phase = 0f, gullHeight = 72.dp)
                FlyingGull(flight, phase = 0.5f, gullHeight = 52.dp)
                Mascot(R.drawable.cast_harbor, clock, Alignment.BottomCenter, 0.96f)
            }
            "brightwork" -> {
                Mascot(R.drawable.cast_bright, clock, Alignment.BottomCenter, 0.96f, sway = 5f)
            }
            "market" -> {
                Mascot(R.drawable.cast_market, clock, Alignment.BottomCenter, 0.98f, sway = 4f)
            }
            "patch" -> {
                Mascot(R.drawable.cast_patch, clock, Alignment.BottomCenter, 0.96f, sway = 3.5f)
            }
            "kelp" -> {
                Mascot(R.drawable.cast_kelp, clock, Alignment.BottomCenter, 0.96f, sway = 4f)
            }
            "mesa" -> {
                Mascot(R.drawable.cast_mesa, clock, Alignment.BottomCenter, 0.96f, sway = 3f)
            }
            "reef" -> {
                Mascot(R.drawable.cast_reef, clock, Alignment.BottomCenter, 0.9f, sway = 2.5f)
            }
            else -> {
                Mascot(R.drawable.cast_beacon, clock, Alignment.BottomCenter, 0.96f, sway = 3.5f)
            }
        }
    }
}

@Composable
private fun BoxScope.Mascot(
    art: Int,
    clock: Float,
    alignment: Alignment,
    heightFraction: Float,
    sway: Float = 3.2f,
) {
    val bob = sin(clock * PI.toFloat() * 2f)
    Image(
        painter = painterResource(art),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .align(alignment)
            .fillMaxHeight(heightFraction)
            .graphicsLayer {
                translationY = bob * 10f
                rotationZ = bob * sway
                scaleY = 1f + bob * 0.025f
                scaleX = 1f - bob * 0.018f
            },
    )
}

@Composable
private fun FlyingGull(flight: Float, phase: Float, gullHeight: Dp) {
    val travel = (flight + phase) % 1f
    val bob = sin(travel * PI.toFloat() * 2f)
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val margin = gullHeight * 1.7f
        val x = (maxWidth + margin) * travel - margin
        Image(
            painter = painterResource(R.drawable.cast_gull),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = x, y = (4f + bob * 8f).dp)
                .height(gullHeight)
                .graphicsLayer { rotationZ = bob * 6f },
        )
    }
}

const val PRIZE_WHEEL_SPIN_MS = 2400
const val PRIZE_WHEEL_HOLD_MS = 1800

@Composable
fun PrizeWheelBonus(
    wedgeIndex: Int,
    wedgeName: String,
    prizeAmount: String,
    modifier: Modifier = Modifier,
) {
    val count = WHEEL_WEDGES.size.coerceAtLeast(1)
    val sweep = 360f / count
    val rotation = remember(wedgeIndex) { Animatable(0f) }
    var landed by remember(wedgeIndex) { mutableStateOf(false) }
    LaunchedEffect(wedgeIndex) {
        landed = false
        rotation.snapTo(0f)
        // Four turns, then the chosen wedge's center sits under the top pointer.
        rotation.animateTo(
            4 * 360f - wedgeIndex * sweep - sweep / 2f,
            tween(PRIZE_WHEEL_SPIN_MS, easing = FastOutSlowInEasing),
        )
        landed = true
    }
    val colors = listOf(
        Color(0xFF4DB7FF),
        Color(0xFF1F6F8A),
        Color(0xFFE4C56A),
        Color(0xFF8A5A12),
        Color(0xFFFF8A6A),
        Color(0xFF2EC4B6),
    )
    Box(
        modifier.background(Color(0xF0061018)),
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
            val wheel = minOf(maxWidth, maxHeight * 0.72f)
            val labelSize = (wheel.value * 0.048f).coerceIn(11f, 16f).sp
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(wheel)) {
                    Canvas(Modifier.fillMaxSize()) {
                        val radius = size.minDimension * 0.40f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val disc = Size(radius * 2, radius * 2)
                        val topLeft = Offset(center.x - radius, center.y - radius)
                        val spin = rotation.value
                        repeat(count) { index ->
                            drawArc(
                                color = colors[index % colors.size],
                                startAngle = -90f + index * sweep + spin,
                                sweepAngle = sweep - 1.2f,
                                useCenter = true,
                                topLeft = topLeft,
                                size = disc,
                            )
                        }
                        drawCircle(color = Gold, radius = radius, center = center, style = Stroke(5.dp.toPx()))
                        val bulbOrbit = radius + 12.dp.toPx()
                        repeat(18) { index ->
                            val angle = (-PI.toFloat() / 2f) + index * (PI.toFloat() * 2f / 18f)
                            drawCircle(
                                color = if (index % 2 == 0) Gold else Color(0xFFFFF6C8),
                                radius = 3.4.dp.toPx(),
                                center = Offset(center.x + cos(angle) * bulbOrbit, center.y + sin(angle) * bulbOrbit),
                            )
                        }
                        val labelPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = labelSize.toPx()
                            isFakeBoldText = true
                            isAntiAlias = true
                            setShadowLayer(4f, 0f, 1f, android.graphics.Color.BLACK)
                        }
                        val labelOffset = (labelPaint.ascent() + labelPaint.descent()) / 2f
                        repeat(count) { index ->
                            val name = WHEEL_WEDGES[index].first.name ?: ""
                            val mid = (-90f + index * sweep + sweep / 2f + spin) * (PI.toFloat() / 180f)
                            val at = radius * 0.64f
                            drawContext.canvas.nativeCanvas.drawText(
                                name,
                                center.x + cos(mid) * at,
                                center.y + sin(mid) * at - labelOffset,
                                labelPaint,
                            )
                        }
                        drawCircle(Color(0xFF1A1208), radius * 0.22f, center)
                        drawCircle(Gold, radius * 0.16f, center, style = Stroke(3.dp.toPx()))
                        if (landed) {
                            drawArc(
                                color = Color.White,
                                startAngle = -90f - sweep / 2f,
                                sweepAngle = sweep - 1.2f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = disc,
                                style = Stroke(4.dp.toPx()),
                            )
                        }
                        val pointer = Path().apply {
                            moveTo(center.x, center.y - radius + 18.dp.toPx())
                            lineTo(center.x - 16.dp.toPx(), center.y - radius - 14.dp.toPx())
                            lineTo(center.x + 16.dp.toPx(), center.y - radius - 14.dp.toPx())
                            close()
                        }
                        drawPath(pointer, Gold)
                        drawPath(pointer, Color(0xFF1A1208), style = Stroke(1.5.dp.toPx()))
                    }
                }
                Text(
                    if (landed) wedgeName else "Spinning",
                    color = Gold,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 18.dp),
                )
                Text(
                    if (landed) prizeAmount else " ",
                    color = Foam,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun FeatureRibbon(label: String, modifier: Modifier = Modifier) {
    val pulse by rememberInfiniteTransition(label = "feature").animateFloat(
        initialValue = 0.94f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(380), RepeatMode.Reverse),
        label = "pulse",
    )
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Text(
            label,
            color = Navy,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            letterSpacing = 1.sp,
            modifier = Modifier
                .padding(top = 8.dp)
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                }
                .background(Teal, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
}
