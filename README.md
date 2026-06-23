# KMP OIDC

Lightweight OpenID Connect (OIDC) authentication library for Kotlin Multiplatform.

kmp-oidc is currently a pre-stable 0.2.0 release. It already supports a working browser-based OIDC flow on Android and iOS, but the API may still change before 1.0.0.

## What Is Included

- Authorization Code Flow with PKCE
- OIDC discovery from /.well-known/openid-configuration
- Authorization code exchange
- Access token refresh
- Local logout
- Provider logout through end_session_endpoint
- Android secure token storage
- iOS Keychain token storage
- Provider-specific request customization

## Current Status

- Android support is available
- iOS support is available
- Keycloak is the only provider verified in this repository so far
- Compose-specific helpers are not part of auth-core

## Coordinates

```kotlin
implementation("io.github.worker432:kmp-oidc:0.2.0")
```

Android-only artifact:

```kotlin
implementation("io.github.worker432:kmp-oidc-android:0.2.0")
```

Current project properties:

- group = io.github.worker432
- artifact = kmp-oidc
- version = 0.2.0

This repository already contains maven-publish configuration. For local verification:

```bash
./gradlew :auth-core:publishToMavenLocal
```

If you use publishToMavenLocal, add mavenLocal() to your repositories.

## Before You Start

Before wiring the library into your app, make sure your OIDC client is configured at the provider side.

You need:

1. A registered OIDC client
2. A valid clientId
3. A registered login redirect URI
4. A registered logout redirect URI if you want provider logout

Example:

- login redirect URI: myapp://callback
- logout redirect URI: myapp://logout

These values are client-specific. They do not come from discovery metadata.

## Installation

### KMP Project

Add the multiplatform artifact to commonMain:

```kotlin
commonMain.dependencies {
    implementation("io.github.worker432:kmp-oidc:0.2.0")
}
```

### Android-only Project

If you want to use the library in a regular Android application, use the Android artifact:

```kotlin
dependencies {
    implementation("io.github.worker432:kmp-oidc-android:0.2.0")
}
```

### Maven Local

If you are consuming the library from mavenLocal() during development:

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
    google()
}
```

## Create The Config

```kotlin
val config = AuthConfig(
    clientId = "kmp-oidc-sdk",
    issuer = "https://issuer.example.com/realms/demo",
    redirectUri = "myapp://callback",
    logoutRedirectUri = "myapp://logout",
    scopes = listOf(
        "openid",
        "profile",
        "email",
        "offline_access"
    ),
    storageName = "auth_tokens"
)
```

What these fields mean:

- issuer: base URL of your OIDC provider
- clientId: registered OAuth/OIDC client id
- redirectUri: where the provider returns the user after login
- logoutRedirectUri: where the provider returns the user after provider logout
- scopes: requested scopes
- storageName: storage namespace for tokens

## Full Integration Flow

At a high level, client integration looks like this:

1. Add the dependency
2. Create AuthConfig
3. Create PlatformDependencies
4. Create AuthClient
5. Register login and logout redirect URIs in your app
6. Register the same redirect URIs in your OIDC provider
7. Start login with login()
8. Pass the callback URL back into the library on Android
9. Ask the library for a valid access token when needed

## Create The Client

```kotlin
val authClient = AuthClientFactory.create(
    config = config,
    dependencies = platformDependencies
)
```

PlatformDependencies is platform-specific.

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

## Android Setup

### Step 1. Add Internet Permission

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### Step 2. Register Redirect Intent Filters

Your activity must be able to receive the login and logout callbacks.

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTask">

    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="myapp"
            android:host="callback" />
    </intent-filter>

    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="myapp"
            android:host="logout" />
    </intent-filter>
</activity>
```

These values must match:

- redirectUri = "myapp://callback"
- logoutRedirectUri = "myapp://logout"

### Step 3. Create The Activity Integration

```kotlin
class MainActivity : ComponentActivity() {
    private var redirectUrl by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        redirectUrl = intent?.dataString

        setContent {
            App(
                dependencies = PlatformDependencies(
                    context = applicationContext,
                    activity = this
                ),
                redirectUrl = redirectUrl
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        redirectUrl = intent.dataString
    }
}
```

The important parts here:

- use PlatformDependencies with context and activity
- keep the latest redirect URL in state
- read intent?.dataString in onCreate
- update redirectUrl in onNewIntent

### Step 4. Pass Redirects Into The Library

In your Compose layer or screen logic:

```kotlin
LaunchedEffect(redirectUrl) {
    val url = redirectUrl ?: return@LaunchedEffect
    authClient.handleRedirect(url)
}
```

### Step 5. Start Login

```kotlin
scope.launch {
    val result = authClient.login()
}
```

On Android, login() usually returns AuthResult.Started because the browser flow continues outside the app. After the provider redirects back, call handleRedirect(url).

### Step 6. Get A Valid Access Token

```kotlin
scope.launch {
    when (val result = authClient.getValidAccessToken()) {
        is TokenResult.Success -> {
            val token = result.accessToken
        }
        TokenResult.NeedLogin -> {
            authClient.login()
        }
        is TokenResult.Failure -> Unit
    }
}
```

### Step 7. Allow HTTP For Local Development If Needed

If your local IdP runs on plain http, Android 9+ blocks cleartext traffic by default.

For emulator-based local development, either:

- use https
- or explicitly allow cleartext traffic for development only

Example:

```xml
<application
    android:usesCleartextTraffic="true" />
```

This is only for local development. Production should use https.

## iOS Setup

### Step 1. Register The URL Scheme

Add your redirect scheme to Info.plist. If your redirectUri is myapp://callback and your logoutRedirectUri is myapp://logout, the scheme is just myapp.

```xml
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>myapp</string>
        </array>
    </dict>
</array>
```

### Step 2. Allow HTTP For Local Development If Needed

If your local IdP uses plain http, iOS may require App Transport Security exceptions during development.

```xml
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
</dict>
```

This is only for local development. Production should use https.

### Step 3. Complete Example Info.plist

This is what a minimal local-development setup can look like:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CADisableMinimumFrameDurationOnPhone</key>
    <true/>

    <key>NSAppTransportSecurity</key>
    <dict>
        <key>NSAllowsArbitraryLoads</key>
        <true/>
    </dict>

    <key>CFBundleURLTypes</key>
    <array>
        <dict>
            <key>CFBundleURLSchemes</key>
            <array>
                <string>myapp</string>
            </array>
        </dict>
    </array>
</dict>
</plist>
```

### Step 4. Create PlatformDependencies

```kotlin
val platformDependencies = PlatformDependencies()
```

### Step 5. Create AuthClient

```kotlin
val authClient = AuthClientFactory.create(
    config = config,
    dependencies = PlatformDependencies()
)
```

### Step 6. Start Login

```kotlin
scope.launch {
    val result = authClient.login()
}
```

### Step 7. Redirect Handling On iOS

The default iOS integration uses ASWebAuthenticationSession, and this is the main difference from Android.

That means:

- the browser flow is opened by the library
- the callback scheme is passed into ASWebAuthenticationSession
- in the standard setup, login() may finish the redirect internally and return AuthResult.Success

Because of that, iOS usually does not need the same manual redirect wiring as Android.

### Step 8. Get A Valid Access Token

```kotlin
scope.launch {
    when (val result = authClient.getValidAccessToken()) {
        is TokenResult.Success -> {
            val token = result.accessToken
        }
        TokenResult.NeedLogin -> {
            authClient.login()
        }
        is TokenResult.Failure -> Unit
    }
}
```

## Login

```kotlin
when (val result = authClient.login()) {
    AuthResult.Started -> {
        // Typical Android path:
        // browser opened and redirect will be delivered later.
    }

    AuthResult.Success -> {
        // Typical iOS path:
        // ASWebAuthenticationSession completed and tokens are already stored.
    }

    AuthResult.AccessDenied -> {
        // Provider returned access_denied
    }

    AuthResult.Cancelled -> Unit

    is AuthResult.Failure -> {
        // Redirect, discovery, browser, storage, or token error
    }
}
```

## Handle Redirect

Android:

```kotlin
authClient.handleRedirect(redirectUrl)
```

iOS:

- in the standard ASWebAuthenticationSession path, the redirect is usually handled during login()
- avoid manually passing the same redirect twice

## Typical Client Setup

This is the minimum shape of a client integration:

```kotlin
val authClient = AuthClientFactory.create(
    config = AuthConfig(
        clientId = "kmp-oidc-sdk",
        issuer = "http://10.0.2.2:8080/realms/kmp",
        redirectUri = "myapp://callback",
        logoutRedirectUri = "myapp://logout",
        scopes = listOf("openid", "profile", "email", "offline_access"),
        storageName = "auth_tokens"
    ),
    dependencies = platformDependencies
)
```

For Android emulator-based local development, 10.0.2.2 is the usual host alias for services running on the development machine.

## Get A Valid Access Token

```kotlin
when (val result = authClient.getValidAccessToken()) {
    is TokenResult.Success -> {
        val accessToken = result.accessToken
    }

    TokenResult.NeedLogin -> {
        authClient.login()
    }

    is TokenResult.Failure -> {
        // Refresh or storage failure
    }
}
```

If the access token is expired and a refresh token is available, the library tries to refresh it automatically.

## Logout

Local logout:

```kotlin
authClient.logout(LogoutMode.LOCAL_ONLY)
```

Provider logout:

```kotlin
authClient.logout(LogoutMode.LOCAL_AND_PROVIDER)
```

Provider logout uses end_session_endpoint when it is available in discovery metadata.

## Identity Provider Customization

If your provider needs extra query or form parameters, use IdpCustomization.

```kotlin
val config = AuthConfig(
    clientId = "client-id",
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

## Supported Identity Providers

Current compatibility status:

| Provider | Status | Notes |
| --- | --- | --- |
| Keycloak | Tested | Verified in the sample app on Android and iOS |
| Other OIDC providers | Not verified yet | The library follows standard OIDC flows, but they have not been verified in this repository yet |

## Public API

```kotlin
interface AuthClient {
    suspend fun login(): AuthResult
    suspend fun handleRedirect(url: String): AuthResult
    suspend fun getValidAccessToken(): TokenResult
    suspend fun logout(mode: LogoutMode = LogoutMode.LOCAL_ONLY): AuthResult
}
```

## Current Limitations

- API is still pre-1.0.0
- Only Keycloak has been verified in this repository so far
- Compose-specific state helpers are not part of auth-core
- Local HTTP development still requires platform-specific setup
- Maven Central publication is not configured yet

## Roadmap

- More automated tests around refresh and logout flows
- Better redirect lifecycle guidance for consumers
- More provider-specific OAuth/OIDC error mapping
- Optional Compose-friendly auth state in a separate module
- Maven Central publication
- Additional sample apps and integration docs

## License

Licensed under the Apache License 2.0.
