package com.harborreel.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.harborreel.engine.Symbol

val Navy = Color(0xFF0B1D36)
val NavyRaised = Color(0xFF132A4A)
val NavyCard = Color(0xFF173258)
val Gold = Color(0xFFE4C56A)
val Foam = Color(0xFFE8EEF6)
val Teal = Color(0xFF2EC4B6)
val Coral = Color(0xFFFF8A6A)

private val scheme = darkColorScheme(
    primary = Gold,
    onPrimary = Navy,
    secondary = Teal,
    onSecondary = Navy,
    background = Navy,
    onBackground = Foam,
    surface = NavyRaised,
    onSurface = Foam,
    surfaceVariant = NavyCard,
    onSurfaceVariant = Color(0xFFC5D2E4),
)

@Composable
fun HarborTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}

fun symbolColor(symbol: Symbol): Color = when (symbol) {
    Symbol.BLANK -> Color(0xFF0E2240)
    Symbol.ANCHOR, Symbol.SQUALL, Symbol.CHEF, Symbol.SCARECROW -> Gold
    Symbol.BUOY, Symbol.COIN -> Teal
    Symbol.CLOUD, Symbol.STALL, Symbol.BASKET -> Coral
    Symbol.COMPASS, Symbol.KEEL, Symbol.NOODLE, Symbol.APPLE -> Color(0xFF7EB6FF)
    Symbol.WHEEL, Symbol.PLANK, Symbol.DUMPLING, Symbol.CLOVER -> Color(0xFFB8A1FF)
    Symbol.ROPE, Symbol.CANVAS, Symbol.TEA, Symbol.BOOT -> Color(0xFF8FD6A4)
    Symbol.BELL, Symbol.GULL, Symbol.FAN, Symbol.LANTERN, Symbol.LAMP -> Color(0xFFF0D48A)
    else -> Color(0xFFD5DEEA)
}
