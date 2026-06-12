package io.github.zm.auth_core.discovery

import io.github.zm.auth_core.config.AuthConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

internal class KtorDiscoveryManager(
    private val config: AuthConfig,
    private val httpClient: HttpClient
): DiscoveryManager {
    private var cachedDiscovery: DiscoveryDocument? = null

    override suspend fun getDiscovery(): DiscoveryDocument {
        cachedDiscovery?.let {
            return it
        }

        val discoveryUrl = buildDiscoveryUrl(config.issuer)

        val discovery = httpClient
            .get(discoveryUrl)
            .body<DiscoveryDocument>()

        validateDiscovery(discovery)

        cachedDiscovery = discovery

        return discovery

    }

    private fun buildDiscoveryUrl(
        issuer: String
    ): String {
        return issuer.trimEnd('/') + "/.well-known/openid-configuration"
    }

    private fun validateDiscovery(
        discovery: DiscoveryDocument
    ) {
        if (discovery.issuer.trimEnd('/') != config.issuer.trimEnd('/')) {
            throw IllegalStateException("Invalid issuer")
        }
        if (discovery.authorizationEndpoint.isBlank()) {
            throw IllegalStateException("Missing authorization endpoint")
        }
        if (discovery.tokenEndpoint.isBlank()) {
            throw IllegalStateException("Missing token endpoint")
        }
    }
}