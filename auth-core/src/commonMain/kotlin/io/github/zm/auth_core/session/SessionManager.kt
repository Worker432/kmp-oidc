package io.github.zm.auth_core.session

interface SessionManager {
    suspend fun createSession(
        state: String,
        codeVerifier: String
    ): AuthSession

    suspend fun getCurrentSession(): AuthSession?

    suspend fun validateState(
        receivedState: String
    ): Boolean

    suspend fun clear()
}
