package com.harborreel.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Build
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import com.harborreel.app.R
import com.harborreel.engine.BUOY_PRIZES
import com.harborreel.engine.Catalog
import com.harborreel.engine.Cell
import com.harborreel.engine.FLEET_BONUS_COINS
import com.harborreel.engine.FreeSpins
import com.harborreel.engine.GameDef
import com.harborreel.engine.GameKind
import com.harborreel.engine.LockRespin
import com.harborreel.engine.SpinOutcome
import com.harborreel.engine.Symbol
import com.harborreel.engine.TUMBLE_CAP
import com.harborreel.engine.TUMBLE_MULT_CAP
import com.harborreel.engine.Tumble
import com.harborreel.engine.WHEEL_WEDGES
import com.harborreel.engine.WheelSpin
import com.harborreel.engine.openingGrid
import com.harborreel.engine.skiffStage
import kotlin.random.Random
import kotlinx.coroutines.delay


@Composable
fun GameScreen(gameId: String, model: CasinoViewModel, onBack: () -> Unit) {
    val game = Catalog.games.firstOrNull { it.id == gameId }
    if (game == null) {
        Column(Modifier.padding(20.dp)) {
            Text("That game is not on this floor.")
            TextButton(onClick = onBack) { Text("Back") }
        }
        return
    }

    val snapshot = model.snapshot
    val outcome = model.last?.takeIf { it.gameId == game.id }
    val alreadyShown = outcome?.takeIf { model.isPresented(it.spinId) }
    var grid by remember(game.id) { mutableStateOf(alreadyShown?.settledGrid() ?: openingGrid(game)) }
    var highlights by remember(game.id) { mutableStateOf(alreadyShown?.settledHighlights() ?: emptySet()) }
    var banner by remember(game.id) { mutableStateOf(alreadyShown?.settledBanner() ?: "Press SPIN") }
    var busy by remember(game.id) { mutableStateOf(false) }
    var rules by remember(game.id) { mutableStateOf(false) }
    var shownWin by remember(game.id) { mutableLongStateOf(alreadyShown?.totalWin ?: 0L) }
    var strips by remember(game.id) { mutableStateOf(alreadyShown?.settledGrid() ?: openingGrid(game)) }
    var positions by remember(game.id) { mutableStateOf(List(5) { 0f }) }
    var blurAmounts by remember(game.id) { mutableStateOf(List(5) { 0f }) }
    var celebrating by remember(game.id) { mutableStateOf(false) }
    var callout by remember(game.id) { mutableStateOf<String?>(null) }
    var wheelIndex by remember(game.id) { mutableStateOf<Int?>(null) }
    var held by remember(game.id) { mutableStateOf(noHolds()) }
    val symbols = remember(game.id) { game.reels.flatten().distinct() }

    if (rules) {
        AlertDialog(
            onDismissRequest = { rules = false },
            title = { Text(game.name) },
            text = { Text(game.rules) },
            confirmButton = { TextButton(onClick = { rules = false }) { Text("Close") } },
        )
    }

    LaunchedEffect(outcome?.spinId) {
        val spin = outcome ?: return@LaunchedEffect
        if (model.isPresented(spin.spinId)) return@LaunchedEffect
        busy = true
        celebrating = false
        callout = null
        wheelIndex = null
        shownWin = 0L
        highlights = emptySet()
        banner = "Spinning…"
        scrollReels(symbols, spin.grid, travel = 34, spinMs = 1650, staggerMs = 240) { nextStrips, nextPos, nextBlur ->
            strips = nextStrips
            positions = nextPos
            blurAmounts = nextBlur
        }
        grid = spin.grid
        strips = spin.grid
        positions = List(5) { 0f }
        blurAmounts = List(5) { 0f }
        highlights = spin.lineWins.flatMap { it.cells }.toSet()
        banner = baseBanner(spin.lineWins.size, spin.scatterPay, spin.totalWin, spin.feature == null, winNoun(game.kind))
        when (val feature = spin.feature) {
            is LockRespin -> {
                callout = "LOCK & RESPIN"
                banner = "Lock & Respin"
                var lockedBoard = spin.grid
                held = buoyHolds(lockedBoard)
                for (frame in feature.frames) {
                    highlights = emptySet()
                    banner = "Lock & Respin · ${frame.livesLeft} respin${if (frame.livesLeft == 1) "" else "s"} left"
                    scrollReels(symbols, frame.grid, travel = 12, spinMs = 620, staggerMs = 80) { nextStrips, nextPos, nextBlur ->
                        strips = nextStrips
                        positions = nextPos
                        blurAmounts = nextBlur
                    }
                    lockedBoard = frame.grid
                    held = buoyHolds(lockedBoard)
                    grid = frame.grid
                    strips = frame.grid
                    positions = List(5) { 0f }
                    blurAmounts = List(5) { 0f }
                }
                held = noHolds()
                banner = "${feature.title}. ${feature.detail}"
            }
            is FreeSpins -> {
                callout = "FREE SPINS"
                for (frame in feature.frames) {
                    highlights = emptySet()
                    val extra = if (frame.extraSpins > 0) "  +${frame.extraSpins} spins" else ""
                    banner = "Free spin · ${frame.spinsLeft} left · reel ${frame.wildReel + 1} wild$extra"
                    scrollReels(symbols, frame.grid, travel = 18, spinMs = 980, staggerMs = 150) { nextStrips, nextPos, nextBlur ->
                        strips = nextStrips
                        positions = nextPos
                        blurAmounts = nextBlur
                    }
                    grid = frame.grid
                    strips = frame.grid
                    positions = List(5) { 0f }
                    blurAmounts = List(5) { 0f }
                    highlights = frame.wins.flatMap { it.cells }.toSet()
                    if (frame.win > 0L) delay(280)
                }
                banner = "${feature.title}. ${feature.detail}"
            }
            is Tumble -> {
                callout = "TUMBLE"
                for (frame in feature.frames) {
                    highlights = emptySet()
                    banner = "Drop ×${frame.multiplier}"
                    grid = frame.grid
                    strips = frame.grid
                    positions = List(5) { 0f }
                    delay(320)
                    highlights = frame.wins.flatMap { it.cells }.toSet()
                    delay(420)
                }
                grid = feature.settled
                strips = feature.settled
                positions = List(5) { 0f }
                highlights = emptySet()
                banner = "${feature.title}. ${feature.detail}"
            }
            is WheelSpin -> {
                callout = "PRIZE WHEEL"
                wheelIndex = feature.wedgeIndex
                banner = "${feature.title}. ${feature.detail}"
                delay(1700)
            }
            null -> Unit
            else -> {
                callout = feature.title.uppercase()
                banner = "${feature.title}. ${feature.detail}"
            }
        }
        shownWin = spin.totalWin
        celebrating = spin.totalWin > 0L
        if (spin.totalWin > 0L) {
            delay(1700)
            celebrating = false
            callout = null
        } else {
            callout = null
        }
        model.markPresented(spin.spinId)
        model.revealBankroll()
        busy = false
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(sceneFor(game.id)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack, enabled = !busy) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                Text("Games")
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { rules = true }, enabled = !busy) { Text("Rules") }
        }

        Image(
            painter = painterResource(gameLogo(game.id)),
            contentDescription = game.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(61.dp)
                .padding(horizontal = 16.dp, vertical = 2.dp),
            contentScale = ContentScale.Fit,
        )
        Text(
            game.featureLabel.uppercase(),
            color = Foam,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().background(Color(0x44000000)),
        )

        CabinetMeters(
            game = game,
            bet = snapshot.bet,
            skiff = snapshot.skiff,
            jar = snapshot.jar,
        )

        Box(Modifier.weight(1f).fillMaxWidth()) {
            SceneLife(game.id, Modifier.fillMaxSize())
            wheelIndex?.let { index ->
                PrizeWheel(index, Modifier.fillMaxSize().padding(12.dp))
            }
        }

        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            val gap = 5.dp
            val framePad = 8.dp
            val tileW = (maxWidth - framePad * 2 - gap * 4) / 5
            val tileHCap = (maxHeight - framePad * 2 - gap * 2) / 3
            val tileH = if (tileHCap < tileW * 1.15f) tileHCap else tileW * 1.15f
            Box(contentAlignment = Alignment.Center) {
                ReelFrame(strips, positions, blurAmounts, held, highlights, tileW, tileH, snapshot.bet / 20L)
                if (celebrating) {
                    WinCelebration(shownWin)
                }
                callout?.let { label ->
                    FeatureRibbon(label)
                }
            }
        }

        Text(
            model.notice ?: banner,
            color = if (model.notice != null) Coral else Foam,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
                .background(Color(0xAA000000), RoundedCornerShape(8.dp))
                .padding(vertical = 4.dp),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold,
        )

        Button(
            onClick = { model.spin(game.id) },
            enabled = !busy && model.bankroll >= snapshot.bet,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
        ) {
            Text(if (busy) "IN PLAY" else "SPIN", fontSize = 18.sp, fontWeight = FontWeight.Black)
        }

        Deck(
            cash = model.bankroll,
            denom = snapshot.denom,
            betPerLine = snapshot.betPerLine,
            wager = snapshot.bet.toLong(),
            win = shownWin,
            busy = busy,
            onDenom = model::nudgeDenom,
            onBet = model::nudgeBet,
        )
    }
    }
}

private fun sceneFor(gameId: String): Int = when (gameId) {
    "harbor" -> R.drawable.bg_harbor
    "brightwork" -> R.drawable.bg_brightwork
    "market" -> R.drawable.bg_market
    "patch" -> R.drawable.bg_patch
    "kelp" -> R.drawable.bg_kelp
    "mesa" -> R.drawable.bg_mesa
    "reef" -> R.drawable.bg_reef
    else -> R.drawable.bg_beacon
}

@Composable
private fun CabinetMeters(game: GameDef, bet: Int, skiff: Int, jar: Long) {
    val coin = bet / 20
    when (game.kind) {
        GameKind.LOCK_RESPIN -> {
            val fleet = FLEET_BONUS_COINS.toLong() * coin
            Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                OrnateMeter("FLEET", fleet.money(), Color(0xFFFFD35A), featured = true)
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OrnateMeter("HARBOR", (prizeCoins("Harbor").toLong() * coin).money(), Color(0xFFE24BFF), Modifier.weight(1f))
                    OrnateMeter("BAY", (prizeCoins("Bay").toLong() * coin).money(), Color(0xFF3DDC6A), Modifier.weight(1f))
                    OrnateMeter("COVE", (prizeCoins("Cove").toLong() * coin).money(), Color(0xFF4DB7FF), Modifier.weight(1f))
                }
            }
        }
        GameKind.GALE -> {
            val stage = skiffStage(skiff)
            Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                OrnateMeter(stage.name.uppercase(), "×${stage.multiplier}", Color(0xFFFFD35A), featured = true)
                Text(
                    "Skiff $skiff${stage.nextAt?.let { "  ·  next at $it" } ?: ""}",
                    color = Color(0xFFFFE7A3),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(Color(0x88000000), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
        GameKind.FREE_SPINS -> {
            Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                OrnateMeter("NIGHT SERVICE", "FREE SPINS", Color(0xFFFF5A3C), featured = true)
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OrnateMeter("3 STALLS", "8", Color(0xFF3DDC6A), Modifier.weight(1f))
                    OrnateMeter("4 STALLS", "12", Color(0xFFE24BFF), Modifier.weight(1f))
                    OrnateMeter("5 STALLS", "20", Color(0xFFFFD35A), Modifier.weight(1f))
                }
            }
        }
        GameKind.JAR -> {
            Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                OrnateMeter("JAR", jar.money(), Color(0xFFFFD35A), featured = true)
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("×1" to Color(0xFF4DB7FF), "×2" to Color(0xFF3DDC6A), "×3" to Color(0xFFE24BFF), "×5" to Color(0xFFFFD35A)).forEach { (label, glow) ->
                        OrnateMeter("TIP", label, glow, Modifier.weight(1f))
                    }
                }
            }
        }
        GameKind.WAYS -> {
            Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                OrnateMeter("243 WAYS", "ANY ROW", Color(0xFFFFD35A), featured = true)
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OrnateMeter("3 REELS", "PAYS", Color(0xFF4DB7FF), Modifier.weight(1f))
                    OrnateMeter("4 REELS", "PAYS", Color(0xFF3DDC6A), Modifier.weight(1f))
                    OrnateMeter("5 REELS", "PAYS", Color(0xFFE24BFF), Modifier.weight(1f))
                }
            }
        }
        GameKind.TUMBLE -> {
            Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                OrnateMeter("DROPS", "×1 TO ×$TUMBLE_MULT_CAP", Color(0xFF3DDC6A), featured = true)
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OrnateMeter("LEFT", "TO RIGHT", Color(0xFF4DB7FF), Modifier.weight(1f))
                    OrnateMeter("WILD", "OTTER", Color(0xFFFFD35A), Modifier.weight(1f))
                    OrnateMeter("CAP", "$TUMBLE_CAP DROPS", Color(0xFFE24BFF), Modifier.weight(1f))
                }
            }
        }
        GameKind.CLUSTER -> {
            Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                OrnateMeter("CLUSTERS", "3 OR MORE", Color(0xFFFF8A6A), featured = true)
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OrnateMeter("TOUCH", "EDGES", Color(0xFF4DB7FF), Modifier.weight(1f))
                    OrnateMeter("WILD", "PUFFER", Color(0xFFFFD35A), Modifier.weight(1f))
                    OrnateMeter("BIGGER", "PAYS MORE", Color(0xFF3DDC6A), Modifier.weight(1f))
                }
            }
        }
        GameKind.WHEEL -> {
            val stake = coin.toLong()
            Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                OrnateMeter("WHEEL", "3 BEACONS", Color(0xFFFFD35A), featured = true)
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OrnateMeter("RIPPLE", (wedgeCoins("Ripple") * stake).money(), Color(0xFF4DB7FF), Modifier.weight(1f))
                    OrnateMeter("BEAM", (wedgeCoins("Beam") * stake).money(), Color(0xFFE24BFF), Modifier.weight(1f))
                    OrnateMeter("LIGHT", (wedgeCoins("Lighthouse") * stake).money(), Color(0xFFFFD35A), Modifier.weight(1f))
                }
            }
        }
    }
}

private fun prizeCoins(name: String): Int =
    BUOY_PRIZES.first { it.first.name == name }.first.coins

private fun wedgeCoins(name: String): Long =
    WHEEL_WEDGES.first { it.first.name == name }.first.coins.toLong()

@Composable
private fun OrnateMeter(
    label: String,
    value: String,
    glow: Color,
    modifier: Modifier = Modifier,
    featured: Boolean = false,
) {
    val pulse by rememberInfiniteTransition(label = label).animateFloat(
        initialValue = 0.62f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(if (featured) 850 else 1300), RepeatMode.Reverse),
        label = "meter",
    )
    val outer = RoundedCornerShape(if (featured) 16.dp else 11.dp)
    val inner = RoundedCornerShape(if (featured) 13.dp else 9.dp)
    Box(
        modifier
            .shadow(12.dp, outer, ambientColor = glow.copy(alpha = 0.7f), spotColor = glow)
            .clip(outer)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFF8DC),
                        Color(0xFFF0D078),
                        Color(0xFF8C5A16),
                        Color(0xFFFFE29A),
                        Color(0xFF4A2C08),
                    ),
                ),
            )
            .padding(if (featured) 3.dp else 2.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(inner)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            glow.copy(alpha = 0.42f * pulse),
                            Color(0xFF1A0C28),
                            Color(0xFF07040C),
                        ),
                    ),
                )
                .border(1.dp, Color.White.copy(alpha = 0.28f), inner)
                .padding(horizontal = 4.dp, vertical = if (featured) 5.dp else 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                label,
                color = Color(0xFFFFE7A3),
                fontSize = if (featured) 12.sp else 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                value,
                color = Color.White,
                fontSize = if (featured) 26.sp else 14.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(shadow = Shadow(glow, Offset(0f, 0f), 14f * pulse)),
            )
        }
    }
}

@Composable
private fun ReelFrame(
    strips: List<List<Cell>>,
    positions: List<Float>,
    blurAmounts: List<Float>,
    held: List<List<Cell?>>,
    highlights: Set<Pair<Int, Int>>,
    tileW: Dp,
    tileH: Dp,
    coinCents: Long,
) {
    Box {
        Row(
            Modifier
                .shadow(16.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF3A2412), Color(0xFF120C08), Color(0xFF3A2412))),
                )
                .border(3.dp, Brush.verticalGradient(listOf(Color(0xFFFFF1C2), Gold, Color(0xFF8A5A16))), RoundedCornerShape(18.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            strips.forEachIndexed { reel, strip ->
                val pos = positions.getOrElse(reel) { 0f }
                val blur = blurAmounts.getOrElse(reel) { 0f }
                val maxStart = (strip.size - 3).coerceAtLeast(0)
                val clamped = pos.coerceIn(0f, maxStart.toFloat())
                val index = clamped.toInt().coerceIn(0, maxStart)
                val fraction = (clamped - index).coerceIn(0f, 0.999f)
                val locks = held.getOrElse(reel) { emptyList() }
                Box(
                    Modifier
                        .width(tileW)
                        .height(tileH * 3)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF071422)),
                ) {
                    Layout(
                        content = {
                            for (slot in 0..3) {
                                val cellIndex = index + slot
                                val cell = if (cellIndex in strip.indices) strip[cellIndex] else Cell(Symbol.BLANK)
                                val hot = blur < 0.08f &&
                                    fraction < 0.04f &&
                                    index == 0 &&
                                    slot in 0..2 &&
                                    (reel to slot) in highlights &&
                                    locks.getOrNull(slot) == null
                                CellTile(cell, hot, tileW, tileH, coinCents, blur > 0.45f)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val radius = blur * 18f
                                renderEffect = if (radius > 1.4f && Build.VERSION.SDK_INT >= 31) {
                                    BlurEffect(radius * 0.18f, radius, TileMode.Decal)
                                } else {
                                    null
                                }
                            },
                    ) { measurables, constraints ->
                        val tilePx = constraints.maxHeight / 3
                        val placeables = measurables.map {
                            it.measure(androidx.compose.ui.unit.Constraints.fixed(constraints.maxWidth, tilePx))
                        }
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeables.forEachIndexed { slot, placeable ->
                                // Fraction grows as the window moves up the strip, so symbols travel downward.
                                placeable.place(0, ((slot - fraction) * tilePx).toInt())
                            }
                        }
                    }
                    locks.forEachIndexed { row, cell ->
                        if (cell != null) {
                            val hot = blur < 0.08f && (reel to row) in highlights
                            Box(Modifier.offset(y = tileH * row)) {
                                CellTile(cell, hot, tileW, tileH, coinCents, blur = false)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CellTile(
    cell: Cell,
    hot: Boolean,
    tileW: Dp,
    tileH: Dp,
    coinCents: Long,
    blur: Boolean,
) {
    val shape = RoundedCornerShape(8.dp)
    val prize = if (cell.prizeCoins > 0) (cell.prizeCoins * coinCents).money() else cell.prizeName
    val glow by rememberInfiniteTransition(label = "cell").animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(280), RepeatMode.Reverse),
        label = "glow",
    )
    Column(
        Modifier
            .width(tileW)
            .height(tileH)
            .graphicsLayer {
                val pop = if (hot) 1.06f else 1f
                scaleX = pop
                scaleY = pop
            }
            .clip(shape)
            .background(
                if (hot) Color(0xFF3A2A12) else if (cell.symbol == Symbol.BLANK) Color(0xFF0A1830) else Color(0xFF163056),
            )
            .border(
                if (hot) 3.dp else 1.dp,
                if (hot) Gold.copy(alpha = glow) else symbolColor(cell.symbol).copy(alpha = 0.9f),
                shape,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        SymbolGlyph(
            cell.symbol,
            Modifier
                .height(tileH * if (prize == null) 0.72f else 0.55f)
                .fillMaxWidth(),
        )
        if (prize != null) {
            Text(
                prize,
                color = if (blur) Gold.copy(alpha = 0.35f) else Gold,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Deck(
    cash: Long,
    denom: Int,
    betPerLine: Int,
    wager: Long,
    win: Long,
    busy: Boolean,
    onDenom: (Int) -> Unit,
    onBet: (Int) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF050C16))
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DeckStat("CASH", cash.money(), Modifier.weight(1f))
            Stepper("DENOM", denomLabel(denom), busy, Modifier.weight(1.15f), onDenom)
            Stepper("BET", "$betPerLine×20", busy, Modifier.weight(1.15f), onBet)
            DeckStat("WIN", win.money(), Modifier.weight(1f))
        }
        Text(
            "Spin ${wager.money()}",
            color = Foam.copy(alpha = 0.7f),
            fontSize = 10.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Stepper(
    label: String,
    value: String,
    busy: Boolean,
    modifier: Modifier,
    onStep: (Int) -> Unit,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Nudge("−", !busy) { onStep(-1) }
            Text(
                value,
                color = Foam,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(46.dp),
                maxLines = 1,
            )
            Nudge("+", !busy) { onStep(1) }
        }
    }
}

@Composable
private fun Nudge(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (enabled) Gold else Gold.copy(alpha = 0.35f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Navy, fontSize = 16.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun DeckStat(label: String, value: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
        Text(value, color = Foam, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private suspend fun scrollReels(
    symbols: List<Symbol>,
    target: List<List<Cell>>,
    travel: Int,
    spinMs: Int,
    staggerMs: Int,
    publish: (List<List<Cell>>, List<Float>, List<Float>) -> Unit,
) {
    if (symbols.isEmpty()) return
    val spinNs = spinMs * 1_000_000L
    val staggerNs = staggerMs * 1_000_000L
    val extras = List(5) { reel -> if (spinMs == 0) 0 else reel * travel * staggerMs / spinMs }
    val spun = List(5) { reel ->
        // One symbol above the stop, then the result, then the symbols the reel falls through.
        listOf(Cell(symbols[Random.nextInt(symbols.size)])) +
            target[reel] +
            List(travel + extras[reel] + 2) { Cell(symbols[Random.nextInt(symbols.size)]) }
    }
    val distances = List(5) { reel -> (travel + extras[reel]).toFloat() }
    val startPositions = distances.map { 1f + it }
    val durations = List(5) { reel -> spinNs + reel * staggerNs }
    var previous = startPositions
    val start = withFrameNanos { it }
    while (true) {
        val now = withFrameNanos { it }
        val elapsed = now - start
        val next = List(5) { reel ->
            val local = elapsed.toFloat() / durations[reel].toFloat()
            startPositions[reel] - reelPosition(local, distances[reel])
        }
        val blur = List(5) { reel ->
            val delta = kotlin.math.abs(next[reel] - previous[reel])
            (delta / 0.42f).coerceIn(0f, 1f)
        }
        previous = next
        publish(spun, next, blur)
        if ((0..4).all { reel -> elapsed >= durations[reel] }) break
    }
}

/** Fast cruise, a long brake, then a short settle back onto the stop. */
private fun reelPosition(t: Float, distance: Float): Float {
    if (t <= 0f) return 0f
    if (t >= 1f) return distance
    val settleAt = 0.9f
    val along = if (t >= settleAt) {
        1f
    } else {
        val u = t / settleAt
        when {
            u < 0.14f -> {
                val a = u / 0.14f
                a * a * 0.06f
            }
            u < 0.68f -> {
                val a = (u - 0.14f) / (0.68f - 0.14f)
                0.06f + a * 0.76f
            }
            else -> {
                val a = (u - 0.68f) / (1f - 0.68f)
                val eased = 1f - (1f - a) * (1f - a) * (1f - a)
                0.82f + eased * 0.18f
            }
        }
    }
    if (t < settleAt) return along * distance
    val u = (t - settleAt) / (1f - settleAt)
    val bounce = kotlin.math.sin(u * Math.PI.toFloat()) * (1f - u) * 0.36f
    return distance + bounce
}

private fun noHolds(): List<List<Cell?>> = List(5) { List(3) { null } }

private fun buoyHolds(grid: List<List<Cell>>): List<List<Cell?>> =
    grid.map { reel -> reel.map { cell -> if (cell.symbol == Symbol.BUOY) cell else null } }

private fun SpinOutcome.settledGrid(): List<List<Cell>> = when (val feature = feature) {
    is LockRespin -> feature.frames.lastOrNull()?.grid ?: grid
    is FreeSpins -> feature.frames.lastOrNull()?.grid ?: grid
    is Tumble -> feature.settled
    else -> grid
}

private fun SpinOutcome.settledHighlights(): Set<Pair<Int, Int>> =
    if (feature == null) lineWins.flatMap { it.cells }.toSet() else emptySet()

private fun SpinOutcome.settledBanner(): String {
    val feature = feature
    return if (feature != null) {
        "${feature.title}. ${feature.detail}"
    } else {
        baseBanner(lineWins.size, scatterPay, totalWin, settled = true, winNoun(Catalog.byId(gameId).kind))
    }
}

private fun winNoun(kind: GameKind): String = when (kind) {
    GameKind.WAYS, GameKind.TUMBLE -> "way"
    GameKind.CLUSTER -> "cluster"
    else -> "line"
}

private fun baseBanner(lines: Int, scatterPay: Long, total: Long, settled: Boolean, noun: String = "line"): String {
    if (!settled) return ""
    val parts = mutableListOf<String>()
    if (lines > 0) parts += "$lines $noun${if (lines == 1) "" else "s"}"
    if (scatterPay > 0) parts += "scatter ${scatterPay.grouped()}"
    return if (total == 0L) "No win" else parts.joinToString(" · ").ifBlank { "Win" }
}
