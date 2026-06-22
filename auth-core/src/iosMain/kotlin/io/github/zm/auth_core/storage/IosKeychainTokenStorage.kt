package io.github.zm.auth_core.storage

import io.github.zm.auth_core.token.TokenSet
import kotlinx.serialization.json.Json

internal class IosKeychainTokenStorage(
    private val storageName: String
) : TokenStorage {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val secureStorage = IosKeychainStringStorage(storageName)

    override suspend fun save(tokens: TokenSet) {
        val encoded = json.encodeToString(tokens)
        secureStorage.save(KEY_TOKENS, encoded)
    }

    override suspend fun read(): TokenSet? {
        val encoded = secureStorage.read(KEY_TOKENS)
            ?: return null

        return json.decodeFromString<TokenSet>(encoded)
    }

    override suspend fun clear() {
        secureStorage.clear(KEY_TOKENS)
    }

    private companion object {
        const val KEY_TOKENS = "tokens"
    }
}
