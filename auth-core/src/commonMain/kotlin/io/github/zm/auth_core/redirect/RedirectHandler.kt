package io.github.zm.auth_core.redirect

interface RedirectHandler {
    fun parse(url: String): RedirectParams
}