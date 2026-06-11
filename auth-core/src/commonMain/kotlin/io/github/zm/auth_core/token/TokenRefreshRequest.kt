package io.github.zm.auth_core.token

data class TokenRefreshRequest(
    val tokenEndpoint: String,
    val clientId: String,
    val refreshToken: String
)