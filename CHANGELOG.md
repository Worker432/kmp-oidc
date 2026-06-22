# Changelog

## 0.2.0

- Added Android and iOS browser-based OIDC login support
- Added PKCE-based Authorization Code flow
- Added discovery, token exchange, token refresh, and logout flows
- Added Android secure storage and iOS Keychain storage
- Added persisted temporary auth session storage for Android and iOS
- Hardened redirect parsing with URL decoding and OAuth error handling
- Added smoke tests for redirect parsing, authorization URL building, token handling, and auth client flow
- Updated documentation to reflect the current multiplatform API
