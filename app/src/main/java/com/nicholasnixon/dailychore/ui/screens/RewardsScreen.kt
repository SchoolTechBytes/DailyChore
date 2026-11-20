package com.nicholasnixon.dailychore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nicholasnixon.dailychore.R
import com.nicholasnixon.dailychore.data.entities.Reward
import com.nicholasnixon.dailychore.ui.theme.DailyChoreTheme
import com.nicholasnixon.dailychore.viewmodel.ChoreUiState

/**
 * Where the kid spends points. Each reward shows how close the kid is (a progress bar),
 * or a "Ready!" badge and an active Redeem button once they can afford it.
 * The result message is shown as a snackbar by DailyChoreApp.
 */
@Composable
fun RewardsScreen(
    state: ChoreUiState,
    onRedeem: (Reward) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GradientHeader {
                Text(
                    stringResource(R.string.tab_rewards),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text(
                    pluralStringResource(R.plurals.points_to_spend, state.balance, state.balance),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }
        }

        if (state.rewards.isEmpty()) {
            item { EmptyState("🎁", stringResource(R.string.no_rewards_kid)) }
        } else {
            items(state.rewards, key = { it.id }) { reward ->
                RewardRow(
                    reward = reward,
                    balance = state.balance,
                    onRedeem = { onRedeem(reward) }
                )
            }
        }
    }
}

@Composable
private fun RewardRow(reward: Reward, balance: Int, onRedeem: () -> Unit) {
    val canAfford = balance >= reward.cost

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            // Affordable rewards get a soft blue card so they stand out.
            containerColor = if (canAfford) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gift emoji in an amber circle.
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxSize()
                ) {}
                Text("🎁", style = MaterialTheme.typography.titleLarge)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(reward.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    pluralStringResource(R.plurals.reward_cost, reward.cost, reward.cost),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (canAfford) {
                    // "Ready!" badge.
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            stringResource(R.string.reward_ready),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    // How close the kid is to this reward.
                    LinearProgressIndicator(
                        progress = { balance.toFloat() / reward.cost },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        drawStopIndicator = {}
                    )
                    val toGo = reward.cost - balance
                    Text(
                        pluralStringResource(R.plurals.points_to_go, toGo, toGo),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // enabled = false greys the button out, so the kid can see what's still out of reach.
            Button(onClick = onRedeem, enabled = canAfford) {
                Text(stringResource(R.string.redeem))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RewardsScreenPreview() {
    DailyChoreTheme {
        RewardsScreen(
            state = ChoreUiState(
                rewards = listOf(Reward(1, "Sticker", 5), Reward(2, "Movie night", 50)),
                balance = 12
            ),
            onRedeem = {}
        )
    }
}
