package io.github.zm.auth_core.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.NSData
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.dataWithBytes
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault

@OptIn(ExperimentalForeignApi::class)
internal actual object CryptoProvider {

    actual fun secureRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)

        bytes.usePinned { pinned ->
            SecRandomCopyBytes(
                kSecRandomDefault,
                size.toULong(),
                pinned.addressOf(0)
            )
        }

        return bytes
    }

    actual fun sha256(bytes: ByteArray): ByteArray {
        val digest = ByteArray(CC_SHA256_DIGEST_LENGTH)

        bytes.usePinned { input ->
            digest.usePinned { output ->
                CC_SHA256(
                    data = input.addressOf(0),
                    len = bytes.size.toUInt(),
                    md = output.addressOf(0).reinterpret()
                )
            }
        }

        return digest
    }

    actual fun base64Encode(bytes: ByteArray): String {
        val data = bytes.usePinned { pinned ->
            NSData.dataWithBytes(
                bytes = pinned.addressOf(0),
                length = bytes.size.toULong()
            )
        }

        return data.base64EncodedStringWithOptions(0u)
    }
}