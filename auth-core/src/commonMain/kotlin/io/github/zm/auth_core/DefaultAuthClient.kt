package io.github.zm.auth_core

import io.github.zm.auth_core.browser.BrowserLauncher
import io.github.zm.auth_core.config.AuthConfig
import io.github.zm.auth_core.discovery.DiscoveryManager
import io.github.zm.auth_core.error.AuthError
import io.github.zm.auth_core.pkce.PkceGenerator
import io.github.zm.auth_core.redirect.RedirectHandler
import io.github.zm.auth_core.request.AuthorizationUrlBuilder
import io.github.zm.auth_core.result.AuthResult
import io.github.zm.auth_core.result.TokenResult
import io.github.zm.auth_core.session.SessionManager
import io.github.zm.auth_core.state.StateGenerator
import io.github.zm.auth_core.storage.TokenStorage
import io.github.zm.auth_core.token.TokenExchangeRequest
import io.github.zm.auth_core.token.TokenExchanger
import io.github.zm.auth_core.token.TokenManager

internal class DefaultAuthClient(
    private val config: AuthConfig,
    private val discoveryManager: DiscoveryManager,
    private val sessionManager: SessionManager,
    private val pkceGenerator: PkceGenerator,
    private val stateGenerator: StateGenerator,
    private val authorizationUrlBuilder: AuthorizationUrlBuilder,
    private val browserLauncher: BrowserLauncher,
    private val redirectHandler: RedirectHandler,
    private val tokenExchanger: TokenExchanger,
    private val tokenStorage: TokenStorage,
    private val tokenManager: TokenManager
): AuthClient {

    override suspend fun login(): AuthResult {
        return try {
            val discovery = discoveryManager.getDiscovery()

            val state = stateGenerator.generateState()

            val codeVerifier = pkceGenerator.generateCodeVerifier()

            sessionManager.createSession(
                state = state,
                codeVerifier = codeVerifier
            )

            val codeChallenge = pkceGenerator.generateCodeChallenge(codeVerifier)

            val authorizationUrl = authorizationUrlBuilder.buildAuthorizationUrl(
                config = config,
                discovery = discovery,
                state = state,
                codeChallenge = codeChallenge
            )
            browserLauncher.open(authorizationUrl)

            AuthResult.Started

        } catch (_: Throwable) {
            AuthResult.Failure(AuthError.Unknown())
        }
    }

    override suspend fun handleRedirect(url: String): AuthResult {
        return try {
            val params = redirectHandler.parse(url)

            val session = sessionManager.getCurrentSession()
                ?: return AuthResult.Failure(
                    AuthError.MissingAuthSession
                )

            val isStateValid = sessionManager.validateState(params.state)

            if (!isStateValid) {
                sessionManager.clear()
                return AuthResult.Failure(
                    AuthError.StateMismatch
                )
            }

            val discovery = discoveryManager.getDiscovery()

            val tokenSet = tokenExchanger.exchangeCode(

                TokenExchangeRequest(
                    tokenEndpoint = discovery.tokenEndpoint,
                    clientId = config.clientId,
                    redirectUri = config.redirectUri,
                    code = params.code,
                    codeVerifier = session.codeVerifier
                )
            )

            tokenStorage.save(tokenSet)
            sessionManager.clear()
            AuthResult.Success

        } catch (_: Throwable) {
            AuthResult.Failure(AuthError.Unknown())
        }
    }

    override suspend fun getValidAccessToken(): TokenResult {
        return tokenManager.getValidAccessToken()
    }

    override suspend fun logout() {
        tokenStorage.clear()
        sessionManager.clear()
    }
}