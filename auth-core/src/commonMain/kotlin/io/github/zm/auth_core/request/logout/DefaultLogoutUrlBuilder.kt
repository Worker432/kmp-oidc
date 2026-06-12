package io.github.zm.auth_core.request.logout

import io.github.zm.auth_core.config.AuthConfig
import io.github.zm.auth_core.discovery.DiscoveryDocument
import io.github.zm.auth_core.request.util.urlEncode

internal class DefaultLogoutUrlBuilder : LogoutUrlBuilder {

    override fun buildLogoutUrl(
        config: AuthConfig,
        discovery: DiscoveryDocument
    ): String {
        val endpoint = discovery.endSessionEndpoint
            ?: throw IllegalStateException("Logout is not supported by provider")

        val queryParams = mapOf(
            "client_id" to config.clientId,
            "post_logout_redirect_uri" to config.logoutRedirectUri
        )

        val query = queryParams
            .map { (key, value) ->
                "${key.urlEncode()}=${value.urlEncode()}"
            }
            .joinToString("&")

        return "$endpoint?$query"
    }
}