package io.github.zm.auth_core.redirect

internal class DefaultRedirectHandler : RedirectHandler {

    override fun parse(url: String): RedirectParams {
        val query = url.substringAfter("?", missingDelimiterValue = "")
        val params = query
            .split("&")
            .mapNotNull { pair ->
                val key = pair.substringBefore(
                    delimiter = "=",
                    missingDelimiterValue = ""
                )

                val value = pair.substringAfter(
                    delimiter = "=",
                    missingDelimiterValue = ""
                )

                if (key.isBlank() || value.isBlank()) {
                    null
                } else {
                    key to value
                }
            }
            .toMap()

        val code = params["code"]
            ?: error("Missing code in redirect url")
        val state = params["state"]
            ?: error("Missing state in redirect url")

        return RedirectParams(
            code = code,
            state = state
        )
    }
}