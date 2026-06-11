package io.github.zm.auth_core.token

import kotlin.time.Clock
import io.github.zm.auth_core.config.AuthConfig
import io.github.zm.auth_core.discovery.DiscoveryManager
import io.github.zm.auth_core.result.TokenResult
import io.github.zm.auth_core.storage.TokenStorage

internal class DefaultTokenManager(
    private val config: AuthConfig,
    private val discoveryManager: DiscoveryManager,
    private val tokenStorage: TokenStorage,
    private val tokenRefresher: TokenRefresher
) : TokenManager {

    override suspend fun getValidAccessToken(): TokenResult {
        val tokens = tokenStorage.read()
            ?: return TokenResult.NeedLogin

        val now = Clock.System.now()

        if (tokens.expiresAt > now) {
            return TokenResult.Success(tokens.accessToken)
        }

        val refreshToken = tokens.refreshToken
            ?: run {
                tokenStorage.clear()
                return TokenResult.NeedLogin
            }

        val discovery = discoveryManager.getDiscovery()

        val newTokens = tokenRefresher.refresh(
            TokenRefreshRequest(
                tokenEndpoint = discovery.tokenEndpoint,
                clientId = config.clientId,
                refreshToken = refreshToken
            )
        )

        tokenStorage.save(newTokens)

        return TokenResult.Success(newTokens.accessToken)
    }
}