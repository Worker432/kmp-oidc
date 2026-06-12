package io.github.zm.auth_core.storage

import io.github.zm.auth_core.platform.PlatformDependencies

internal expect object PlatformTokenStorageFactory {
    fun create(
        dependencies: PlatformDependencies,
        storageName: String
    ): TokenStorage
}