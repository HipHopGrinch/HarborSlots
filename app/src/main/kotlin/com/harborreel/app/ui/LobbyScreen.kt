package com.harborreel.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harborreel.app.R
import com.harborreel.engine.Catalog
import com.harborreel.engine.CruiseLines
import com.harborreel.engine.GameDef
import com.harborreel.engine.PlayerState

@Composable
fun LobbyScreen(snapshot: PlayerState, onOpen: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.logo_slots),
            contentDescription = "Harbor Slots",
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .padding(horizontal = 8.dp),
            contentScale = ContentScale.Fit,
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(Catalog.games, key = { it.id }) { game ->
                GamePoster(game, Modifier, onOpen)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BalancePlaque("CASH", snapshot.credits.money(), Modifier.weight(1f))
            BalancePlaque(
                CruiseLines.nameOf(snapshot.lineId).uppercase(),
                "${snapshot.points[snapshot.lineId]?.grouped() ?: "0"} PTS",
                Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BalancePlaque(label: String, value: String, modifier: Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF6D0), Color(0xFFE4C56A), Color(0xFF8A5A12), Color(0xFFFFE29A)),
                ),
            )
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF3A2412), Color(0xFF100806))))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            label,
            color = Gold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            value,
            color = Foam,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GamePoster(game: GameDef, modifier: Modifier, onOpen: (String) -> Unit) {
    val art = posterArt(game.id)
    val shape = RoundedCornerShape(18.dp)
    var info by remember(game.id) { mutableStateOf(false) }
    if (info) {
        SimilarSlotsDialog(game) { info = false }
    }
    Box(
        modifier
            .aspectRatio(0.78f)
            .clip(shape)
            .border(2.dp, Gold, shape)
            .clickable { onOpen(game.id) },
    ) {
        Image(
            painter = painterResource(art.scene),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Image(
            painter = painterResource(art.mascot),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxHeight(0.78f)
                .padding(bottom = 28.dp),
            contentScale = ContentScale.Fit,
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE6100804))))
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(gameLogo(game.id)),
                    contentDescription = game.name,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    contentScale = ContentScale.Fit,
                )
                Text(
                    game.featureLabel,
                    color = Foam,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        GameInfoButton(
            game,
            onClick = { info = true },
            modifier = Modifier.align(Alignment.TopEnd).padding(2.dp),
        )
    }
}

private data class PosterArt(val scene: Int, val mascot: Int)

fun gameLogo(gameId: String): Int = when (gameId) {
    "harbor" -> R.drawable.logo_harbor
    "brightwork" -> R.drawable.logo_bright
    "market" -> R.drawable.logo_market
    "patch" -> R.drawable.logo_patch
    "kelp" -> R.drawable.logo_kelp
    "mesa" -> R.drawable.logo_mesa
    "reef" -> R.drawable.logo_reef
    else -> R.drawable.logo_beacon
}

private fun posterArt(gameId: String): PosterArt = when (gameId) {
    "harbor" -> PosterArt(R.drawable.bg_harbor, R.drawable.cast_harbor)
    "brightwork" -> PosterArt(R.drawable.bg_brightwork, R.drawable.cast_bright)
    "market" -> PosterArt(R.drawable.bg_market, R.drawable.cast_market)
    "patch" -> PosterArt(R.drawable.bg_patch, R.drawable.cast_patch)
    "kelp" -> PosterArt(R.drawable.bg_kelp, R.drawable.cast_kelp)
    "mesa" -> PosterArt(R.drawable.bg_mesa, R.drawable.cast_mesa)
    "reef" -> PosterArt(R.drawable.bg_reef, R.drawable.cast_reef)
    else -> PosterArt(R.drawable.bg_beacon, R.drawable.cast_beacon)
}
