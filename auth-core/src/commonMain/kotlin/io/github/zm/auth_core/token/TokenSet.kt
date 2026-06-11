package io.github.zm.auth_core.token

import kotlin.time.Instant

data class TokenSet(
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String?,
    val tokenType: String = "Bearer",
    val expiresAt: Instant
)