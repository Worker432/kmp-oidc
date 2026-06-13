package io.github.zm.auth_core.storage

import io.github.zm.auth_core.token.TokenSet
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.serialization.json.Json
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryCreate
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
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
internal class IosKeychainTokenStorage(
    private val storageName: String
) : TokenStorage {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun save(tokens: TokenSet) {
        val encoded = json.encodeToString(tokens)
        saveString(encoded)
    }

    override suspend fun read(): TokenSet? {
        val encoded = readString() ?: return null
        return json.decodeFromString<TokenSet>(encoded)
    }

    override suspend fun clear() {
        deleteItem()
    }

    private fun saveString(value: String) = memScoped {
        deleteItem()

        val data = value
            .encodeToByteArray()
            .toNSData()

        val query = cfDictionary(
            mapOf(
                kSecClass to (kSecClassGenericPassword as CFTypeRef?),
                kSecAttrService to cfString(storageName),
                kSecAttrAccount to cfString(KEY_TOKENS),
                kSecValueData to cfData(data)
            )
        )

        val status = SecItemAdd(query, null)

        if (status != errSecSuccess) {
            throw IllegalStateException("Keychain save failed: $status")
        }
    }

    @OptIn(BetaInteropApi::class)
    private fun readString(): String? = memScoped {
        val query = cfDictionary(
            mapOf(
                kSecClass to (kSecClassGenericPassword as CFTypeRef?),
                kSecAttrService to cfString(storageName),
                kSecAttrAccount to cfString(KEY_TOKENS),
                kSecReturnData to (kCFBooleanTrue as CFTypeRef?),
                kSecMatchLimit to (kSecMatchLimitOne as CFTypeRef?)
            )
        )

        val result = alloc<CFTypeRefVar>()

        val status = SecItemCopyMatching(
            query,
            result.ptr
        )

        when (status) {
            errSecSuccess -> {
                val data = result.value as? CFDataRef
                    ?: return@memScoped null

                return@memScoped data.toUtf8String()
            }
            errSecItemNotFound -> {
                return@memScoped null
            }
            else -> {
                throw IllegalStateException("Keychain read failed: $status")
            }
        }
    }

    private fun deleteItem() = memScoped {
        val query = cfDictionary(
            mapOf(
                kSecClass to (kSecClassGenericPassword as CFTypeRef?),
                kSecAttrService to cfString(storageName),
                kSecAttrAccount to cfString(KEY_TOKENS)
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

    private fun cfData(data: NSData): CFDataRef {
        return CFDataCreate(
            kCFAllocatorDefault,
            data.bytes?.reinterpret(),
            data.length.toLong()
        )!!
    }

    @OptIn(BetaInteropApi::class)
    private fun CFDataRef.toUtf8String(): String? {
        val length = CFDataGetLength(this)
        if (length == 0L) {
            return ""
        }

        val bytes = CFDataGetBytePtr(this)
            ?: return null

        val string = NSString.create(
            bytes = bytes,
            length = length.toULong(),
            encoding = NSUTF8StringEncoding
        )

        return string?.toString()
    }

    private companion object {
        const val KEY_TOKENS = "tokens"
    }
}
