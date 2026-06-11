package io.github.zm.auth_core.session

class DefaultSessionManager: SessionManager {
    private var currentSession: AuthSession? = null

    override fun createSession(
        state: String,
        codeVerifier: String
    ): AuthSession {

        val session = AuthSession(
            state = state,
            codeVerifier = codeVerifier
        )

        currentSession = session

        return session
    }

    override fun getCurrentSession(): AuthSession? {
        return currentSession
    }

    override fun validateState(receivedState: String): Boolean {
        return currentSession?.state == receivedState
    }

    override fun clear() {
        currentSession = null
    }
}