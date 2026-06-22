package io.github.zm.auth_core.session

internal interface AuthSessionStorage {
    suspend fun save(session: AuthSession)
    suspend fun read(): AuthSession?
    suspend fun clear()
}
