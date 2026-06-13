package io.github.zm.auth_core.browser

internal sealed interface BrowserLaunchResult {
    data object Opened : BrowserLaunchResult
    data object Cancelled : BrowserLaunchResult

    data class RedirectReceived(
        val url: String
    ) : BrowserLaunchResult
}