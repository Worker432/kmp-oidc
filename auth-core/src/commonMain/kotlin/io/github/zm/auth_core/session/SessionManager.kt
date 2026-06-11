package io.github.zm.auth_core.session

interface SessionManager {
    fun createSession(
        state: String,
        codeVerifier: String
    ): AuthSession

    fun getCurrentSession(): AuthSession?

    fun validateState(
        receivedState: String
    ): Boolean

    fun clear()
}