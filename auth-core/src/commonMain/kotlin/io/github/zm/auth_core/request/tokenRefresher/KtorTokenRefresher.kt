package io.github.zm.auth_core.request.tokenRefresher

import io.github.zm.auth_core.request.util.toTokenSet
import io.github.zm.auth_core.token.TokenResponse
import io.github.zm.auth_core.token.TokenSet
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

internal class KtorTokenRefresher(
    private val httpClient: HttpClient
) : TokenRefresher {

    override suspend fun refresh(
        request: TokenRefreshRequest
    ): TokenSet {
        val response = httpClient.submitForm(
            url = request.tokenEndpoint,
            formParameters = Parameters.build {
                append("grant_type", "refresh_token")
                append("client_id", request.clientId)
                append("refresh_token", request.refreshToken)

                request.extraParams.forEach { (key, value) ->
                    append(key, value)
                }
            }
        ).body<TokenResponse>()

        return response.toTokenSet()
    }
}