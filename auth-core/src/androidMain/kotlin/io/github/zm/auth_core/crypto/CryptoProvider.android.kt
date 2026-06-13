package io.github.zm.auth_core.crypto

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

internal actual object CryptoProvider {
    private val secureRandom = SecureRandom()

    actual fun secureRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        secureRandom.nextBytes(bytes)
        return bytes

    }

    actual fun sha256(bytes: ByteArray): ByteArray {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(bytes)
    }

    actual fun base64Encode(bytes: ByteArray): String {
        return Base64.getEncoder()
            .encodeToString(bytes)
    }
}