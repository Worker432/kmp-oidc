package io.github.zm.auth_core.request.tokenRefresher

data class TokenRefreshRequest(
    val tokenEndpoint: String,
    val clientId: String,
    val refreshToken: String,
    val extraParams: Map<String, String> = emptyMap()
)