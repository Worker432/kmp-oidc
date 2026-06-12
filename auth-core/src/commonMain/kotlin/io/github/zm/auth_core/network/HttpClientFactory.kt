package io.github.zm.auth_core.network

import io.ktor.client.HttpClient

internal expect object HttpClientFactory {
    fun create(): HttpClient
}