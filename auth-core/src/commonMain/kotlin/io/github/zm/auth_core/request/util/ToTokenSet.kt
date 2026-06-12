package io.github.zm.auth_core.request.util

import io.github.zm.auth_core.token.TokenResponse
import io.github.zm.auth_core.token.TokenSet
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

internal fun TokenResponse.toTokenSet(): TokenSet {
    return TokenSet(
        accessToken = accessToken,
        refreshToken = refreshToken,
        idToken = idToken,
        tokenType = tokenType,
        expiresAt = Clock.System.now() + expiresIn.seconds
    )
}