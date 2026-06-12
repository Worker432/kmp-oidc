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

        val queryParams = buildMap {
            put("client_id", config.clientId)
            put("post_logout_redirect_uri", config.logoutRedirectUri)

            putAll(config.customization.logoutParameters)
        }

        val query = queryParams
            .map { (key, value) ->
                "${key.urlEncode()}=${value.urlEncode()}"
            }
            .joinToString("&")

        return "$endpoint?$query"
    }
}