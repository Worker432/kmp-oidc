package io.github.zm.auth_core.request.tokenExchanger

import io.github.zm.auth_core.token.TokenResponse
import io.github.zm.auth_core.token.TokenSet
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

internal class KtorTokenExchanger(
    private val httpClient: HttpClient
): TokenExchanger {
    override suspend fun exchangeCode(
        request: TokenExchangeRequest
    ): TokenSet {
        val response = httpClient.submitForm(
            url = request.tokenEndpoint,
            formParameters = Parameters.Companion.build {
                append("grant_type", "authorization_code")
                append("client_id", request.clientId)
                append("redirect_uri", request.redirectUri)
                append("code", request.code)
                append("code_verifier", request.codeVerifier)
            }
        ).body<TokenResponse>()

        return response.toTokenSet()
    }

    private fun TokenResponse.toTokenSet(): TokenSet {
        return TokenSet(
            accessToken = accessToken,
            refreshToken = refreshToken,
            idToken = idToken,
            tokenType = tokenType,
            expiresAt = Clock.System.now() + expiresIn.seconds
        )
    }
}