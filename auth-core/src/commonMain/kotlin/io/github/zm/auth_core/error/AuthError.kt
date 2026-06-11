package io.github.zm.auth_core.error

sealed interface AuthError {

    data object UserCancelled : AuthError
    data object StateMismatch : AuthError
    data object MissingAuthSession : AuthError
    data object DiscoveryInvalid : AuthError
    data object DiscoveryLoadFailed : AuthError
    data object TokenExchangeFailed : AuthError
    data object RefreshFailed : AuthError
    data class Unknown(val message: String? = null) : AuthError
    data object BrowserLaunchFailed : AuthError
    data object RedirectInvalid : AuthError
    data object TokenStorageFailed : AuthError
}