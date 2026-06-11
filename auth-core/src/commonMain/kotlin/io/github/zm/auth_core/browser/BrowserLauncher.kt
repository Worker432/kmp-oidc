package io.github.zm.auth_core.browser

interface BrowserLauncher {
    suspend fun open(
        url: String
    )
}