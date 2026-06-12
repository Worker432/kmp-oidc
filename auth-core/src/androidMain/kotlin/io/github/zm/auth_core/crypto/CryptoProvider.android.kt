package io.github.zm.auth_core.crypto

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

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

    actual fun base64UrlEncodeWithoutPadding(bytes: ByteArray): String {
        return Base64.encodeToString(
            bytes,
            Base64.URL_SAFE or
                    Base64.NO_PADDING or
                    Base64.NO_WRAP
        )
    }
}