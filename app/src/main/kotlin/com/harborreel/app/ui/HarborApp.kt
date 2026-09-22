package com.harborreel.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("lobby", "Games", Icons.Filled.Home),
    Tab("voyage", "Voyage", Icons.Filled.Sailing),
    Tab("bank", "Bank", Icons.Filled.AccountBalance),
)

@Composable
fun HarborApp(model: CasinoViewModel) {
    val nav = rememberNavController()

    Box(
        Modifier
            .fillMaxSize()
            .background(Navy)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        NavHost(
            navController = nav,
            startDestination = "lobby",
            modifier = Modifier.fillMaxSize(),
        ) {
            composable("lobby") {
                TabPage(nav) {
                    LobbyScreen(
                        snapshot = model.snapshot,
                        onOpen = { id -> nav.navigate("game/$id") },
                    )
                }
            }
            composable("voyage") {
                TabPage(nav) {
                    VoyageScreen(
                        snapshot = model.snapshot,
                        onSelect = model::setLine,
                        onReset = model::resetPoints,
                        onResetLine = model::resetLinePoints,
                    )
                }
            }
            composable("bank") {
                TabPage(nav) {
                    BankScreen(
                        snapshot = model.snapshot,
                        onSetCash = model::setCash,
                        onClear = model::clearCash,
                        onClearHistory = model::clearHistory,
                        onResetPoints = model::resetPoints,
                    )
                }
            }
            composable("game/{gameId}") { backStack ->
                val gameId = backStack.arguments?.getString("gameId").orEmpty()
                GameScreen(
                    gameId = gameId,
                    model = model,
                    onBack = { nav.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun TabPage(nav: NavHostController, content: @Composable () -> Unit) {
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route.orEmpty()
    Scaffold(
        containerColor = Navy,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            NavigationBar(containerColor = NavyRaised) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = route == tab.route,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            content()
        }
    }
}
