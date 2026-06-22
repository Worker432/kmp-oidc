package io.github.zm.auth_core.request.util

internal fun String.urlDecode(): String {
    if (isEmpty()) {
        return this
    }

    val result = StringBuilder(length)
    val pendingBytes = mutableListOf<Byte>()
    var index = 0

    fun flushPendingBytes() {
        if (pendingBytes.isEmpty()) {
            return
        }

        result.append(pendingBytes.toByteArray().decodeToString())
        pendingBytes.clear()
    }

    while (index < length) {
        when (val char = this[index]) {
            '%' -> {
                if (index + 2 >= length) {
                    throw IllegalArgumentException("Invalid percent-encoding in '$this'")
                }

                val hex = substring(index + 1, index + 3)
                val byte = hex.toIntOrNull(16)
                    ?: throw IllegalArgumentException("Invalid percent-encoding in '$this'")

                pendingBytes.add(byte.toByte())
                index += 3
            }

            '+' -> {
                flushPendingBytes()
                result.append(' ')
                index++
            }

            else -> {
                flushPendingBytes()
                result.append(char)
                index++
            }
        }
    }

    flushPendingBytes()

    return result.toString()
}
