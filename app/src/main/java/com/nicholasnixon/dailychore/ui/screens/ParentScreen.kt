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
import com.nicholasnixon.dailychore.data.entities.Chore
import com.nicholasnixon.dailychore.data.entities.Reward
import com.nicholasnixon.dailychore.ui.theme.DailyChoreTheme
import com.nicholasnixon.dailychore.viewmodel.ChoreUiState

/**
 * Where the parent sets up chores and rewards. PLACEHOLDER (Stage 4): summary only.
 * Stage 5 adds the add/delete forms.
 */
@Composable
fun ParentScreen(state: ChoreUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.tab_parent), style = MaterialTheme.typography.headlineMedium)
        Text("${state.chores.size} chores · ${state.rewards.size} rewards set up")
    }
}

@Preview(showBackground = true)
@Composable
private fun ParentScreenPreview() {
    DailyChoreTheme {
        ParentScreen(
            ChoreUiState(
                chores = listOf(Chore(1, "Make bed", 5)),
                rewards = listOf(Reward(1, "Sticker", 5))
            )
        )
    }
}
