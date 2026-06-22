package io.github.zm.auth_core.session

import io.github.zm.auth_core.platform.PlatformDependencies

internal actual object PlatformAuthSessionStorageFactory {
    actual fun create(
        dependencies: PlatformDependencies,
        storageName: String
    ): AuthSessionStorage {
        return AndroidEncryptedAuthSessionStorage(
            context = dependencies.context,
            storageName = storageName
        )
    }
}
