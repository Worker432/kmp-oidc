package io.github.zm.auth_core.request.tokenExchanger

data class TokenExchangeRequest(
    val tokenEndpoint: String,
    val clientId: String,
    val redirectUri: String,
    val code: String,
    val codeVerifier: String
)