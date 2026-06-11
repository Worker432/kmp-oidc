package io.github.zm.auth_core.state

import io.github.zm.auth_core.crypto.CryptoProvider

class DefaultStateGenerator: StateGenerator {
    override fun generateState(): String {
        with(CryptoProvider) {
            return base64UrlEncodeWithoutPadding(
                secureRandomBytes(STATE_SIZE_BYTES)
            )
        }
    }

    private companion object {
        const val STATE_SIZE_BYTES = 32
    }
}