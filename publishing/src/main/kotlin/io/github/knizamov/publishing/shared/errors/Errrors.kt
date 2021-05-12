package io.github.knizamov.publishing.shared.errors

import java.lang.RuntimeException


// Note for a reviewer: I use exceptions even for domain specific errors because the standard kotlin lacks
// some necessary functional style primitives to allow for Railway Oriented Programming style handling of the flow
// We need some monads and for-comprehension to handle it in a readable and manageable way (I know arrow supports but I have no experience with that)
// Without the necessary primitives, returning errors types in a functional way from a deeply nested code would be a nightmare
public abstract class Error : RuntimeException() {
    public open val type: String
        get() = this::class.simpleName!!
    abstract override val message: String
    abstract override val cause: Throwable?
}


