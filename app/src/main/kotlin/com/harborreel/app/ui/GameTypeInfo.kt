package com.harborreel.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harborreel.engine.GameDef

@Composable
fun GameInfoButton(
    game: GameDef,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = "Real slots like ${game.name}",
            tint = if (enabled) Gold else Gold.copy(alpha = 0.35f),
            modifier = Modifier
                .size(26.dp)
                .background(Color(0xB3100804), CircleShape)
                .border(1.dp, Gold.copy(alpha = 0.6f), CircleShape),
        )
    }
}

@Composable
fun SimilarSlotsDialog(game: GameDef, onDismiss: () -> Unit) {
    val type = game.type
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                SectionLabel(game.name.uppercase())
                Text("Real slots like this")
            }
        },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                type.floorExamples.forEach { example ->
                    Text(
                        "${example.name} (${example.note})",
                        color = Foam,
                        fontSize = 15.sp,
                    )
                }
                type.floorNote?.let { Text(it, fontSize = 13.sp) }
                SectionLabel("WHY", Modifier.padding(top = 6.dp))
                Text(
                    type.alsoCalled?.let { "${type.title} ($it)" } ?: type.title,
                    color = Foam,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(type.description, fontSize = 13.sp)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun SectionLabel(label: String, modifier: Modifier = Modifier) {
    Text(
        label,
        color = Gold,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = modifier,
    )
}
