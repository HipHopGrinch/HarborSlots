package com.harborreel.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harborreel.engine.Catalog
import com.harborreel.engine.PlayerState

@Composable
fun BankScreen(
    snapshot: PlayerState,
    onSetCash: (Long) -> Unit,
    onClear: () -> Unit,
    onClearHistory: () -> Unit,
    onResetPoints: () -> Unit,
) {
    var amount by rememberSaveable { mutableStateOf("200") }
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    var confirmHistory by rememberSaveable { mutableStateOf(false) }
    var confirmPoints by rememberSaveable { mutableStateOf(false) }
    val parsed = parseDollarsToCents(amount)

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear the bank?") },
            text = { Text("Cash goes to $0.00. Cruise points, the skiff, and the jar stay.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    onClear()
                }) { Text("Clear") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } },
        )
    }
    if (confirmHistory) {
        AlertDialog(
            onDismissRequest = { confirmHistory = false },
            title = { Text("Clear spin history?") },
            text = { Text("The recent-spin list is wiped. Cash and cruise points stay.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmHistory = false
                    onClearHistory()
                }) { Text("Clear history") }
            },
            dismissButton = { TextButton(onClick = { confirmHistory = false }) { Text("Cancel") } },
        )
    }
    if (confirmPoints) {
        AlertDialog(
            onDismissRequest = { confirmPoints = false },
            title = { Text("Reset cruise points?") },
            text = { Text("Every line's tally goes back to zero. Cash stays.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmPoints = false
                    onResetPoints()
                }) { Text("Reset points") }
            },
            dismissButton = { TextButton(onClick = { confirmPoints = false }) { Text("Cancel") } },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text("Bankroll", color = Gold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(
                snapshot.credits.money(),
                color = Foam,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                "Play money only. Type a starting cash amount, or clear the bank to zero.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 8.dp),
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Starting cash") },
                prefix = { Text("$") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            Row {
                TextButton(
                    onClick = { parsed?.let(onSetCash) },
                    enabled = parsed != null,
                ) { Text("Set cash") }
                TextButton(onClick = { confirmClear = true }) { Text("Clear bank") }
                TextButton(onClick = { confirmHistory = true }) { Text("Clear history") }
                TextButton(onClick = { confirmPoints = true }) { Text("Reset points") }
            }
        }
        item {
            Text("How a spin is chosen", color = Foam, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
            Text(
                "Denomination is the value of one credit. Bet is how many credits you play on each of the 20 lines. The spin costs denomination times bet times 20. Each reel is a virtual strip. A spin draws a stop with this app's generator, then scrolls the reel to that stop.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Text("Recent spins", color = Foam, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
        }
        if (snapshot.history.isEmpty()) {
            item { Text("No spins yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(snapshot.history) { entry ->
                val name = Catalog.games.firstOrNull { it.id == entry.gameId }?.name ?: entry.gameId
                Text(
                    "$name    bet ${entry.bet.money()}    win ${entry.win.money()}",
                    color = if (entry.win > 0) Teal else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
