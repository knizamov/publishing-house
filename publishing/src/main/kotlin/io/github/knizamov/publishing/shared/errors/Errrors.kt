package io.github.knizamov.publishing.shared.errors

import java.lang.RuntimeException

public abstract class Error : RuntimeException() {
    public open val type: String
        get() = this::class.simpleName!!
    abstract override val message: String
    abstract override val cause: Throwable?

}


