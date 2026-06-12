package io.github.zm.kmpoidc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.zm.auth_core.AuthClientFactory
import io.github.zm.auth_core.config.AuthConfig
import io.github.zm.auth_core.platform.PlatformDependencies
import io.github.zm.auth_core.request.logout.LogoutMode
import kotlinx.coroutines.launch

@Composable
fun App(
    dependencies: PlatformDependencies,
    redirectUrl: String? = null
) {
    val authClient = remember {
        AuthClientFactory.create(
            config = AuthConfig(
                issuer = "http://10.0.2.2:8080/realms/kmp",
                clientId = "kmp-oidc-sdk",
                redirectUri = "io.github.zm.kmpoidc://callback",
                logoutRedirectUri = "io.github.zm.kmpoidc://logout",
                scopes = listOf("openid", "profile", "email", "offline_access"),
                storageName = "sample_auth_tokens"
            ),
            dependencies = dependencies
        )
    }

    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Idle") }

    LaunchedEffect(redirectUrl) {
        val url = redirectUrl ?: return@LaunchedEffect

        when {
            url.startsWith("io.github.zm.kmpoidc://callback") -> {
                status = authClient.handleRedirect(url).toString()
            }

            url.startsWith("io.github.zm.kmpoidc://logout") -> {
                status = "Logged out from provider"
            }

            else -> {
                "Invalid redirect: $url"
            }
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("KMP OIDC Sample")

            Button(
                onClick = {
                    scope.launch {
                        status = authClient.login().toString()
                    }
                }
            ) {
                Text("Login")
            }

            Button(
                onClick = {
                    scope.launch {
                        status = authClient.getValidAccessToken().toString()
                    }
                }
            ) {
                Text("Get valid access token")
            }

            Button(
                onClick = {
                    scope.launch {
                        status = authClient.logout(
                            mode = LogoutMode.LOCAL_ONLY
                        ).toString()
                    }
                }
            ) {
                Text("Logout local")
            }

            Button(
                onClick = {
                    scope.launch {
                        status = authClient.logout(
                            mode = LogoutMode.LOCAL_AND_PROVIDER
                        ).toString()
                    }
                }
            ) {
                Text("Logout provider")
            }

            Text("Status: $status")
        }
    }
}