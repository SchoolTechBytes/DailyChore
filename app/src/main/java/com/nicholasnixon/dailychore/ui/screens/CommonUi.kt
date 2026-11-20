package com.nicholasnixon.dailychore.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nicholasnixon.dailychore.ui.theme.GradientBlue
import com.nicholasnixon.dailychore.ui.theme.GradientGreen

/*
 * Small pieces of UI shared by several screens.
 */

/**
 * A rounded card with a green → blue gradient and white text, used at the top of the
 * Today and Rewards screens. [content] is what goes inside (a ColumnScope, so items stack).
 */
@Composable
fun GradientHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            // Brush.linearGradient blends between the colours from top-left to bottom-right.
            .background(Brush.linearGradient(listOf(GradientGreen, GradientBlue)))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

/** A friendly "nothing here yet" message with a big emoji. */
@Composable
fun EmptyState(emoji: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(emoji, style = MaterialTheme.typography.displayMedium)
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
