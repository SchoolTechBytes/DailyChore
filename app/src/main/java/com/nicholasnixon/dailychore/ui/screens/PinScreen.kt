package com.nicholasnixon.dailychore.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nicholasnixon.dailychore.R
import com.nicholasnixon.dailychore.ui.theme.DailyChoreTheme
import com.nicholasnixon.dailychore.viewmodel.ChoreViewModel.Companion.PIN_LENGTH

/**
 * The lock in front of the Parent screen. Two modes:
 * - no PIN yet ([hasPin] = false): type a new PIN, then type it again to confirm → [onCreatePin]
 * - PIN exists: type it → [onUnlock] says if it was right; "Forgot PIN?" → warning → [onReset]
 *
 * The PIN is checked as soon as the 4th digit is typed, so there's no "OK" button.
 * Only what's being typed lives here (rememberSaveable keeps it on rotation); the real PIN
 * is checked by the ViewModel.
 */
@Composable
fun PinScreen(
    hasPin: Boolean,
    onCreatePin: (String) -> Unit,
    onUnlock: (String) -> Boolean,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var entered by rememberSaveable { mutableStateOf("") }
    // While creating: the first PIN typed, waiting to be confirmed.
    var firstPin by rememberSaveable { mutableStateOf<String?>(null) }
    // A string resource id for the error to show, or null.
    var errorRes by rememberSaveable { mutableStateOf<Int?>(null) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }

    // Called when the 4th digit is typed.
    fun submit(pin: String) {
        entered = ""
        if (!hasPin) {
            when (firstPin) {
                null -> firstPin = pin                    // step 1: remember it, ask again
                pin -> onCreatePin(pin)                   // step 2: matches → save
                else -> { firstPin = null; errorRes = R.string.pin_mismatch }
            }
        } else if (!onUnlock(pin)) {
            errorRes = R.string.pin_wrong
        }
    }

    fun onDigit(digit: Char) {
        if (entered.length >= PIN_LENGTH) return
        errorRes = null
        entered += digit
        if (entered.length == PIN_LENGTH) submit(entered)
    }

    val title = when {
        hasPin -> R.string.pin_enter_title
        firstPin == null -> R.string.pin_create_title
        else -> R.string.pin_confirm_title
    }

    // BoxWithConstraints tells us the screen height. The Column is at least that tall (so the
    // keypad sits in the middle) but can scroll when it doesn't fit, e.g. in landscape.
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🔒", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(title), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))
            PinDots(filled = entered.length)
            Spacer(Modifier.height(12.dp))
            // Always takes up space, so the keypad doesn't jump when an error appears.
            Text(
                text = errorRes?.let { stringResource(it) } ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            NumberPad(
                onDigit = ::onDigit,
                onBackspace = { entered = entered.dropLast(1) }
            )
            if (hasPin) {
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = { showResetDialog = true }) {
                    Text(stringResource(R.string.pin_forgot))
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_title)) },
            text = { Text(stringResource(R.string.reset_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    entered = ""
                    errorRes = null
                    onReset()
                }) {
                    Text(stringResource(R.string.reset_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/** 4 circles; the first [filled] are coloured in. Screen readers hear "2 of 4 digits entered". */
@Composable
private fun PinDots(filled: Int) {
    val description = stringResource(R.string.pin_dots, filled, PIN_LENGTH)
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.semantics { contentDescription = description }
    ) {
        repeat(PIN_LENGTH) { i ->
            val color = MaterialTheme.colorScheme.primary
            Box(
                Modifier
                    .size(18.dp)
                    .border(2.dp, color, CircleShape)
                    .background(if (i < filled) color else MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

/** A phone-style keypad: 1-9, then backspace, 0. */
@Composable
private fun NumberPad(onDigit: (Char) -> Unit, onBackspace: () -> Unit) {
    val rows = listOf("123", "456", "789")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { digit -> PadKey(digit.toString()) { onDigit(digit) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Spacer(Modifier.size(72.dp))
            PadKey("0") { onDigit('0') }
            PadKey("⌫", description = stringResource(R.string.pin_backspace), onClick = onBackspace)
        }
    }
}

@Composable
private fun PadKey(label: String, description: String? = null, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        shape = CircleShape,
        // Soft blue keys (secondaryContainer) to go with the green PIN dots.
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier
            .size(72.dp)
            .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)
    ) {
        Text(label, style = MaterialTheme.typography.titleLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun PinScreenPreview() {
    DailyChoreTheme {
        PinScreen(hasPin = true, onCreatePin = {}, onUnlock = { false }, onReset = {})
    }
}
