package io.github.knizamov.publishing.shared.authentication

import io.github.knizamov.publishing.shared.errors.Error

public abstract class AuthError : Error() {
    public abstract val userId: String?

    public open class Unauthenticated(
        override val userId: String? = null,
        override val message: String = "Current user $userId is unauthenticated",
        override val cause: Throwable? = null,
    ) : AuthError()

    public open class Unauthorized(
        override val userId: String? = null,
        override val message: String = "Current user $userId has no permission to do the operation",
        override val cause: Throwable? = null,
    ) : AuthError()

    public open class MissingRole(
        override val userId: String? = null,
        public val role: String,
        override val message: String = "User $userId has no role $role",
        override val cause: Throwable? = null,
    ) : AuthError()
}