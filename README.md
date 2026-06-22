# KMP OIDC

Lightweight OpenID Connect (OIDC) authentication library for Kotlin Multiplatform.

## Status

Version 0.2.0 is still pre-stable.

- Android support is available
- iOS support is available
- API may still change before `1.0.0`

## Features

1. Authorization Code Flow with PKCE
2. OpenID Connect Discovery
3. Automatic access token refresh
4. Secure token storage
5. Browser-based authentication
6. Local logout
7. Provider logout through `end_session_endpoint`
8. Identity Provider customization hooks
9. Kotlin Multiplatform support

## Coordinates

Project coordinates for this release:

```kotlin
implementation("io.github.worker432:kmp-oidc:0.2.0")
```

This repository currently includes `maven-publish` configuration and can be verified locally with:

```bash
./gradlew :auth-core:publishToMavenLocal
```

If you publish the artifact to GitHub Packages or another Maven repository, use the same coordinates there.

## Configuration

```kotlin
val config = AuthConfig(
    clientId = "...",
    issuer = "https://issuer.example.com/realms/demo",
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

### PlatformDependencies

Android:

```kotlin
val platformDependencies = PlatformDependencies(
    context = applicationContext,
    activity = this
)
```

iOS:

```kotlin
val platformDependencies = PlatformDependencies()
```

## Login

Start the authorization flow:

```kotlin
when (val result = authClient.login()) {
    AuthResult.Started -> {
        // Typical Android path: browser opened, redirect will be delivered later.
    }
    AuthResult.Success -> {
        // Typical iOS path: ASWebAuthenticationSession completed and tokens are already stored.
    }
    AuthResult.AccessDenied -> {
        // Provider returned access_denied.
    }
    is AuthResult.Failure -> {
        // Handle transport / redirect / token errors.
    }
    AuthResult.Cancelled -> Unit
}
```

The library opens the system browser and sends the user to your OIDC provider.

## Handle Redirect

On Android, pass the redirect URI back to the library after it returns to your activity:

```kotlin
authClient.handleRedirect(redirectUrl)
```

On iOS, login may finish the redirect inside ASWebAuthenticationSession.
If your app also forwards incoming URLs by hand, make sure the same redirect is not passed twice.

## Get Access Token

```kotlin
when (val result = authClient.getValidAccessToken()) {
    is TokenResult.Success -> {
        val accessToken = result.accessToken
    }
    TokenResult.NeedLogin -> {
        authClient.login()
    }
    is TokenResult.Failure -> {
        // Handle refresh/storage failures.
    }
}
```

If the access token has expired and a refresh token is available, the library will try to refresh it automatically.

## Logout

Local logout:

```kotlin
authClient.logout(LogoutMode.LOCAL_ONLY)
```

Logout from the Identity Provider:

```kotlin
authClient.logout(LogoutMode.LOCAL_AND_PROVIDER)
```

## Identity Provider Customization

If your provider needs extra query or form parameters, add them through IdpCustomization.

```kotlin
val config = AuthConfig(
    clientId = "...",
    issuer = "https://issuer.example.com/realms/demo",
    redirectUri = "myapp://callback",
    logoutRedirectUri = "myapp://logout",
    customization = IdpCustomization(
        authorizationParameters = mapOf(
            "prompt" to "login"
        ),
        tokenParameters = mapOf(),
        logoutParameters = mapOf()
    )
)
```

This is useful for providers that expect extra parameters in authorize, token, or logout requests.

## Identity Providers

Current compatibility status:

| Provider | Status | Notes |
| --- | --- | --- |
| Keycloak | Tested | Verified in the sample app on Android and iOS |
| Other OIDC providers | Not verified yet | The library is built around standard OIDC flows, but they have not been tested in this repository yet |

## Current Limitations

- API is still pre-`1.0.0`
- Only Keycloak has been verified in this repository so far
- Local development on iOS may still require extra setup for HTTP issuers
- Compose-specific state helpers are not part of `auth-core`
- Maven Central publication is not configured yet

## Roadmap

- More automated tests around token refresh and logout flows
- Better redirect lifecycle guidance for iOS consumers
- Hardened error mapping for more provider-specific OAuth/OIDC errors
- Optional Compose-friendly auth state in a separate module, without adding Compose to `auth-core`
- Maven Central publication
- Additional sample apps and integration docs

## License

Licensed under the Apache License 2.0.
