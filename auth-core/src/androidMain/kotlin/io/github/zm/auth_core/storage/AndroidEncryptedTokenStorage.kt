package io.github.zm.auth_core.storage

import android.content.Context
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

    private val secureStorage = AndroidEncryptedStringStorage(
        context = context,
        storageName = storageName
    )

    override suspend fun save(tokens: TokenSet) {
        val encoded = json.encodeToString(tokens)
        secureStorage.save(KEY_TOKEN_SET, encoded)
    }

    override suspend fun read(): TokenSet? {
        val encoded = secureStorage.read(KEY_TOKEN_SET)
            ?: return null

        return json.decodeFromString<TokenSet>(encoded)
    }

    override suspend fun clear() {
        secureStorage.clear(KEY_TOKEN_SET)
    }

    private companion object {
        private const val KEY_TOKEN_SET = "token_set"
    }
}
