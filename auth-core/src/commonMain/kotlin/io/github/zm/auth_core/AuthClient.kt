package io.github.zm.auth_core

import io.github.zm.auth_core.result.AuthResult
import io.github.zm.auth_core.result.TokenResult

interface AuthClient {
    suspend fun login(): AuthResult

    suspend fun handleRedirect(
        url: String
    ): AuthResult

    suspend fun getValidAccessToken(): TokenResult

    suspend fun logout()
}