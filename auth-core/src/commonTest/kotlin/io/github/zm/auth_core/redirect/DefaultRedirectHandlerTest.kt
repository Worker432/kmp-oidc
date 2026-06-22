package io.github.zm.auth_core.redirect

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class DefaultRedirectHandlerTest {
    private val handler = DefaultRedirectHandler()

    @Test
    fun `parses authorization code redirect and decodes values`() {
        val result = handler.parse(
            "myapp://callback?code=a%2Bb%20c&state=state%2D123"
        )

        val params = assertIs<RedirectParams.AuthorizationCode>(result)
        assertEquals("a+b c", params.code)
        assertEquals("state-123", params.state)
    }

    @Test
    fun `parses provider error redirect`() {
        val result = handler.parse(
            "myapp://callback?error=access_denied&state=s123&error_description=User+cancelled"
        )

        val params = assertIs<RedirectParams.Error>(result)
        assertEquals("access_denied", params.error)
        assertEquals("s123", params.state)
        assertEquals("User cancelled", params.errorDescription)
    }

    @Test
    fun `fails on malformed success redirect`() {
        assertFailsWith<IllegalArgumentException> {
            handler.parse("myapp://callback?state=only-state")
        }
    }

    @Test
    fun `keeps optional state nullable for provider error`() {
        val result = handler.parse(
            "myapp://callback?error=temporarily_unavailable"
        )

        val params = assertIs<RedirectParams.Error>(result)
        assertNull(params.state)
        assertNull(params.errorDescription)
    }
}
