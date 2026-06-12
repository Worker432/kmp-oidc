package io.github.zm.auth_core.request.authorization

import io.github.zm.auth_core.config.AuthConfig
import io.github.zm.auth_core.discovery.DiscoveryDocument
import io.github.zm.auth_core.request.util.urlEncode

internal class DefaultAuthorizationUrlBuilder : AuthorizationUrlBuilder {

    override fun buildAuthorizationUrl(
        config: AuthConfig,
        discovery: DiscoveryDocument,
        state: String,
        codeChallenge: String
    ): String {
        val queryParams = buildMap {
            put("response_type", "code")
            put("client_id", config.clientId)
            put("redirect_uri", config.redirectUri)
            put("scope", config.scopes.joinToString(" "))
            put("state", state)
            put("code_challenge", codeChallenge)
            put("code_challenge_method", "S256")

            putAll(config.customization.authorizationParameters)
        }

        val query = queryParams
            .map { (key, value) ->
                "${key.urlEncode()}=${value.urlEncode()}"
            }
            .joinToString("&")

        return "${discovery.authorizationEndpoint}?$query"
    }
}