package io.github.zm.auth_core.storage

import io.github.zm.auth_core.token.TokenSet

interface TokenStorage {
    suspend fun save(tokens: TokenSet)
    suspend fun read(): TokenSet?
    suspend fun clear()
}