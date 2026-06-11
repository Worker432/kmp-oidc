package io.github.zm.auth_core.session

//Хранит данные, необходимые для продолжения Authorization Code Flow после возврата из браузера.
data class AuthSession(
    val state: String,
    val codeVerifier: String,
)