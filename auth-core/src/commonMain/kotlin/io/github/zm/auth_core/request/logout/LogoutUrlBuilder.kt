package io.github.zm.auth_core.request.logout

import io.github.zm.auth_core.config.AuthConfig
import io.github.zm.auth_core.discovery.DiscoveryDocument

internal interface LogoutUrlBuilder {

    fun buildLogoutUrl(
        config: AuthConfig,
        discovery: DiscoveryDocument
    ): String
}