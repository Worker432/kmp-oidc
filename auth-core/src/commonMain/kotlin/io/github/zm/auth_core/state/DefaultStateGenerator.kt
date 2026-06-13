package io.github.zm.auth_core.state

import io.github.zm.auth_core.crypto.CryptoProvider
import io.github.zm.auth_core.crypto.base64UrlEncodeWithoutPadding

class DefaultStateGenerator : StateGenerator {

    override fun generateState(): String {
        return CryptoProvider
            .secureRandomBytes(STATE_SIZE_BYTES)
            .base64UrlEncodeWithoutPadding()
    }

    private companion object {
        const val STATE_SIZE_BYTES = 32
    }
}