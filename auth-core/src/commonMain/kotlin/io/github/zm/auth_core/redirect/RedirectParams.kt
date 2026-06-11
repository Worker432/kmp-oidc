package io.github.zm.auth_core.redirect

data class RedirectParams(
    val code: String,
    val state: String
)