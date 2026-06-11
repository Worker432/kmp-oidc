package io.github.zm.auth_core.result

import io.github.zm.auth_core.error.AuthError


sealed interface TokenResult {
    data class Success(val accessToken: String) : TokenResult
    data object NeedLogin : TokenResult
    data class Failure(val error: AuthError) : TokenResult
}