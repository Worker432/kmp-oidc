package io.github.zm.auth_core.platform

import android.app.Activity
import android.content.Context

actual class PlatformDependencies(
    val context: Context,
    val activity: Activity
)