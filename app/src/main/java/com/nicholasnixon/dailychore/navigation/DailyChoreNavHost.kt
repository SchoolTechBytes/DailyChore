package com.nicholasnixon.dailychore.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nicholasnixon.dailychore.ui.screens.KidScreen
import com.nicholasnixon.dailychore.ui.screens.ParentScreen
import com.nicholasnixon.dailychore.ui.screens.PinScreen
import com.nicholasnixon.dailychore.ui.screens.RewardsScreen
import com.nicholasnixon.dailychore.viewmodel.ChoreUiState
import com.nicholasnixon.dailychore.viewmodel.ChoreViewModel

/**
 * The navigation graph: which screen to show for each route.
 * NavHost swaps the content when navController.navigate(...) is called.
 * The app starts on the Kid ("Today") screen.
 *
 * This is the only place that connects screens to the ViewModel: each screen gets the
 * state to draw and the ViewModel functions to call when the user does something.
 * `viewModel::toggleChore` means "pass the toggleChore function itself as the callback".
 */
@Composable
fun DailyChoreNavHost(
    navController: NavHostController,
    uiState: ChoreUiState,
    viewModel: ChoreViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = KidRoute,
        modifier = modifier
    ) {
        // composable<Route> { ... } = "when the route is Route, show this screen".
        composable<KidRoute> {
            KidScreen(state = uiState, onToggleChore = viewModel::toggleChore)
        }
        composable<RewardsRoute> {
            RewardsScreen(state = uiState, onRedeem = viewModel::redeemReward)
        }
        // The Parent screen is behind the PIN: show the PIN pad until it's unlocked.
        composable<ParentRoute> {
            if (uiState.isParentUnlocked) {
                ParentScreen(
                    state = uiState,
                    onAddChore = viewModel::addChore,
                    onDeleteChore = viewModel::deleteChore,
                    onAddReward = viewModel::addReward,
                    onDeleteReward = viewModel::deleteReward,
                    onChangePin = viewModel::changePin
                )
            } else {
                PinScreen(
                    hasPin = uiState.hasPin,
                    onCreatePin = viewModel::createPin,
                    onUnlock = viewModel::unlockParent,
                    onReset = viewModel::resetEverything
                )
            }
        }
    }
}
