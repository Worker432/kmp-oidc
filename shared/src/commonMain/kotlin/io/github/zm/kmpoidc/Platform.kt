package io.github.zm.kmpoidc

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform