package io.github.zm.auth_core.discovery

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable

data class DiscoveryDocument(
    /**
     * Корневой идентификатор Identity Provider.
     */
    val issuer: String,

    /**
     * Endpoint для начала Authorization Code Flow.
     */
    @SerialName("authorization_endpoint")
    val authorizationEndpoint: String,

    /**
     * Endpoint для обмена authorization code или refresh token на токены.
     */
    @SerialName("token_endpoint")
    val tokenEndpoint: String,

    /**
     * URI для получения публичных ключей Identity Provider.
     * Используется для проверки подписи JWT.
     */
    @SerialName("jwks_uri")
    val jwksUri: String? = null,

    /**
     * Endpoint для получения информации о пользователе.
     */
    @SerialName("userinfo_endpoint")
    val userInfoEndpoint: String? = null,

    /**
     * Endpoint для завершения сессии у Identity Provider.
     */
    @SerialName("end_session_endpoint")
    val endSessionEndpoint: String? = null

)