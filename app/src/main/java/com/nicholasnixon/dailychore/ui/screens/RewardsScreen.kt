package com.nicholasnixon.dailychore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nicholasnixon.dailychore.R
import com.nicholasnixon.dailychore.data.entities.Reward
import com.nicholasnixon.dailychore.ui.theme.DailyChoreTheme
import com.nicholasnixon.dailychore.viewmodel.ChoreUiState

/**
 * Where the kid spends points. PLACEHOLDER (Stage 4): summary only.
 * Stage 5 adds the reward list with "Redeem" buttons.
 */
@Composable
fun RewardsScreen(state: ChoreUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.tab_rewards), style = MaterialTheme.typography.headlineMedium)
        Text("${state.balance} points to spend", style = MaterialTheme.typography.titleLarge)
        Text("${state.rewards.size} rewards available")
    }
}

@Preview(showBackground = true)
@Composable
private fun RewardsScreenPreview() {
    DailyChoreTheme {
        RewardsScreen(
            ChoreUiState(
                rewards = listOf(Reward(1, "Sticker", 5), Reward(2, "Movie night", 50)),
                balance = 12
            )
        )
    }
}
