package io.github.zm.auth_core.config

data class IdpCustomization(
    // Additional query parameters for /authorize request
    val authorizationParameters: Map<String, String> = emptyMap(),

    // Additional form parameters for /token and refresh requests
    val tokenParameters: Map<String, String> = emptyMap(),

    // Additional query parameters for end_session_endpoint
    val logoutParameters: Map<String, String> = emptyMap()
)