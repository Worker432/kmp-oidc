package io.github.zm.auth_core.crypto

internal fun ByteArray.base64UrlEncodeWithoutPadding(): String {
    return CryptoProvider
        .base64Encode(this)
        .replace('+', '-')
        .replace('/', '_')
        .replace("=", "")
}