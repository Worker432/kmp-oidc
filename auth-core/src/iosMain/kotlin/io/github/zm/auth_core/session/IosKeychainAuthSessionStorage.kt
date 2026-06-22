package io.github.zm.auth_core.session

import io.github.zm.auth_core.storage.IosKeychainStringStorage
import kotlinx.serialization.json.Json

internal class IosKeychainAuthSessionStorage(
    storageName: String
) : AuthSessionStorage {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val secureStorage = IosKeychainStringStorage(storageName)

    override suspend fun save(session: AuthSession) {
        val encoded = json.encodeToString(session)
        secureStorage.save(KEY_AUTH_SESSION, encoded)
    }

    override suspend fun read(): AuthSession? {
        val encoded = secureStorage.read(KEY_AUTH_SESSION)
            ?: return null

        return json.decodeFromString<AuthSession>(encoded)
    }

    override suspend fun clear() {
        secureStorage.clear(KEY_AUTH_SESSION)
    }

    private companion object {
        private const val KEY_AUTH_SESSION = "auth_session"
    }
}
