package com.nicholasnixon.dailychore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nicholasnixon.dailychore.ui.theme.DailyChoreTheme

/**
 * A small form: a name, a number, and an Add button.
 * Used twice on the Parent screen (once for chores, once for rewards).
 *
 * The typed text is the only state kept inside this composable. rememberSaveable keeps it
 * when the phone rotates (plain remember would lose it).
 */
@Composable
fun AddItemForm(
    nameLabel: String,
    numberLabel: String,
    buttonLabel: String,
    onAdd: (name: String, number: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // `by` lets us read/write the state like a normal variable.
    var name by rememberSaveable { mutableStateOf("") }
    var number by rememberSaveable { mutableStateOf("") }

    // toIntOrNull() gives null for "" or anything that isn't a whole number.
    val numberValue = number.toIntOrNull()
    val canAdd = name.isNotBlank() && numberValue != null && numberValue > 0

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(nameLabel) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = number,
                // Only keep digits, max 4 of them, so the number can't be negative or huge.
                onValueChange = { typed -> number = typed.filter { it.isDigit() }.take(4) },
                label = { Text(numberLabel) },
                singleLine = true,
                // Shows the number keyboard instead of letters.
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(120.dp)
            )
            Button(
                onClick = {
                    onAdd(name.trim(), numberValue ?: 0)
                    name = ""
                    number = ""
                },
                // Greyed out until the form is valid (same rule the ViewModel checks).
                enabled = canAdd
            ) {
                Text(buttonLabel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddItemFormPreview() {
    DailyChoreTheme {
        AddItemForm("Chore name", "Points", "Add chore", onAdd = { _, _ -> })
    }
}
