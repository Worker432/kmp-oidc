package io.github.zm.auth_core.request.util

internal fun String.uriScheme(): String {
    return substringBefore(":")
}