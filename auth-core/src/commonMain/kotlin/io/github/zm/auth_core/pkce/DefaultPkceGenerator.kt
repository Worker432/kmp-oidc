package io.github.zm.auth_core.pkce

import io.github.zm.auth_core.crypto.CryptoProvider

internal class DefaultPkceGenerator: PkceGenerator {
    override fun generateCodeVerifier(): String {
        with(CryptoProvider) {
            return base64UrlEncodeWithoutPadding(
                secureRandomBytes(
                    CODE_VERIFIER_SIZE_BYTES)
            )
        }
    }

    override fun generateCodeChallenge(codeVerifier: String): String {
        with(CryptoProvider) {
            return base64UrlEncodeWithoutPadding(
                sha256(
                    codeVerifier.encodeToByteArray()
                )
            )
        }
    }

    private companion object {
        const val CODE_VERIFIER_SIZE_BYTES = 32
    }
}