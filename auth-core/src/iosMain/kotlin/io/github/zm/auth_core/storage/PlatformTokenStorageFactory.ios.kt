package io.github.zm.auth_core.storage

import io.github.zm.auth_core.platform.PlatformDependencies

internal actual object PlatformTokenStorageFactory {
    actual fun create(
        dependencies: PlatformDependencies,
        storageName: String
    ): TokenStorage {
        TODO("Not yet implemented")
    }
}