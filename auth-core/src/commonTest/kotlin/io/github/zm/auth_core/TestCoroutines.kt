package io.github.zm.auth_core

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

internal fun runSuspendTest(block: suspend () -> Unit) {
    var failure: Throwable? = null

    block.startCoroutine(
        object : Continuation<Unit> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                failure = result.exceptionOrNull()
            }
        }
    )

    failure?.let { throw it }
}
