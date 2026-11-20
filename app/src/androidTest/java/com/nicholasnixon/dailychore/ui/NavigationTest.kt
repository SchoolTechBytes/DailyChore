package com.nicholasnixon.dailychore.ui

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nicholasnixon.dailychore.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Opens the real app and taps the bottom tabs, like a user would.
 * isSelectable() picks the tab button (not the screen title, which has the same text).
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun tab(label: String) = composeRule.onNode(hasText(label) and isSelectable())

    @Test
    fun startsOnTodayAndSwitchesTabs() {
        tab("Today").assertIsSelected()
        composeRule.onNodeWithText("⭐", substring = true).assertExists()

        tab("Parent").performClick()
        tab("Parent").assertIsSelected()
        // Parent is behind the PIN, so the PIN screen shows ("Create a … parent PIN" or "Enter parent PIN").
        // (This test uses the real app, so it doesn't type a PIN: that would change the real PIN.)
        composeRule.onNodeWithText("parent PIN", substring = true).assertExists()
        composeRule.onNodeWithText("Add chore").assertDoesNotExist()

        tab("Rewards").performClick()
        tab("Rewards").assertIsSelected()
        composeRule.onNodeWithText("points to spend", substring = true).assertExists()
    }
}
