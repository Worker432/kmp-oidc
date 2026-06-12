package io.github.zm.auth_core.browser

import io.github.zm.auth_core.platform.PlatformDependencies

internal expect object PlatformBrowserLauncherFactory {
    fun create(
        dependencies: PlatformDependencies
    ): BrowserLauncher

}