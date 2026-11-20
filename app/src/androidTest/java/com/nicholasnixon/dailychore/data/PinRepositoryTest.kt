package com.nicholasnixon.dailychore.data

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nicholasnixon.dailychore.data.repository.PinRepository
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Uses its own storage file ("pin_test"), so the real app's PIN is never touched. */
@RunWith(AndroidJUnit4::class)
class PinRepositoryTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var pins: PinRepository

    @Before
    fun setUp() {
        pins = PinRepository(context, prefsName = "pin_test")
        pins.clearPin()
    }

    @After
    fun tearDown() {
        pins.clearPin()
    }

    @Test
    fun noPinAtFirst() {
        assertFalse(pins.hasPin())
        assertFalse(pins.checkPin("1234"))
    }

    @Test
    fun rightPinPassesWrongPinFails() {
        pins.setPin("1234")
        assertTrue(pins.hasPin())
        assertTrue(pins.checkPin("1234"))
        assertFalse(pins.checkPin("4321"))
    }

    @Test
    fun pinIsNotStoredAsPlainText() {
        pins.setPin("1234")
        val stored = context.getSharedPreferences("pin_test", Context.MODE_PRIVATE).all.values
        assertFalse(stored.any { it.toString().contains("1234") })
    }

    @Test
    fun clearPinRemovesIt() {
        pins.setPin("1234")
        pins.clearPin()
        assertFalse(pins.hasPin())
        assertFalse(pins.checkPin("1234"))
    }
}
