package io.github.zm.auth_core.token

import io.github.zm.auth_core.runSuspendTest
import io.github.zm.auth_core.config.AuthConfig
import io.github.zm.auth_core.discovery.DiscoveryDocument
import io.github.zm.auth_core.discovery.DiscoveryManager
import io.github.zm.auth_core.request.tokenRefresher.TokenRefreshRequest
import io.github.zm.auth_core.request.tokenRefresher.TokenRefresher
import io.github.zm.auth_core.result.TokenResult
import io.github.zm.auth_core.storage.TokenStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class DefaultTokenManagerTest {
    @Test
    fun `returns NeedLogin when storage is empty`() = runSuspendTest {
        val manager = createManager(
            storage = FakeTokenStorage()
        )

        val result = manager.getValidAccessToken()

        assertEquals(TokenResult.NeedLogin, result)
    }

    @Test
    fun `returns cached token when access token is still valid`() = runSuspendTest {
        val manager = createManager(
            storage = FakeTokenStorage(
                tokenSet = TokenSet(
                    accessToken = "access-token",
                    refreshToken = "refresh-token",
                    idToken = null,
                    expiresAt = Clock.System.now() + 1.hours
                )
            )
        )

        val result = manager.getValidAccessToken()

        val success = assertIs<TokenResult.Success>(result)
        assertEquals("access-token", success.accessToken)
    }

    @Test
    fun `refreshes expired token when refresh token exists`() = runSuspendTest {
        val storage = FakeTokenStorage(
            tokenSet = TokenSet(
                accessToken = "expired",
                refreshToken = "refresh-token",
                idToken = null,
                expiresAt = Clock.System.now() - 1.hours
            )
        )
        val refresher = FakeTokenRefresher(
            refreshed = TokenSet(
                accessToken = "new-access-token",
                refreshToken = "new-refresh-token",
                idToken = null,
                expiresAt = Clock.System.now() + 1.hours
            )
        )
        val manager = createManager(
            storage = storage,
            refresher = refresher
        )

        val result = manager.getValidAccessToken()

        val success = assertIs<TokenResult.Success>(result)
        assertEquals("new-access-token", success.accessToken)
        assertEquals("new-access-token", storage.tokenSet?.accessToken)
        assertTrue(refresher.called)
    }

    @Test
    fun `clears storage and asks for login when token is expired without refresh token`() = runSuspendTest {
        val storage = FakeTokenStorage(
            tokenSet = TokenSet(
                accessToken = "expired",
                refreshToken = null,
                idToken = null,
                expiresAt = Clock.System.now() - 1.hours
            )
        )
        val manager = createManager(storage = storage)

        val result = manager.getValidAccessToken()

        assertEquals(TokenResult.NeedLogin, result)
        assertEquals(null, storage.tokenSet)
    }

    private fun createManager(
        storage: FakeTokenStorage,
        refresher: FakeTokenRefresher = FakeTokenRefresher(
            refreshed = TokenSet(
                accessToken = "unused",
                refreshToken = "unused",
                idToken = null,
                expiresAt = Clock.System.now() + 1.hours
            )
        )
    ): DefaultTokenManager {
        return DefaultTokenManager(
            config = AuthConfig(
                clientId = "client-id",
                issuer = "https://issuer.example.com",
                redirectUri = "myapp://callback",
                logoutRedirectUri = "myapp://logout"
            ),
            discoveryManager = object : DiscoveryManager {
                override suspend fun getDiscovery(): DiscoveryDocument {
                    return DiscoveryDocument(
                        issuer = "https://issuer.example.com",
                        authorizationEndpoint = "https://issuer.example.com/auth",
                        tokenEndpoint = "https://issuer.example.com/token"
                    )
                }
            },
            tokenStorage = storage,
            tokenRefresher = refresher
        )
    }

    private class FakeTokenStorage(
        var tokenSet: TokenSet? = null
    ) : TokenStorage {
        override suspend fun save(tokens: TokenSet) {
            tokenSet = tokens
        }

        override suspend fun read(): TokenSet? = tokenSet

        override suspend fun clear() {
            tokenSet = null
        }
    }

    private class FakeTokenRefresher(
        private val refreshed: TokenSet
    ) : TokenRefresher {
        var called = false
            private set

        override suspend fun refresh(request: TokenRefreshRequest): TokenSet {
            called = true
            return refreshed
        }
    }
}
