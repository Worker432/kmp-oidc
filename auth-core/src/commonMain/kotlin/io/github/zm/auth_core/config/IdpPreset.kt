package io.github.zm.auth_core.config

sealed interface IdpPreset {
    data object GenericOidc : IdpPreset
    data object Keycloak : IdpPreset
}