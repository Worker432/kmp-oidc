package io.github.zm.kmpoidc

import androidx.compose.ui.window.ComposeUIViewController
import io.github.zm.auth_core.platform.PlatformDependencies

fun MainViewController() = ComposeUIViewController {
    App(
        dependencies = PlatformDependencies(),
    )
}
