package io.github.zm.auth_core.session

import kotlinx.serialization.Serializable

@Serializable
data class AuthSession(
    val state: String,
    val codeVerifier: String,
)
