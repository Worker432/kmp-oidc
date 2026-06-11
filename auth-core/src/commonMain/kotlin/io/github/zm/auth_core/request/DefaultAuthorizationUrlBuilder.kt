package io.github.zm.auth_core.request

import io.github.zm.auth_core.config.AuthConfig
import io.github.zm.auth_core.discovery.DiscoveryDocument

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

    private fun String.urlEncode(): String {
        return encodeToByteArray()
            .joinToString("") { byte ->
                val char = byte.toInt().toChar()

                when {
                    char.isLetterOrDigit() -> char.toString()
                    char in listOf('-', '_', '.', '~') -> char.toString()
                    else -> "%${byte.toUByte().toString(16).uppercase().padStart(2, '0')}"
                }
            }
    }
}