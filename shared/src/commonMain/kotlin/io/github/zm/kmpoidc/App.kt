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
import io.github.zm.auth_core.error.AuthError
import io.github.zm.auth_core.platform.PlatformDependencies
import io.github.zm.auth_core.request.logout.LogoutMode
import io.github.zm.auth_core.result.AuthResult
import io.github.zm.auth_core.result.TokenResult
import kotlinx.coroutines.launch

private const val SampleRedirectUri = "io.github.zm.kmpoidc://callback"
private const val SampleLogoutRedirectUri = "io.github.zm.kmpoidc://logout"

private sealed interface SampleUiStatus {
    data object Idle : SampleUiStatus
    data object AuthorizationInProgress : SampleUiStatus
    data object Authorized : SampleUiStatus
    data object AccessDenied : SampleUiStatus
    data object LoggedOut : SampleUiStatus
    data object LogoutCompletedAtProvider : SampleUiStatus
    data object NeedsLogin : SampleUiStatus
    data class AccessTokenReceived(val preview: String) : SampleUiStatus
    data class Failure(val message: String) : SampleUiStatus
    data class InvalidRedirect(val url: String) : SampleUiStatus
}

@Composable
fun App(
    dependencies: PlatformDependencies,
    redirectUrl: String? = null
) {
    val authClient = remember {
        AuthClientFactory.create(
            config = AuthConfig(
                issuer = sampleIssuerUrl(),
                clientId = "kmp-oidc-sdk",
                redirectUri = SampleRedirectUri,
                logoutRedirectUri = SampleLogoutRedirectUri,
                scopes = listOf("openid", "profile", "email", "offline_access"),
                storageName = "sample_auth_tokens",
            ),
            dependencies = dependencies
        )
    }

    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<SampleUiStatus>(SampleUiStatus.Idle) }

    LaunchedEffect(redirectUrl) {
        val url = redirectUrl ?: return@LaunchedEffect

        when {
            url.startsWith(SampleRedirectUri) -> {
                status = authClient.handleRedirect(url).toUiStatus()
            }

            url.startsWith(SampleLogoutRedirectUri) -> {
                status = SampleUiStatus.LogoutCompletedAtProvider
            }

            else -> {
                status = SampleUiStatus.InvalidRedirect(url)
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
                        status = SampleUiStatus.AuthorizationInProgress
                        status = authClient.login().toUiStatus()
                    }
                }
            ) {
                Text("Login")
            }

            Button(
                onClick = {
                    scope.launch {
                        status = authClient.getValidAccessToken().toUiStatus()
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
                        ).toLogoutUiStatus(providerLogout = false)
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
                        ).toLogoutUiStatus(providerLogout = true)
                    }
                }
            ) {
                Text("Logout provider")
            }

            Text("Status: ${status.asText()}")
        }
    }
}

private fun AuthResult.toUiStatus(): SampleUiStatus {
    return when (this) {
        AuthResult.Started -> SampleUiStatus.AuthorizationInProgress
        AuthResult.Success -> SampleUiStatus.Authorized
        AuthResult.Cancelled -> SampleUiStatus.Failure("User cancelled authorization")
        AuthResult.AccessDenied -> SampleUiStatus.AccessDenied
        is AuthResult.Failure -> SampleUiStatus.Failure(error.toReadableText())
    }
}

private fun TokenResult.toUiStatus(): SampleUiStatus {
    return when (this) {
        is TokenResult.Success -> SampleUiStatus.AccessTokenReceived(
            preview = accessToken.take(16)
        )
        TokenResult.NeedLogin -> SampleUiStatus.NeedsLogin
        is TokenResult.Failure -> SampleUiStatus.Failure(error.toReadableText())
    }
}

private fun AuthResult.toLogoutUiStatus(providerLogout: Boolean): SampleUiStatus {
    return when (this) {
        AuthResult.Success -> {
            if (providerLogout) {
                SampleUiStatus.LogoutCompletedAtProvider
            } else {
                SampleUiStatus.LoggedOut
            }
        }
        AuthResult.Cancelled -> SampleUiStatus.Failure("User cancelled logout")
        AuthResult.Started -> SampleUiStatus.AuthorizationInProgress
        AuthResult.AccessDenied -> SampleUiStatus.AccessDenied
        is AuthResult.Failure -> SampleUiStatus.Failure(error.toReadableText())
    }
}

private fun AuthError.toReadableText(): String {
    return when (this) {
        AuthError.UserCancelled -> "User cancelled authorization"
        AuthError.StateMismatch -> "State mismatch"
        AuthError.MissingAuthSession -> "Missing auth session"
        AuthError.DiscoveryInvalid -> "Discovery response is invalid"
        AuthError.DiscoveryLoadFailed -> "Failed to load discovery document"
        AuthError.TokenExchangeFailed -> "Failed to exchange authorization code"
        AuthError.RefreshFailed -> "Failed to refresh access token"
        is AuthError.AuthorizationFailed -> {
            description?.let { "Authorization failed: $error ($it)" }
                ?: "Authorization failed: $error"
        }
        is AuthError.Unknown -> message ?: "Unknown error"
        AuthError.BrowserLaunchFailed -> "Failed to launch browser"
        AuthError.RedirectInvalid -> "Invalid redirect"
        AuthError.TokenStorageFailed -> "Token storage failure"
        AuthError.LogoutNotSupported -> "Provider logout is not supported"
        AuthError.LogoutFailed -> "Logout failed"
    }
}

private fun SampleUiStatus.asText(): String {
    return when (this) {
        SampleUiStatus.Idle -> "Idle"
        SampleUiStatus.AuthorizationInProgress -> "Authorization in progress"
        SampleUiStatus.Authorized -> "Authorized"
        SampleUiStatus.AccessDenied -> "Access denied"
        SampleUiStatus.LoggedOut -> "Logged out"
        SampleUiStatus.LogoutCompletedAtProvider -> "Logged out from provider"
        SampleUiStatus.NeedsLogin -> "Login required"
        is SampleUiStatus.AccessTokenReceived -> "Access token received: $preview..."
        is SampleUiStatus.Failure -> message
        is SampleUiStatus.InvalidRedirect -> "Invalid redirect: $url"
    }
}
