package io.github.zm.auth_core.discovery

interface DiscoveryManager {
    suspend fun getDiscovery(): DiscoveryDocument
}