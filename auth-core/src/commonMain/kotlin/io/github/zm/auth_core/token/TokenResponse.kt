package io.github.zm.auth_core.token

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("id_token")
    val idToken: String? = null,
    @SerialName("token_type")
    val tokenType: String = "Bearer",
    @SerialName("expires_in")
    val expiresIn: Long
)