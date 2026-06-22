package io.github.zm.auth_core.storage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDictionaryCreate
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataWithBytes
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

@OptIn(ExperimentalForeignApi::class)
internal class IosKeychainStringStorage(
    private val storageName: String
) {
    fun save(
        key: String,
        value: String
    ) = memScoped {
        clear(key)

        val query = cfDictionary(
            mapOf(
                kSecClass to (kSecClassGenericPassword as CFTypeRef?),
                kSecAttrService to cfString(storageName),
                kSecAttrAccount to cfString(key),
                kSecValueData to cfData(value.encodeToByteArray().toNSData())
            )
        )

        val status = SecItemAdd(query, null)

        if (status != errSecSuccess) {
            throw IllegalStateException("Keychain save failed: $status")
        }
    }

    @OptIn(BetaInteropApi::class)
    fun read(key: String): String? = memScoped {
        val query = cfDictionary(
            mapOf(
                kSecClass to (kSecClassGenericPassword as CFTypeRef?),
                kSecAttrService to cfString(storageName),
                kSecAttrAccount to cfString(key),
                kSecReturnData to (kCFBooleanTrue as CFTypeRef?),
                kSecMatchLimit to (kSecMatchLimitOne as CFTypeRef?)
            )
        )

        val result = alloc<ObjCObjectVar<NSData?>>()
        val status = SecItemCopyMatching(
            query,
            result.ptr.reinterpret()
        )

        when (status) {
            errSecSuccess -> {
                val data = result.value
                    ?: return@memScoped null

                return@memScoped data.toUtf8String()
            }

            errSecItemNotFound -> return@memScoped null
            else -> throw IllegalStateException("Keychain read failed: $status")
        }
    }

    fun clear(key: String) = memScoped {
        val query = cfDictionary(
            mapOf(
                kSecClass to (kSecClassGenericPassword as CFTypeRef?),
                kSecAttrService to cfString(storageName),
                kSecAttrAccount to cfString(key)
            )
        )

        val status = SecItemDelete(query)

        if (status != errSecSuccess && status != errSecItemNotFound) {
            throw IllegalStateException("Keychain delete failed: $status")
        }
    }

    private fun MemScope.cfString(value: String): CFStringRef {
        return CFStringCreateWithCString(
            kCFAllocatorDefault,
            value,
            kCFStringEncodingUTF8
        )!!
    }

    private fun MemScope.cfDictionary(
        map: Map<CFStringRef?, CFTypeRef?>
    ): CFDictionaryRef {
        val keys = allocArray<COpaquePointerVar>(map.size)
        val values = allocArray<COpaquePointerVar>(map.size)

        var index = 0
        for ((key, value) in map) {
            keys[index] = key
            values[index] = value
            index++
        }

        return CFDictionaryCreate(
            allocator = kCFAllocatorDefault,
            keys = keys,
            values = values,
            numValues = map.size.toLong(),
            keyCallBacks = kCFTypeDictionaryKeyCallBacks.ptr,
            valueCallBacks = kCFTypeDictionaryValueCallBacks.ptr
        )!!
    }

    private fun ByteArray.toNSData(): NSData {
        return usePinned { pinned ->
            NSData.dataWithBytes(
                bytes = pinned.addressOf(0),
                length = size.toULong()
            )
        }
    }

    private fun cfData(data: NSData) = CFDataCreate(
        kCFAllocatorDefault,
        data.bytes?.reinterpret(),
        data.length.toLong()
    )!!

    @OptIn(BetaInteropApi::class)
    private fun NSData.toUtf8String(): String? {
        val string = NSString.create(
            bytes = bytes,
            length = length,
            encoding = NSUTF8StringEncoding
        )

        return string?.toString()
    }
}
