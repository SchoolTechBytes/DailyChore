package com.nicholasnixon.dailychore.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nicholasnixon.dailychore.ui.screens.KidScreen
import com.nicholasnixon.dailychore.ui.screens.ParentScreen
import com.nicholasnixon.dailychore.ui.screens.RewardsScreen
import com.nicholasnixon.dailychore.viewmodel.ChoreUiState

/**
 * The navigation graph: which screen to show for each route.
 * NavHost swaps the content when navController.navigate(...) is called.
 * The app starts on the Kid ("Today") screen.
 */
@Composable
fun DailyChoreNavHost(
    navController: NavHostController,
    uiState: ChoreUiState,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = KidRoute,
        modifier = modifier
    ) {
        // composable<Route> { ... } = "when the route is Route, show this screen".
        composable<KidRoute> { KidScreen(state = uiState) }
        composable<RewardsRoute> { RewardsScreen(state = uiState) }
        composable<ParentRoute> { ParentScreen(state = uiState) }
    }
}
