package io.github.knizamov.publishing.shared.security

public interface UserContext {

    public fun authenticatedUser(): User {
        val user = this.authenticatedUserOrNull() ?: throw AuthError.Unauthenticated()
        return user
    }

    public fun authenticatedUserOrNull(): User?
}

public inline fun <reified U : User> UserContext.assumeRole(): U {
    val currentUser = this.authenticatedUser()
    val assumeRole = currentUser.assumeRole<U>()
    return assumeRole
}