package io.github.zm.auth_core.request.TokenRefresher

data class TokenRefreshRequest(
    val tokenEndpoint: String,
    val clientId: String,
    val refreshToken: String
)