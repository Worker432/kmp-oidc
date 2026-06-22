package io.github.zm.auth_core.session

internal class DefaultSessionManager(
    private val storage: AuthSessionStorage
) : SessionManager {
    private var currentSession: AuthSession? = null

    override suspend fun createSession(
        state: String,
        codeVerifier: String
    ): AuthSession {
        val session = AuthSession(
            state = state,
            codeVerifier = codeVerifier
        )

        storage.save(session)
        currentSession = session

        return session
    }

    override suspend fun getCurrentSession(): AuthSession? {
        currentSession?.let { return it }

        val restored = storage.read()
        currentSession = restored
        return restored
    }

    override suspend fun validateState(receivedState: String): Boolean {
        return getCurrentSession()?.state == receivedState
    }

    override suspend fun clear() {
        currentSession = null
        storage.clear()
    }
}
