package io.github.zm.auth_core.request.authorization

import io.github.zm.auth_core.config.AuthConfig
import io.github.zm.auth_core.config.IdpCustomization
import io.github.zm.auth_core.discovery.DiscoveryDocument
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultAuthorizationUrlBuilderTest {
    private val builder = DefaultAuthorizationUrlBuilder()

    @Test
    fun `builds authorization url with encoded query parameters`() {
        val url = builder.buildAuthorizationUrl(
            config = AuthConfig(
                clientId = "sample client",
                issuer = "https://issuer.example.com/realms/demo",
                redirectUri = "myapp://callback",
                logoutRedirectUri = "myapp://logout",
                scopes = listOf("openid", "profile", "offline_access"),
                customization = IdpCustomization(
                    authorizationParameters = mapOf(
                        "prompt" to "login consent"
                    )
                )
            ),
            discovery = DiscoveryDocument(
                issuer = "https://issuer.example.com/realms/demo",
                authorizationEndpoint = "https://issuer.example.com/auth",
                tokenEndpoint = "https://issuer.example.com/token"
            ),
            state = "state-123",
            codeChallenge = "challenge value"
        )

        assertEquals(
            "https://issuer.example.com/auth" +
                "?response_type=code" +
                "&client_id=sample%20client" +
                "&redirect_uri=myapp%3A%2F%2Fcallback" +
                "&scope=openid%20profile%20offline_access" +
                "&state=state-123" +
                "&code_challenge=challenge%20value" +
                "&code_challenge_method=S256" +
                "&prompt=login%20consent",
            url
        )
    }
}
