package com.ramya.learnhub.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

interface TokenStore {
    fun save(token: String)
    fun get(): String?
    fun clear()
}

/** Token persisted in EncryptedSharedPreferences, with the key held in the Android Keystore. */
class EncryptedTokenStore(context: Context) : TokenStore {

    private val prefs = try {
        createPrefs(context)
    } catch (e: Exception) {
        // Keystore key lost or file corrupted (e.g. after restore): drop the token and start clean.
        context.deleteSharedPreferences(FILE_NAME)
        createPrefs(context)
    }

    private fun createPrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun save(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()

    override fun get(): String? = prefs.getString(KEY_TOKEN, null)

    override fun clear() = prefs.edit().remove(KEY_TOKEN).apply()

    private companion object {
        const val FILE_NAME = "secure_auth"
        const val KEY_TOKEN = "auth_token"
    }
}
