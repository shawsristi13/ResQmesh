package com.meshmap.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.meshmap.app.ui.screens.ChatScreen
import com.meshmap.app.ui.screens.MeshMapScreen
import com.meshmap.app.ui.screens.SosScreen
import com.meshmap.app.ui.theme.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Sos : Screen("sos", "SOS", Icons.Default.Warning)
    data object Chat : Screen("chat", "Chat", Icons.Default.ChatBubble)
    data object MeshMap : Screen("mesh", "Mesh", Icons.Default.Hub)
}

private val screens = listOf(Screen.Sos, Screen.Chat, Screen.MeshMap)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeshNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        containerColor = DeepNavy,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextPrimary,
                tonalElevation = 8.dp
            ) {
                screens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.label,
                                tint = when {
                                    selected && screen is Screen.Sos -> AlertRed
                                    selected -> IcyBlue
                                    else -> TextDim
                                }
                            )
                        },
                        label = {
                            Text(
                                screen.label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = when {
                                    selected && screen is Screen.Sos -> AlertRed
                                    selected -> IcyBlue
                                    else -> TextDim
                                }
                            )
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = if (screen is Screen.Sos) AlertRedSurface else IcyBlueSurface
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Sos.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Sos.route) {
                SosScreen()
            }
            composable(Screen.Chat.route) {
                ChatScreen()
            }
            composable(Screen.MeshMap.route) {
                MeshMapScreen()
            }
        }
    }
}
