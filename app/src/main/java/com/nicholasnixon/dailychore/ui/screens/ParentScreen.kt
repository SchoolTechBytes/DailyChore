package com.nicholasnixon.dailychore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nicholasnixon.dailychore.R
import com.nicholasnixon.dailychore.data.entities.Chore
import com.nicholasnixon.dailychore.data.entities.Reward
import com.nicholasnixon.dailychore.ui.theme.DailyChoreTheme
import com.nicholasnixon.dailychore.viewmodel.ChoreUiState
import com.nicholasnixon.dailychore.viewmodel.ChoreViewModel.Companion.PIN_LENGTH

/**
 * Where the parent sets things up: add/delete chores and rewards.
 * Everything is in one LazyColumn so the whole screen scrolls together.
 */
@Composable
fun ParentScreen(
    state: ChoreUiState,
    onAddChore: (String, Int) -> Unit,
    onDeleteChore: (Chore) -> Unit,
    onAddReward: (String, Int) -> Unit,
    onDeleteReward: (Reward) -> Unit,
    onChangePin: (current: String, new: String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var showChangePin by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(stringResource(R.string.tab_parent), style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(R.string.parent_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- Chores section (one card: form + list) ---
        item {
            SectionCard(title = stringResource(R.string.section_chores)) {
                AddItemForm(
                    nameLabel = stringResource(R.string.chore_name),
                    numberLabel = stringResource(R.string.points_label),
                    buttonLabel = stringResource(R.string.add_chore),
                    onAdd = onAddChore
                )
                if (state.chores.isEmpty()) {
                    Text(
                        stringResource(R.string.parent_no_chores),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                state.chores.forEach { chore ->
                    ItemRow(
                        title = chore.title,
                        detail = stringResource(R.string.chore_points, chore.points),
                        onDelete = { onDeleteChore(chore) }
                    )
                }
            }
        }

        // --- Rewards section ---
        item {
            SectionCard(title = stringResource(R.string.section_rewards)) {
                AddItemForm(
                    nameLabel = stringResource(R.string.reward_name),
                    numberLabel = stringResource(R.string.cost_label),
                    buttonLabel = stringResource(R.string.add_reward),
                    onAdd = onAddReward
                )
                if (state.rewards.isEmpty()) {
                    Text(
                        stringResource(R.string.parent_no_rewards),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                state.rewards.forEach { reward ->
                    ItemRow(
                        title = reward.title,
                        detail = pluralStringResource(R.plurals.reward_cost, reward.cost, reward.cost),
                        onDelete = { onDeleteReward(reward) }
                    )
                }
            }
        }

        item {
            OutlinedButton(onClick = { showChangePin = true }) {
                Text("🔑  " + stringResource(R.string.change_pin))
            }
        }
    }

    if (showChangePin) {
        ChangePinDialog(
            onChangePin = onChangePin,
            onDismiss = { showChangePin = false }
        )
    }
}

/**
 * Asks for the current PIN and a new one. Stays open with an error if the current PIN is wrong.
 * NumberPassword shows the number keyboard; PasswordVisualTransformation shows dots instead of digits.
 */
@Composable
private fun ChangePinDialog(onChangePin: (String, String) -> Boolean, onDismiss: () -> Unit) {
    var current by rememberSaveable { mutableStateOf("") }
    var new by rememberSaveable { mutableStateOf("") }
    var wrong by rememberSaveable { mutableStateOf(false) }

    // Only digits, at most PIN_LENGTH of them.
    fun clean(typed: String) = typed.filter { it.isDigit() }.take(PIN_LENGTH)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.change_pin)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = clean(it); wrong = false },
                    label = { Text(stringResource(R.string.change_pin_current)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = wrong
                )
                OutlinedTextField(
                    value = new,
                    onValueChange = { new = clean(it) },
                    label = { Text(stringResource(R.string.change_pin_new)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                if (wrong) {
                    Text(
                        stringResource(R.string.change_pin_wrong),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (onChangePin(current, new)) onDismiss() else wrong = true },
                enabled = current.length == PIN_LENGTH && new.length == PIN_LENGTH
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/** A rounded card holding one section (a title, the add form, and the list). */
@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

/** One existing chore or reward, with a delete (bin) button. */
@Composable
private fun ItemRow(title: String, detail: String, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(detail, style = MaterialTheme.typography.bodyMedium)
        IconButton(onClick = onDelete) {
            Icon(
                painterResource(R.drawable.ic_delete),
                // Read out by screen readers, e.g. "Delete Make bed".
                contentDescription = stringResource(R.string.delete_item, title)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ParentScreenPreview() {
    DailyChoreTheme {
        ParentScreen(
            state = ChoreUiState(
                chores = listOf(Chore(1, "Make bed", 5)),
                rewards = listOf(Reward(1, "Sticker", 5))
            ),
            onAddChore = { _, _ -> },
            onDeleteChore = {},
            onAddReward = { _, _ -> },
            onDeleteReward = {},
            onChangePin = { _, _ -> true }
        )
    }
}
