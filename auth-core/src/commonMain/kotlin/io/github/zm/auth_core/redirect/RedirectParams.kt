package io.github.zm.auth_core.redirect

sealed interface RedirectParams {
    data class AuthorizationCode(
        val code: String,
        val state: String
    ) : RedirectParams

    data class Error(
        val error: String,
        val state: String? = null,
        val errorDescription: String? = null
    ) : RedirectParams
}
