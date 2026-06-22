package io.github.zm.auth_core.session

import io.github.zm.auth_core.platform.PlatformDependencies

internal expect object PlatformAuthSessionStorageFactory {
    fun create(
        dependencies: PlatformDependencies,
        storageName: String
    ): AuthSessionStorage
}
