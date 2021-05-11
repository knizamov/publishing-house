package io.github.knizamov.publishing.base

import java.lang.RuntimeException


inline fun catch(block: () -> Unit): RuntimeException {
    var caughtException: RuntimeException? = null
    try {
        block()
    } catch (ex: RuntimeException) {
        caughtException = ex
    }

    assert(caughtException != null) { "Expected exception to be thrown but there was none" }
    return caughtException!!
}