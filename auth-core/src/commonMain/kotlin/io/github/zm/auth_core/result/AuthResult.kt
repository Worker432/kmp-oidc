package io.github.zm.auth_core.result

import io.github.zm.auth_core.error.AuthError


sealed interface AuthResult {
    data object Started : AuthResult
    data object Success : AuthResult
    data object Cancelled : AuthResult
    data object AccessDenied : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}