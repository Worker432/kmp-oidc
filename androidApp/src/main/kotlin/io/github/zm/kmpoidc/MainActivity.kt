package io.github.zm.kmpoidc

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import io.github.zm.auth_core.platform.PlatformDependencies

class MainActivity : ComponentActivity() {
    private var redirectUrl by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(
                dependencies = PlatformDependencies(
                    context = applicationContext,
                    activity = this
                ),
                redirectUrl = redirectUrl
            )

        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        redirectUrl = intent.dataString

    }
}