package io.github.zm.auth_core.browser

import android.app.Activity

import android.net.Uri

import androidx.browser.customtabs.CustomTabsIntent

internal class AndroidBrowserLauncher(
    private val activity: Activity
) : BrowserLauncher {
    
    override suspend fun open(
        url: String,
        callbackScheme: String?
    ): BrowserLaunchResult {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()

        customTabsIntent.launchUrl(
            activity,
            Uri.parse(url)
        )

        return BrowserLaunchResult.Opened
    }
}