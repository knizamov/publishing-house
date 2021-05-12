package io.github.knizamov.publishing.shared.security

import java.lang.IllegalArgumentException

public sealed class User {
    public abstract val userId: String
    public abstract val roles: List<String>

    public fun hasRole(role: String): Boolean {
        return this.roles.any { it.equals(role, ignoreCase = true) }
    }

    public fun assertHasRole(role: String) {
        if (!this.hasRole(role)) {
            throw AuthError.MissingRole(userId = userId, role = role)
        }
    }
}

public inline fun <reified U : User> User.assumeRole(): U {
    return when (U::class) {
        Journalist::class -> Journalist(userId = userId, roles = roles)
        Copywriter::class -> Copywriter(userId = userId, roles = roles)
        GenericUser::class -> GenericUser(userId = userId, roles = roles)
        else -> throw IllegalArgumentException("Unknown user ${U::class.simpleName}") // unfortunately there is no exhaustive checks for KClass<T>
    } as U
}


public data class GenericUser(override val userId: String, override val roles: List<String>): User()

public data class Journalist(override val userId: String, override val roles: List<String>) : User() {
    init {
        assertHasRole("Journalist")
    }
}

public data class Copywriter(override val userId: String, override val roles: List<String>) : User() {
    init {
        assertHasRole("Copywriter")
    }
}

