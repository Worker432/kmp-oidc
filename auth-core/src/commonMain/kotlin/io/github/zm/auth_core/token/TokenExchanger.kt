package io.github.zm.auth_core.token

interface TokenExchanger {
    suspend fun exchangeCode(
        request: TokenExchangeRequest
    ): TokenSet
}