package com.nicholasnixon.dailychore.ui

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nicholasnixon.dailychore.navigation.BottomTab
import com.nicholasnixon.dailychore.navigation.DailyChoreNavHost
import com.nicholasnixon.dailychore.navigation.ParentRoute
import com.nicholasnixon.dailychore.viewmodel.ChoreViewModel

/**
 * The whole app: a bottom bar with 3 tabs, and the current screen above it.
 */
@Composable
fun DailyChoreApp() {
    // One ViewModel shared by all three screens, so they all see the same data.
    val vm: ChoreViewModel = viewModel(factory = ChoreViewModel.Factory)
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()

    // Shows ViewModel messages (e.g. "Enjoy: Sticker!") as a snackbar at the bottom.
    val snackbarHostState = remember { SnackbarHostState() }
    // LaunchedEffect runs this block whenever `message` changes. showSnackbar waits until the
    // snackbar disappears, then we clear the message so the same one isn't shown again.
    LaunchedEffect(message) {
        val text = message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
        vm.clearMessage()
    }

    // The navController remembers which screen we're on and the back stack.
    val navController = rememberNavController()
    // Re-draws the bar whenever the current screen changes.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // --- Parent lock ---
    // Lock the Parent screen whenever we're on any other screen (tab switch OR the Back button).
    // onParent is null for a moment while the screen is being set up (e.g. after a rotation);
    // we only lock on a definite "false", so rotating doesn't lock it.
    val onParent: Boolean? = currentDestination?.hierarchy?.any { it.hasRoute(ParentRoute::class) }
    LaunchedEffect(onParent) {
        if (onParent == false) vm.lockParent()
    }
    // Lock when the app goes to the background (ON_STOP). A rotation also stops the Activity
    // briefly, so skip it then (isChangingConfigurations = "this is just a rotation").
    val activity = LocalContext.current as? Activity
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        if (activity?.isChangingConfigurations != true) vm.lockParent()
    }
    // When the app comes back to the foreground, check if it's a new day (ticks reset at midnight).
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        vm.refreshDate()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            viewModel = vm,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
