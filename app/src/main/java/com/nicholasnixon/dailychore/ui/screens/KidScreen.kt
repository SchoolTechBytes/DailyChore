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
import com.nicholasnixon.dailychore.ui.theme.DailyChoreTheme
import com.nicholasnixon.dailychore.viewmodel.ChoreUiState

/**
 * The kid's "Today" screen. PLACEHOLDER (Stage 4): shows a summary only.
 * Stage 5 adds the chore list with tick boxes.
 *
 * "State hoisting": the screen doesn't fetch data itself, it just draws the [state] it is given.
 * That's why the @Preview below can show it with made-up data.
 */
@Composable
fun KidScreen(state: ChoreUiState, modifier: Modifier = Modifier) {
    val done = state.chores.count { it.id in state.completedTodayIds }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.tab_today), style = MaterialTheme.typography.headlineMedium)
        Text("⭐ ${state.balance} points", style = MaterialTheme.typography.titleLarge)
        Text("$done of ${state.chores.size} chores done today")
    }
}

@Preview(showBackground = true)
@Composable
private fun KidScreenPreview() {
    DailyChoreTheme {
        KidScreen(
            ChoreUiState(
                chores = listOf(Chore(1, "Make bed", 5), Chore(2, "Feed dog", 10)),
                balance = 15,
                completedTodayIds = setOf(1)
            )
        )
    }
}
