package io.github.zm.auth_core.redirect

import io.github.zm.auth_core.request.util.urlDecode

internal class DefaultRedirectHandler : RedirectHandler {

    override fun parse(url: String): RedirectParams {
        val query = url.substringAfter("?", missingDelimiterValue = "")
        val params = query
            .split("&")
            .filter { it.isNotBlank() }
            .mapNotNull { pair ->
                val key = pair.substringBefore(
                    delimiter = "=",
                    missingDelimiterValue = ""
                ).urlDecode()

                val value = pair.substringAfter(
                    delimiter = "=",
                    missingDelimiterValue = ""
                ).urlDecode()

                if (key.isBlank()) {
                    null
                } else {
                    key to value
                }
            }
            .toMap()

        val error = params["error"]
        if (!error.isNullOrBlank()) {
            return RedirectParams.Error(
                error = error,
                state = params["state"],
                errorDescription = params["error_description"]
            )
        }

        val code = params["code"]
            ?: throw IllegalArgumentException("Missing code in redirect url")
        val state = params["state"]
            ?: throw IllegalArgumentException("Missing state in redirect url")

        return RedirectParams.AuthorizationCode(
            code = code,
            state = state
        )
    }
}
