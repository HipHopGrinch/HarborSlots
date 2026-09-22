package com.harborreel.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.harborreel.app.R
import com.harborreel.engine.Symbol

fun symbolArt(symbol: Symbol): Int? = when (symbol) {
    Symbol.BLANK -> null
    Symbol.ANCHOR -> R.drawable.sym_anchor
    Symbol.COMPASS -> R.drawable.sym_compass
    Symbol.WHEEL -> R.drawable.sym_wheel
    Symbol.ROPE -> R.drawable.sym_rope
    Symbol.BELL -> R.drawable.sym_bell
    Symbol.LANTERN -> R.drawable.sym_lantern
    Symbol.BUOY -> R.drawable.sym_buoy
    Symbol.SQUALL -> R.drawable.sym_squall
    Symbol.KEEL -> R.drawable.sym_keel
    Symbol.PLANK -> R.drawable.sym_plank
    Symbol.CANVAS -> R.drawable.sym_canvas
    Symbol.GULL -> R.drawable.sym_gull
    Symbol.LAMP -> R.drawable.sym_lamp
    Symbol.CLOUD -> R.drawable.sym_cloud
    Symbol.CHEF -> R.drawable.sym_chef
    Symbol.NOODLE -> R.drawable.sym_noodle
    Symbol.DUMPLING -> R.drawable.sym_dumpling
    Symbol.TEA -> R.drawable.sym_tea
    Symbol.FAN -> R.drawable.sym_fan
    Symbol.STALL -> R.drawable.sym_stall
    Symbol.COIN -> R.drawable.sym_coin
    Symbol.SCARECROW -> R.drawable.sym_scarecrow
    Symbol.APPLE -> R.drawable.sym_apple
    Symbol.CLOVER -> R.drawable.sym_clover
    Symbol.BOOT -> R.drawable.sym_boot
    Symbol.BASKET -> R.drawable.sym_basket
    Symbol.ACE -> R.drawable.sym_ace
    Symbol.KING -> R.drawable.sym_king
    Symbol.QUEEN -> R.drawable.sym_queen
    Symbol.JACK -> R.drawable.sym_jack
    Symbol.TEN -> R.drawable.sym_ten
    Symbol.NINE -> R.drawable.sym_nine
}

@Composable
fun SymbolGlyph(symbol: Symbol, modifier: Modifier = Modifier) {
    val art = symbolArt(symbol) ?: return
    Image(
        painter = painterResource(art),
        contentDescription = symbol.title,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
    )
}
