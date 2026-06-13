package io.github.zm.auth_core.browser

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASPresentationAnchor
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume

internal class IosBrowserLauncher : BrowserLauncher {

    private var session: ASWebAuthenticationSession? = null

    private val presentationContextProvider =
        IosPresentationContextProvider()

    override suspend fun open(
        url: String,
        callbackScheme: String?
    ): BrowserLaunchResult {
        val nsUrl = NSURL.URLWithString(url)
            ?: return BrowserLaunchResult.Cancelled

        return suspendCancellableCoroutine { continuation ->
            var completed = false

            fun complete(result: BrowserLaunchResult) {
                if (completed) return

                completed = true
                session = null
                continuation.resume(result)
            }

            val webSession = ASWebAuthenticationSession(
                uRL = nsUrl,
                callbackURLScheme = callbackScheme
            ) { callbackUrl, _ ->
                val redirectUrl = callbackUrl?.absoluteString

                if (redirectUrl != null) {
                    complete(
                        BrowserLaunchResult.RedirectReceived(redirectUrl)
                    )
                } else {
                    complete(BrowserLaunchResult.Cancelled)
                }
            }

            webSession.presentationContextProvider =
                presentationContextProvider

            webSession.prefersEphemeralWebBrowserSession = false

            session = webSession

            val started = webSession.start()

            if (!started) {
                complete(BrowserLaunchResult.Cancelled)
            }

            continuation.invokeOnCancellation {
                webSession.cancel()
                session = null
            }
        }
    }
}

private class IosPresentationContextProvider :
    NSObject(),
    ASWebAuthenticationPresentationContextProvidingProtocol {

    override fun presentationAnchorForWebAuthenticationSession(
        session: ASWebAuthenticationSession
    ): ASPresentationAnchor {
        return UIApplication.sharedApplication.keyWindow
            ?: UIWindow()
    }
}