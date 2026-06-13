package io.github.zm.auth_core.browser

internal interface BrowserLauncher {
    suspend fun open(
        url: String,
        callbackScheme: String? = null
    ): BrowserLaunchResult
}