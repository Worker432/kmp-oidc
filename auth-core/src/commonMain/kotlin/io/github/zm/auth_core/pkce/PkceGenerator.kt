package io.github.zm.auth_core.pkce

interface PkceGenerator {
    fun generateCodeVerifier(): String
    fun generateCodeChallenge(
        codeVerifier: String
    ): String
}