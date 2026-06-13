package io.github.zm.auth_core

import io.github.zm.auth_core.browser.BrowserLaunchResult
import io.github.zm.auth_core.browser.BrowserLauncher
import io.github.zm.auth_core.config.AuthConfig
import io.github.zm.auth_core.discovery.DiscoveryManager
import io.github.zm.auth_core.error.AuthError
import io.github.zm.auth_core.request.logout.LogoutMode
import io.github.zm.auth_core.pkce.PkceGenerator
import io.github.zm.auth_core.redirect.RedirectHandler
import io.github.zm.auth_core.request.authorization.AuthorizationUrlBuilder
import io.github.zm.auth_core.request.logout.LogoutUrlBuilder
import io.github.zm.auth_core.result.AuthResult
import io.github.zm.auth_core.result.TokenResult
import io.github.zm.auth_core.session.SessionManager
import io.github.zm.auth_core.state.StateGenerator
import io.github.zm.auth_core.storage.TokenStorage
import io.github.zm.auth_core.request.tokenExchanger.TokenExchangeRequest
import io.github.zm.auth_core.request.tokenExchanger.TokenExchanger
import io.github.zm.auth_core.request.util.uriScheme
import io.github.zm.auth_core.token.TokenManager

internal class DefaultAuthClient(
    private val config: AuthConfig,
    private val discoveryManager: DiscoveryManager,
    private val sessionManager: SessionManager,
    private val pkceGenerator: PkceGenerator,
    private val stateGenerator: StateGenerator,
    private val authorizationUrlBuilder: AuthorizationUrlBuilder,
    private val logoutUrlBuilder: LogoutUrlBuilder,
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
            when (
                val browserResult = browserLauncher.open(
                    url = authorizationUrl,
                    callbackScheme = config.redirectUri.uriScheme()
                )
            ) {
                BrowserLaunchResult.Opened -> {
                    AuthResult.Started
                }

                is BrowserLaunchResult.RedirectReceived -> {
                    handleRedirect(browserResult.url)
                }

                BrowserLaunchResult.Cancelled -> {
                    AuthResult.Failure(AuthError.UserCancelled)
                }
            }

        } catch (e: Throwable) {
            e.printStackTrace()
            AuthResult.Failure(AuthError.Unknown(e.message))
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
                    codeVerifier = session.codeVerifier,
                    extraParams = config.customization.tokenParameters
                )
            )

            tokenStorage.save(tokenSet)
            sessionManager.clear()
            AuthResult.Success

        } catch (e: Throwable) {
            e.printStackTrace()
            AuthResult.Failure(AuthError.Unknown(e.message))
        }
    }

    override suspend fun getValidAccessToken(): TokenResult {
        return tokenManager.getValidAccessToken()
    }

    override suspend fun logout(
        mode: LogoutMode
    ): AuthResult {
        tokenStorage.clear()
        sessionManager.clear()

        if (mode == LogoutMode.LOCAL_ONLY) {
            return AuthResult.Success
        }

        return try {
            val discovery = discoveryManager.getDiscovery()

            val logoutUrl = logoutUrlBuilder.buildLogoutUrl(
                config = config,
                discovery = discovery
            )

            when (
                browserLauncher.open(
                    url = logoutUrl,
                    callbackScheme = config.logoutRedirectUri.uriScheme()
                )
            ) {
                BrowserLaunchResult.Opened -> {
                    AuthResult.Success
                }
                is BrowserLaunchResult.RedirectReceived -> {
                    AuthResult.Success
                }
                BrowserLaunchResult.Cancelled -> {
                    AuthResult.Failure(AuthError.UserCancelled)
                }
            }
        } catch (_: IllegalStateException) {
            AuthResult.Failure(AuthError.LogoutNotSupported)
        } catch (_: Throwable) {
            AuthResult.Failure(AuthError.LogoutFailed)
        }
    }
}