package io.github.zm.auth_core

import io.github.zm.auth_core.browser.BrowserLaunchResult
import io.github.zm.auth_core.browser.BrowserLauncher
import io.github.zm.auth_core.config.AuthConfig
import io.github.zm.auth_core.discovery.DiscoveryDocument
import io.github.zm.auth_core.discovery.DiscoveryManager
import io.github.zm.auth_core.error.AuthError
import io.github.zm.auth_core.pkce.PkceGenerator
import io.github.zm.auth_core.redirect.RedirectHandler
import io.github.zm.auth_core.redirect.RedirectParams
import io.github.zm.auth_core.request.authorization.AuthorizationUrlBuilder
import io.github.zm.auth_core.request.logout.LogoutMode
import io.github.zm.auth_core.request.logout.LogoutUrlBuilder
import io.github.zm.auth_core.request.tokenExchanger.TokenExchangeRequest
import io.github.zm.auth_core.request.tokenExchanger.TokenExchanger
import io.github.zm.auth_core.result.AuthResult
import io.github.zm.auth_core.result.TokenResult
import io.github.zm.auth_core.session.AuthSession
import io.github.zm.auth_core.session.SessionManager
import io.github.zm.auth_core.state.StateGenerator
import io.github.zm.auth_core.storage.TokenStorage
import io.github.zm.auth_core.token.TokenManager
import io.github.zm.auth_core.token.TokenSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class DefaultAuthClientTest {
    private val config = AuthConfig(
        clientId = "client-id",
        issuer = "https://issuer.example.com",
        redirectUri = "myapp://callback",
        logoutRedirectUri = "myapp://logout"
    )

    @Test
    fun `login returns Started when browser opens without immediate redirect`() = runSuspendTest {
        val client = createClient(
            browserLauncher = object : BrowserLauncher {
                override suspend fun open(url: String, callbackScheme: String?): BrowserLaunchResult {
                    return BrowserLaunchResult.Opened
                }
            }
        )

        val result = client.login()

        assertEquals(AuthResult.Started, result)
    }

    @Test
    fun `handleRedirect returns AccessDenied for provider denial`() = runSuspendTest {
        val client = createClient(
            redirectHandler = object : RedirectHandler {
                override fun parse(url: String): RedirectParams {
                    return RedirectParams.Error(
                        error = "access_denied",
                        state = "state-123"
                    )
                }
            }
        )

        val result = client.handleRedirect("myapp://callback?error=access_denied")

        assertEquals(AuthResult.AccessDenied, result)
    }

    @Test
    fun `handleRedirect fails on state mismatch`() = runSuspendTest {
        val sessionManager = FakeSessionManager(
            current = AuthSession(
                state = "expected-state",
                codeVerifier = "verifier"
            )
        )
        val client = createClient(
            sessionManager = sessionManager,
            redirectHandler = object : RedirectHandler {
                override fun parse(url: String): RedirectParams {
                    return RedirectParams.AuthorizationCode(
                        code = "code-123",
                        state = "other-state"
                    )
                }
            }
        )

        val result = client.handleRedirect("myapp://callback?code=code-123&state=other-state")

        val failure = assertIs<AuthResult.Failure>(result)
        assertEquals(AuthError.StateMismatch, failure.error)
    }

    @Test
    fun `handleRedirect exchanges code and stores tokens on success`() = runSuspendTest {
        val tokenStorage = FakeTokenStorage()
        val client = createClient(
            tokenStorage = tokenStorage,
            redirectHandler = object : RedirectHandler {
                override fun parse(url: String): RedirectParams {
                    return RedirectParams.AuthorizationCode(
                        code = "code-123",
                        state = "state-123"
                    )
                }
            }
        )

        val result = client.handleRedirect("myapp://callback?code=code-123&state=state-123")

        assertEquals(AuthResult.Success, result)
        assertEquals("stored-access-token", tokenStorage.saved?.accessToken)
    }

    private fun createClient(
        browserLauncher: BrowserLauncher = object : BrowserLauncher {
            override suspend fun open(url: String, callbackScheme: String?): BrowserLaunchResult {
                return BrowserLaunchResult.Opened
            }
        },
        sessionManager: SessionManager = FakeSessionManager(
            current = AuthSession(
                state = "state-123",
                codeVerifier = "code-verifier"
            )
        ),
        redirectHandler: RedirectHandler = object : RedirectHandler {
            override fun parse(url: String): RedirectParams {
                return RedirectParams.AuthorizationCode(
                    code = "code-123",
                    state = "state-123"
                )
            }
        },
        tokenStorage: FakeTokenStorage = FakeTokenStorage()
    ): DefaultAuthClient {
        return DefaultAuthClient(
            config = config,
            discoveryManager = object : DiscoveryManager {
                override suspend fun getDiscovery(): DiscoveryDocument {
                    return DiscoveryDocument(
                        issuer = config.issuer,
                        authorizationEndpoint = "https://issuer.example.com/auth",
                        tokenEndpoint = "https://issuer.example.com/token"
                    )
                }
            },
            sessionManager = sessionManager,
            pkceGenerator = object : PkceGenerator {
                override fun generateCodeVerifier(): String = "code-verifier"
                override fun generateCodeChallenge(codeVerifier: String): String = "code-challenge"
            },
            stateGenerator = object : StateGenerator {
                override fun generateState(): String = "state-123"
            },
            authorizationUrlBuilder = object : AuthorizationUrlBuilder {
                override fun buildAuthorizationUrl(
                    config: AuthConfig,
                    discovery: DiscoveryDocument,
                    state: String,
                    codeChallenge: String
                ): String = "https://issuer.example.com/auth"
            },
            logoutUrlBuilder = object : LogoutUrlBuilder {
                override fun buildLogoutUrl(
                    config: AuthConfig,
                    discovery: DiscoveryDocument
                ): String = "https://issuer.example.com/logout"
            },
            browserLauncher = browserLauncher,
            redirectHandler = redirectHandler,
            tokenExchanger = object : TokenExchanger {
                override suspend fun exchangeCode(request: TokenExchangeRequest): TokenSet {
                    return TokenSet(
                        accessToken = "stored-access-token",
                        refreshToken = "refresh-token",
                        idToken = null,
                        expiresAt = Clock.System.now() + 1.hours
                    )
                }
            },
            tokenStorage = tokenStorage,
            tokenManager = object : TokenManager {
                override suspend fun getValidAccessToken(): TokenResult {
                    return TokenResult.NeedLogin
                }
            }
        )
    }

    private class FakeSessionManager(
        var current: AuthSession? = null
    ) : SessionManager {
        override suspend fun createSession(state: String, codeVerifier: String): AuthSession {
            val session = AuthSession(state = state, codeVerifier = codeVerifier)
            current = session
            return session
        }

        override suspend fun getCurrentSession(): AuthSession? = current

        override suspend fun validateState(receivedState: String): Boolean {
            return current?.state == receivedState
        }

        override suspend fun clear() {
            current = null
        }
    }

    private class FakeTokenStorage : TokenStorage {
        var saved: TokenSet? = null

        override suspend fun save(tokens: TokenSet) {
            saved = tokens
        }

        override suspend fun read(): TokenSet? = saved

        override suspend fun clear() {
            saved = null
        }
    }
}
