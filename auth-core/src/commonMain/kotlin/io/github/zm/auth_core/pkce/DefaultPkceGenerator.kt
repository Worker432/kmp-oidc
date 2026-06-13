package io.github.zm.auth_core.pkce

import io.github.zm.auth_core.crypto.CryptoProvider
import io.github.zm.auth_core.crypto.base64UrlEncodeWithoutPadding

internal class DefaultPkceGenerator : PkceGenerator {

    override fun generateCodeVerifier(): String {
        return CryptoProvider
            .secureRandomBytes(CODE_VERIFIER_SIZE_BYTES)
            .base64UrlEncodeWithoutPadding()
    }

    override fun generateCodeChallenge(
        codeVerifier: String
    ): String {
        return CryptoProvider
            .sha256(codeVerifier.encodeToByteArray())
            .base64UrlEncodeWithoutPadding()
    }

    private companion object {
        const val CODE_VERIFIER_SIZE_BYTES = 32
    }
}