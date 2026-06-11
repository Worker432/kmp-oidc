package io.github.zm.auth_core.state

interface StateGenerator {
    fun generateState(): String
}