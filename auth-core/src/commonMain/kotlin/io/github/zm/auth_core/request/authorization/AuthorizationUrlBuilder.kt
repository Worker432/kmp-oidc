package io.github.zm.auth_core.request.authorization

import io.github.zm.auth_core.config.AuthConfig
import io.github.zm.auth_core.discovery.DiscoveryDocument

interface AuthorizationUrlBuilder {

    fun buildAuthorizationUrl(
        config: AuthConfig,
        discovery: DiscoveryDocument,
        state: String,
        codeChallenge: String
    ): String
}