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
        val queryParams = mapOf(
            "response_type" to "code",
            "client_id" to config.clientId,
            "redirect_uri" to config.redirectUri,
            "scope" to config.scopes.joinToString(" "),
            "state" to state,
            "code_challenge" to codeChallenge,
            "code_challenge_method" to "S256"
        )

        val query = queryParams
            .map { (key, value) ->
                "${key.urlEncode()}=${value.urlEncode()}"
            }
            .joinToString("&")

        return "${discovery.authorizationEndpoint}?$query"
    }
}