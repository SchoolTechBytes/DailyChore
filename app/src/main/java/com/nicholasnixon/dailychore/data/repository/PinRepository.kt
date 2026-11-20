package com.nicholasnixon.dailychore.data.repository

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Stores the parent PIN.
 *
 * - It uses SharedPreferences: Android's built-in small key-value storage (a private file
 *   inside the app), which is enough for one setting and needs no extra library.
 * - It never saves the PIN itself. It saves a "salted hash": random bytes (the salt) mixed with
 *   the PIN and put through SHA-256, a one-way function. To check a PIN we hash it the same way
 *   and compare. This keeps the PIN from being read in plain text; it is meant to keep kids out
 *   of the Parent screen, not to be bank-level security.
 *
 * @param prefsName the storage file name. Tests use a different name so they never touch
 *                  the real app's PIN.
 */
class PinRepository(context: Context, prefsName: String = "parent_pin") {

    private val prefs = context.applicationContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    fun hasPin(): Boolean = prefs.contains(KEY_HASH)

    fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        // edit { } is a core-ktx helper: make changes, then save them in the background.
        prefs.edit {
            putString(KEY_SALT, salt.encode())
            putString(KEY_HASH, hash(pin, salt).encode())
        }
    }

    fun checkPin(pin: String): Boolean {
        val salt = prefs.getString(KEY_SALT, null)?.decode() ?: return false
        val stored = prefs.getString(KEY_HASH, null)?.decode() ?: return false
        // isEqual compares in constant time (doesn't stop early at the first different byte).
        return MessageDigest.isEqual(stored, hash(pin, salt))
    }

    fun clearPin() {
        prefs.edit { clear() }
    }

    private fun hash(pin: String, salt: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").run {
            update(salt)
            digest(pin.toByteArray())
        }

    // Base64 turns bytes into plain text so they can be stored as a String.
    private fun ByteArray.encode(): String = Base64.encodeToString(this, Base64.NO_WRAP)
    private fun String.decode(): ByteArray = Base64.decode(this, Base64.NO_WRAP)

    private companion object {
        const val KEY_SALT = "salt"
        const val KEY_HASH = "hash"
    }
}
