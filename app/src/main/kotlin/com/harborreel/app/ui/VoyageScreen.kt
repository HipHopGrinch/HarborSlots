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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harborreel.engine.CruiseLine
import com.harborreel.engine.CruiseLines
import com.harborreel.engine.PlayerState

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
                    "Select a cruise theme to track your simulated point milestones. For entertainment purposes only; no real-world value or affiliation.",
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
            CruiseLineName(
                lineId = line.id,
                capHeight = 14.dp,
                emojiBox = 24.dp,
                centered = false,
            )
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
