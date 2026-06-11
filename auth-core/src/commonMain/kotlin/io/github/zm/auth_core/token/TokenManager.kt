package io.github.zm.auth_core.token

import io.github.zm.auth_core.result.TokenResult

interface TokenManager {
    suspend fun getValidAccessToken(): TokenResult
}