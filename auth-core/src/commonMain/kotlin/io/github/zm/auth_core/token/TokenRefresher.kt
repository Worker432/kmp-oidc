package io.github.zm.auth_core.token

interface TokenRefresher {
    suspend fun refresh(
        request: TokenRefreshRequest
    ): TokenSet
}