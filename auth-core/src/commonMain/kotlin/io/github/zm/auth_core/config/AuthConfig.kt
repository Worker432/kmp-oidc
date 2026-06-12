package io.github.zm.auth_core.config

data class AuthConfig(
    val clientId: String, // Идентификатор зарегистрированного OAuth/OIDC клиента
    val issuer: String, // Корневой URL Identity Provider
    val redirectUri: String, // URI, на который Identity Provider вернёт пользователя после авторизации
    val logoutRedirectUri: String,
    // Запрашиваемые разрешения
    val scopes: List<String> = listOf(
        "openid",
        "profile",
        "email"
    ),
    val storageName: String = "auth_tokens",
    val preset: IdpPreset = IdpPreset.GenericOidc,
    val customization: IdpCustomization = IdpCustomization()
)