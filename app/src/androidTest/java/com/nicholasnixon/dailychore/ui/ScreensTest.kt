package com.nicholasnixon.dailychore.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nicholasnixon.dailychore.data.entities.Chore
import com.nicholasnixon.dailychore.data.entities.Reward
import com.nicholasnixon.dailychore.ui.screens.KidScreen
import com.nicholasnixon.dailychore.ui.screens.ParentScreen
import com.nicholasnixon.dailychore.ui.screens.PinScreen
import com.nicholasnixon.dailychore.ui.screens.RewardsScreen
import com.nicholasnixon.dailychore.viewmodel.ChoreUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests each screen on its own with made-up state (no database, no ViewModel).
 * This works because the screens are "stateless": they draw what they're given and
 * report taps through callbacks, which the tests record.
 */
@RunWith(AndroidJUnit4::class)
class ScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val bed = Chore(1, "Make bed", 5)
    private val sticker = Reward(1, "Sticker", 5)

    @Test
    fun kidScreen_tappingChoreReportsIt() {
        var toggled: Chore? = null
        composeRule.setContent {
            KidScreen(ChoreUiState(chores = listOf(bed)), onToggleChore = { toggled = it })
        }
        composeRule.onNodeWithText("Make bed").assertIsOff()
        composeRule.onNodeWithText("Make bed").performClick()
        assertEquals(bed, toggled)
    }

    @Test
    fun kidScreen_showsDoneChoreAsTicked() {
        composeRule.setContent {
            KidScreen(
                ChoreUiState(chores = listOf(bed), balance = 5, completedTodayIds = setOf(1)),
                onToggleChore = {}
            )
        }
        composeRule.onNodeWithText("Make bed").assertIsOn()
        composeRule.onNodeWithText("All done", substring = true).assertExists()
    }

    @Test
    fun kidScreen_emptyStateWhenNoChores() {
        composeRule.setContent { KidScreen(ChoreUiState(), onToggleChore = {}) }
        composeRule.onNodeWithText("No chores yet", substring = true).assertExists()
    }

    @Test
    fun rewardsScreen_redeemDisabledWhenTooFewPoints() {
        composeRule.setContent {
            RewardsScreen(ChoreUiState(rewards = listOf(sticker), balance = 4), onRedeem = {})
        }
        composeRule.onNodeWithText("Redeem").assertIsNotEnabled()
    }

    @Test
    fun rewardsScreen_redeemWorksWithEnoughPoints() {
        var redeemed: Reward? = null
        composeRule.setContent {
            RewardsScreen(
                ChoreUiState(rewards = listOf(sticker), balance = 5),
                onRedeem = { redeemed = it }
            )
        }
        composeRule.onNodeWithText("Redeem").assertIsEnabled().performClick()
        assertEquals(sticker, redeemed)
    }

    @Test
    fun parentScreen_addChoreOnlyWhenFormValid() {
        var added: Pair<String, Int>? = null
        composeRule.setContent {
            ParentScreen(
                state = ChoreUiState(),
                onAddChore = { name, points -> added = name to points },
                onDeleteChore = {},
                onAddReward = { _, _ -> },
                onDeleteReward = {},
                onChangePin = { _, _ -> true }
            )
        }
        composeRule.onNodeWithText("Add chore").assertIsNotEnabled()

        composeRule.onNodeWithText("Chore name").performTextInput("Make bed")
        composeRule.onNodeWithText("Add chore").assertIsNotEnabled() // points still empty

        // Two "Points"-like fields exist ("Points" and "Cost"); this one is the chore form's.
        composeRule.onNodeWithText("Points").performTextInput("5")
        composeRule.onNodeWithText("Add chore").assertIsEnabled().performClick()

        assertEquals("Make bed" to 5, added)
    }

    @Test
    fun parentScreen_deleteButtonReportsChore() {
        var deleted: Chore? = null
        composeRule.setContent {
            ParentScreen(
                state = ChoreUiState(chores = listOf(bed)),
                onAddChore = { _, _ -> },
                onDeleteChore = { deleted = it },
                onAddReward = { _, _ -> },
                onDeleteReward = {},
                onChangePin = { _, _ -> true }
            )
        }
        composeRule.onNodeWithContentDescription("Delete Make bed").performClick()
        assertEquals(bed, deleted)
    }

    // --- PIN screen ---

    private fun typePin(pin: String) = pin.forEach { composeRule.onNodeWithText(it.toString()).performClick() }

    @Test
    fun pinScreen_createAsksTwiceThenSaves() {
        var created: String? = null
        composeRule.setContent {
            PinScreen(hasPin = false, onCreatePin = { created = it }, onUnlock = { false }, onReset = {})
        }
        composeRule.onNodeWithText("Create a 4-digit parent PIN").assertExists()
        typePin("1234")
        composeRule.onNodeWithText("Enter the PIN again to confirm").assertExists()
        typePin("1234")
        assertEquals("1234", created)
    }

    @Test
    fun pinScreen_createMismatchStartsAgain() {
        var created: String? = null
        composeRule.setContent {
            PinScreen(hasPin = false, onCreatePin = { created = it }, onUnlock = { false }, onReset = {})
        }
        typePin("1234")
        typePin("5678")
        composeRule.onNodeWithText("didn't match", substring = true).assertExists()
        composeRule.onNodeWithText("Create a 4-digit parent PIN").assertExists()
        assertEquals(null, created)
    }

    @Test
    fun pinScreen_wrongPinShowsError() {
        var tried: String? = null
        composeRule.setContent {
            PinScreen(hasPin = true, onCreatePin = {}, onUnlock = { tried = it; it == "1234" }, onReset = {})
        }
        typePin("9999")
        assertEquals("9999", tried)
        composeRule.onNodeWithText("Wrong PIN. Try again.").assertExists()
        composeRule.onNodeWithContentDescription("0 of 4 digits entered").assertExists()
    }

    @Test
    fun pinScreen_forgotPinCancelKeepsDataEraseResets() {
        var resets = 0
        composeRule.setContent {
            PinScreen(hasPin = true, onCreatePin = {}, onUnlock = { false }, onReset = { resets++ })
        }
        composeRule.onNodeWithText("Forgot PIN?").performClick()
        composeRule.onNodeWithText("Reset everything?").assertExists()
        composeRule.onNodeWithText("Cancel").performClick()
        assertEquals(0, resets)

        composeRule.onNodeWithText("Forgot PIN?").performClick()
        composeRule.onNodeWithText("Erase & reset").performClick()
        assertEquals(1, resets)
    }
}
