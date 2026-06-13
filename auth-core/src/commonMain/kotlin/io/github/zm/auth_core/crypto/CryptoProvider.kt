package io.github.zm.auth_core.crypto

internal expect object CryptoProvider {
    fun secureRandomBytes(size: Int): ByteArray
    fun sha256(bytes: ByteArray): ByteArray
    fun base64Encode(bytes: ByteArray): String
}
