package io.github.zm.auth_core.request.TokenRefresher

import io.github.zm.auth_core.token.TokenSet

interface TokenRefresher {
    suspend fun refresh(
        request: TokenRefreshRequest
    ): TokenSet
}