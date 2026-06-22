package io.github.zm.auth_core

import io.github.zm.auth_core.browser.PlatformBrowserLauncherFactory
import io.github.zm.auth_core.config.AuthConfig
import io.github.zm.auth_core.discovery.KtorDiscoveryManager
import io.github.zm.auth_core.network.HttpClientFactory
import io.github.zm.auth_core.pkce.DefaultPkceGenerator
import io.github.zm.auth_core.platform.PlatformDependencies
import io.github.zm.auth_core.redirect.DefaultRedirectHandler
import io.github.zm.auth_core.request.authorization.DefaultAuthorizationUrlBuilder
import io.github.zm.auth_core.session.DefaultSessionManager
import io.github.zm.auth_core.session.PlatformAuthSessionStorageFactory
import io.github.zm.auth_core.state.DefaultStateGenerator
import io.github.zm.auth_core.storage.PlatformTokenStorageFactory
import io.github.zm.auth_core.token.DefaultTokenManager
import io.github.zm.auth_core.request.tokenExchanger.KtorTokenExchanger
import io.github.zm.auth_core.request.tokenRefresher.KtorTokenRefresher
import io.github.zm.auth_core.request.logout.DefaultLogoutUrlBuilder

object AuthClientFactory {
    fun create(
        config: AuthConfig,
        dependencies: PlatformDependencies
    ): AuthClient {
        val httpClient = HttpClientFactory.create()

        val discoveryManager = KtorDiscoveryManager(
            config = config,
            httpClient = httpClient
        )

        val tokenStorage = PlatformTokenStorageFactory.create(
            dependencies = dependencies,
            storageName = config.storageName
        )

        val tokenRefresher = KtorTokenRefresher(
            httpClient = httpClient
        )

        val authSessionStorage = PlatformAuthSessionStorageFactory.create(
            dependencies = dependencies,
            storageName = "${config.storageName}_auth_session"
        )

        val tokenManager = DefaultTokenManager(
            config = config,
            discoveryManager = discoveryManager,
            tokenStorage = tokenStorage,
            tokenRefresher = tokenRefresher
        )

        return DefaultAuthClient(
            config = config,
            discoveryManager = discoveryManager,
            sessionManager = DefaultSessionManager(authSessionStorage),
            stateGenerator = DefaultStateGenerator(),
            pkceGenerator = DefaultPkceGenerator(),
            authorizationUrlBuilder = DefaultAuthorizationUrlBuilder(),
            logoutUrlBuilder = DefaultLogoutUrlBuilder(),
            browserLauncher = PlatformBrowserLauncherFactory.create(dependencies),
            redirectHandler = DefaultRedirectHandler(),
            tokenExchanger = KtorTokenExchanger(httpClient),
            tokenStorage = tokenStorage,
            tokenManager = tokenManager
        )
    }
}
