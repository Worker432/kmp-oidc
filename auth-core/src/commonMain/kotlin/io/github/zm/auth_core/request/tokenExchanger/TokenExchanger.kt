package io.github.zm.auth_core.request.tokenExchanger

import io.github.zm.auth_core.token.TokenSet

interface TokenExchanger {
    suspend fun exchangeCode(
        request: TokenExchangeRequest
    ): TokenSet
}