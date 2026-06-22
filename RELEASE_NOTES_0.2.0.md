# kmp-oidc 0.2.0

`0.2.0` is the current pre-stable release of `kmp-oidc`.

This version includes a working browser-based OIDC flow for Android and iOS with Authorization Code + PKCE, discovery, token exchange, refresh, logout, and secure token storage.

## Highlights

- Android and iOS support
- Authorization Code Flow with PKCE
- OIDC discovery
- Automatic access token refresh
- Android encrypted storage and iOS Keychain storage
- Provider logout through `end_session_endpoint`
- Redirect parsing with URL decoding and OAuth error handling
- Improved automated coverage for redirect, authorization URL, token, and auth client flows

## Notes

- This is still a pre-`1.0.0` release, so API changes are still possible
- Keycloak is the only provider verified in this repository so far
- Compose integration is planned as a separate layer rather than part of `auth-core`
