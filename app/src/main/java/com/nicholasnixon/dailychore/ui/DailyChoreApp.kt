package com.nicholasnixon.dailychore.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nicholasnixon.dailychore.navigation.BottomTab
import com.nicholasnixon.dailychore.navigation.DailyChoreNavHost
import com.nicholasnixon.dailychore.viewmodel.ChoreViewModel

/**
 * The whole app: a bottom bar with 3 tabs, and the current screen above it.
 */
@Composable
fun DailyChoreApp() {
    // One ViewModel shared by all three screens, so they all see the same data.
    val vm: ChoreViewModel = viewModel(factory = ChoreViewModel.Factory)
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    // The navController remembers which screen we're on and the back stack.
    val navController = rememberNavController()
    // Re-draws the bar whenever the current screen changes.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                BottomTab.entries.forEach { tab ->
                    // Is this tab's screen the one showing?
                    val selected = currentDestination?.hierarchy
                        ?.any { it.hasRoute(tab.route::class) } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                // Keep only the start screen (Today) under us, so Back goes to Today.
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Tapping the current tab again doesn't open a second copy.
                                launchSingleTop = true
                                // Coming back to a tab restores where it was.
                                restoreState = true
                            }
                        },
                        icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                        label = { Text(stringResource(tab.label)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        DailyChoreNavHost(
            navController = navController,
            uiState = uiState,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
