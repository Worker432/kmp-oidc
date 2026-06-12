# KMP OIDC
Lightweight OpenID Connect (OIDC) authentication library for Kotlin Multiplatform.
Project Status
This library is currently under active development.
The API may change before the first stable release (1.0.0).
Android support is available, while iOS support is still in progress.
## Features
1) Authorization Code Flow with PKCE
2) OpenID Connect Discovery
3) Automatic Access Token Refresh
4) Secure Token Storage
5) Browser-based Authentication
6) Local Logout
7) Provider Logout (end_session_endpoint)
8) Identity Provider Customization
9) Kotlin Multiplatform Support
## Installation
Add the dependency to your project:
```kotlin
dependencies {
    implementation("io.github.zm:kmp-oidc:<version>")
}
```
## Configuration
Create an AuthConfig instance:
```kotlin
val config = AuthConfig(
    clientId = "...",
    issuer = "...",
    redirectUri = "myapp://callback",
    logoutRedirectUri = "myapp://logout",

    scopes = listOf(
        "openid",
        "profile",
        "email",
        "offline_access"
    )
)
```
## Create AuthClient
```kotlin
val authClient = AuthClientFactory.create(
    config = config,
    dependencies = platformDependencies
)
```
## Login
Start the authorization flow:
```kotlin
authClient.login()
```
The library opens the system browser and redirects the user to the configured Identity Provider.
## Handle Redirect
After successful authentication, pass the redirect URI back to the library:
```kotlin
authClient.handleRedirect(
    redirectUrl
)
```
## Get Access Token
```kotlin
when (val result = authClient.getValidAccessToken()) {
    is TokenResult.Success -> {
        val accessToken = result.accessToken
    }
    TokenResult.NeedLogin -> {
        authClient.login()
    }
}
```
If the current access token has expired, the library automatically performs a refresh token request.
## Logout
Local logout:
```kotlin
authClient.logout(
    LogoutMode.LocalOnly
)
```
Logout from the Identity Provider:
```kotlin
authClient.logout(
    LogoutMode.LocalAndProvider
)
```
## Identity Provider Customization
Additional provider-specific parameters can be supplied through IdpCustomization.
```kotlin
AuthConfig(
    ...
    customization = IdpCustomization(
        authorizationParameters = mapOf(
            "prompt" to "login"
        ),
        tokenParameters = mapOf(),
        logoutParameters = mapOf()
    )
)
```
This makes the library compatible with OIDC providers that require custom request parameters.
## Supported Identity Providers
The library is designed to work with any OIDC-compatible Identity Provider.
Examples include:
* Keycloak
* Auth0
* Okta
* Azure AD / Microsoft Entra ID
* Google Identity Platform
* AWS Cognito
* FusionAuth
## Roadmap
* Authorization Code Flow + PKCE
* Token Exchange
* Token Refresh
* Secure Token Storage
* Provider Logout
* Identity Provider Customization
* iOS Support
* Unit Tests
* Maven Central Publication
* Compose Integration
## License
Licensed under the Apache License 2.0.
