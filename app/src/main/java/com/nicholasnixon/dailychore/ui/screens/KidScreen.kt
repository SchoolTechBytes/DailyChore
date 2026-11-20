package com.nicholasnixon.dailychore.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nicholasnixon.dailychore.R
import com.nicholasnixon.dailychore.data.entities.Chore
import com.nicholasnixon.dailychore.ui.theme.DailyChoreTheme
import com.nicholasnixon.dailychore.viewmodel.ChoreUiState

/**
 * The kid's "Today" screen: a colourful points header, progress, and today's chores to tick off.
 *
 * "State hoisting": this screen doesn't fetch or change data itself. It draws the [state] it is
 * given and reports taps through [onToggleChore]. That's why the @Preview and the tests can use it
 * with made-up data.
 */
@Composable
fun KidScreen(
    state: ChoreUiState,
    onToggleChore: (Chore) -> Unit,
    modifier: Modifier = Modifier
) {
    val total = state.chores.size
    val done = state.chores.count { it.id in state.completedTodayIds }

    // One LazyColumn for the whole screen, so everything scrolls together (also in landscape).
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { PointsHeader(balance = state.balance, done = done, total = total) }

        if (total == 0) {
            item { EmptyState("🧹", stringResource(R.string.no_chores_kid)) }
        } else {
            item {
                // AnimatedVisibility: the banner pops in (bouncy scale) when the last chore is done.
                AnimatedVisibility(
                    visible = done == total,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    AllDoneBanner()
                }
            }
            // key = chore.id helps Compose keep track of rows when the list changes.
            items(state.chores, key = { it.id }) { chore ->
                ChoreRow(
                    chore = chore,
                    isDone = chore.id in state.completedTodayIds,
                    onToggle = { onToggleChore(chore) }
                )
            }
        }
    }
}

/** The green → blue card with the points total and today's progress. */
@Composable
private fun PointsHeader(balance: Int, done: Int, total: Int) {
    // animateIntAsState: when balance changes, the number counts up/down instead of jumping.
    val shownBalance by animateIntAsState(targetValue = balance, animationSpec = tween(600), label = "points")

    GradientHeader {
        Text(
            stringResource(R.string.tab_today),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.85f)
        )
        Text(
            pluralStringResource(R.plurals.points_balance, shownBalance, shownBalance),
            style = MaterialTheme.typography.displaySmall,
            color = Color.White
        )
        if (total > 0) {
            LinearProgressIndicator(
                progress = { done.toFloat() / total },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
                drawStopIndicator = {}
            )
            Text(
                pluralStringResource(R.plurals.chores_done_today, total, done, total),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

@Composable
private fun AllDoneBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Text(
            stringResource(R.string.all_done),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )
    }
}

/** One chore: tapping anywhere on the card ticks/unticks it, with a little bounce. */
@Composable
private fun ChoreRow(chore: Chore, isDone: Boolean, onToggle: () -> Unit) {
    // The card fades to soft green when done.
    val containerColor by animateColorAsState(
        targetValue = if (isDone) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "choreColor"
    )

    // Bounce: grow a little, then spring back. Skipped on the first draw so the list
    // doesn't bounce every time the screen opens.
    val scale = remember { Animatable(1f) }
    val firstDraw = remember { booleanArrayOf(true) }
    LaunchedEffect(isDone) {
        if (firstDraw[0]) {
            firstDraw[0] = false
        } else if (isDone) {
            scale.animateTo(1.05f, tween(100))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().scale(scale.value),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                // toggleable makes the whole row act like one checkbox (bigger target for kids,
                // and screen readers announce it as a checkbox).
                .toggleable(value = isDone, role = Role.Checkbox, onValueChange = { onToggle() })
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // onCheckedChange = null: the Row above handles the tap.
            Checkbox(
                checked = isDone,
                onCheckedChange = null,
                modifier = Modifier.padding(12.dp),
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Text(
                text = chore.title,
                style = MaterialTheme.typography.titleMedium,
                textDecoration = if (isDone) TextDecoration.LineThrough else null,
                modifier = Modifier.weight(1f)
            )
            // A small amber "+5" pill.
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    stringResource(R.string.chore_points, chore.points),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KidScreenPreview() {
    DailyChoreTheme {
        KidScreen(
            state = ChoreUiState(
                chores = listOf(Chore(1, "Make bed", 5), Chore(2, "Feed dog", 10)),
                balance = 15,
                completedTodayIds = setOf(1)
            ),
            onToggleChore = {}
        )
    }
}
