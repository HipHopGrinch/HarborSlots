package com.harborreel.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harborreel.engine.CruiseLine
import com.harborreel.engine.CruiseLines
import com.harborreel.engine.PlayerState

private val CarnivalRed = Color(0xFFEE2E24)
private val VirginRed = Color(0xFFE10A17)
private val WordmarkIvory = Color(0xFFF6F1E4)

private val WordmarkStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

@Composable
fun VoyageScreen(
    snapshot: PlayerState,
    onSelect: (String) -> Unit,
    onReset: () -> Unit,
    onResetLine: (String) -> Unit,
) {
    var confirm by rememberSaveable { mutableStateOf(false) }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Reset voyage tallies?") },
            text = { Text("Cash, the skiff meter, and the penny jar stay. Every cruise tally goes back to zero.") },
            confirmButton = {
                TextButton(onClick = {
                    confirm = false
                    onReset()
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) { Text("Cancel") }
            },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            Column(Modifier.padding(bottom = 6.dp)) {
                Text("Voyage", color = Gold, fontSize = 30.sp, fontWeight = FontWeight.Black)
                Text(
                    "Pick the line that earns points. These tallies stay on this phone.",
                    color = Foam.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        items(CruiseLines.all, key = { it.id }) { line ->
            LineRow(
                line = line,
                selected = line.id == snapshot.lineId,
                points = snapshot.points[line.id] ?: 0L,
                toward = snapshot.pointRemainder[line.id] ?: 0L,
                onSelect = { onSelect(line.id) },
                onReset = { onResetLine(line.id) },
            )
        }
        item {
            TextButton(onClick = { confirm = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Reset all tallies")
            }
        }
    }
}

@Composable
private fun LineRow(
    line: CruiseLine,
    selected: Boolean,
    points: Long,
    toward: Long,
    onSelect: () -> Unit,
    onReset: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) NavyCard else NavyRaised)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) Gold else Gold.copy(alpha = 0.28f), shape)
            .clickable(onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            LineWordmark(line.id)
            Row(
                modifier = Modifier.padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    lineMeta(line, points, toward),
                    color = Foam.copy(alpha = 0.62f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (selected) {
                    Text(
                        "SAILING",
                        color = Gold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
        Text(
            "Reset",
            color = Foam.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier
                .padding(start = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onReset)
                .padding(horizontal = 8.dp, vertical = 8.dp),
        )
    }
}

private fun lineMeta(line: CruiseLine, points: Long, toward: Long): String {
    val dollars = line.centsPerPoint / 100
    val cents = line.centsPerPoint % 100
    val money = if (cents == 0) "$$dollars" else "$$dollars.${cents.toString().padStart(2, '0')}"
    val rate = "1 pt / $money"
    return if (toward > 0L) {
        "${points.grouped()} pts  ·  $rate  ·  ${toward.money()} to next"
    } else {
        "${points.grouped()} pts  ·  $rate"
    }
}

@Composable
private fun LineWordmark(id: String) {
    when (id) {
        "carnival" -> Text(
            "CARNIVAL",
            color = CarnivalRed,
            fontFamily = MartelHeavy,
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            letterSpacing = 0.6.sp,
            maxLines = 1,
            softWrap = false,
            style = WordmarkStyle,
            modifier = Modifier.graphicsLayer {
                scaleX = 1.1f
                transformOrigin = TransformOrigin(0f, 0.5f)
            },
        )
        "royal" -> Text(
            "Royal Caribbean",
            color = WordmarkIvory,
            fontFamily = EbGaramond,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            maxLines = 1,
            softWrap = false,
            style = WordmarkStyle,
        )
        "norwegian" -> Text(
            "NORWEGIAN",
            color = Color.White,
            fontFamily = JostBoldItalic,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            fontSize = 22.sp,
            letterSpacing = (-0.4).sp,
            maxLines = 1,
            softWrap = false,
            style = WordmarkStyle,
        )
        "princess" -> Text(
            "PRINCESS",
            color = Color.White,
            fontFamily = InterBold,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            letterSpacing = (-0.6).sp,
            maxLines = 1,
            softWrap = false,
            style = WordmarkStyle,
            modifier = Modifier.graphicsLayer {
                scaleX = 0.94f
                transformOrigin = TransformOrigin(0f, 0.5f)
            },
        )
        "celebrity" -> Text(
            "CELEBRITY",
            color = Color.White,
            fontFamily = RobotoRegular,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            letterSpacing = (-1.05).sp,
            maxLines = 1,
            softWrap = false,
            style = WordmarkStyle,
        )
        "holland" -> Text(
            "HOLLAND AMERICA",
            color = WordmarkIvory,
            fontFamily = PlayfairBold,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            letterSpacing = 3.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            style = WordmarkStyle,
        )
        "msc" -> Text(
            "MSC",
            color = Color.White,
            fontFamily = DmSansSemiBold,
            fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp,
            letterSpacing = 0.15.sp,
            maxLines = 1,
            softWrap = false,
            style = WordmarkStyle,
        )
        else -> Column {
            Text(
                "Virgin",
                color = VirginRed,
                fontFamily = CaveatSemiBold,
                fontWeight = FontWeight.SemiBold,
                fontSize = 34.sp,
                maxLines = 1,
                softWrap = false,
                style = WordmarkStyle,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .graphicsLayer {
                        rotationZ = -15f
                        transformOrigin = TransformOrigin(0f, 1f)
                    },
            )
            Text(
                "VOYAGES",
                color = Color.White,
                fontFamily = Monoton,
                fontSize = 13.sp,
                letterSpacing = 1.4.sp,
                maxLines = 1,
                softWrap = false,
                style = WordmarkStyle,
                modifier = Modifier
                    .offset(y = (-4).dp)
                    .padding(start = 8.dp),
            )
        }
    }
}
