package io.github.zm.auth_core.request.util

internal fun String.urlEncode(): String {
    return encodeToByteArray()
        .joinToString("") { byte ->
            val char = byte.toInt().toChar()

            when {
                char.isLetterOrDigit() -> char.toString()
                char in listOf('-', '_', '.', '~') -> char.toString()
                else -> "%${byte.toUByte().toString(16).uppercase().padStart(2, '0')}"
            }
        }
}