package io.github.zm.auth_core.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.github.zm.auth_core.token.TokenSet
import kotlinx.serialization.json.Json

internal class AndroidEncryptedTokenStorage(
    context: Context,
    storageName: String
) : TokenStorage {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        storageName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun save(tokens: TokenSet) {
        val encoded = json.encodeToString(tokens)
        preferences.edit()
            .putString(KEY_TOKEN_SET, encoded)
            .commit()

    }

    override suspend fun read(): TokenSet? {
        val encoded = preferences.getString(KEY_TOKEN_SET, null)
            ?: return null

        return json.decodeFromString<TokenSet>(encoded)

    }

    override suspend fun clear() {
        preferences.edit()
            .remove(KEY_TOKEN_SET)
            .apply()
    }

    private companion object {
        private const val KEY_TOKEN_SET = "token_set"
    }
}