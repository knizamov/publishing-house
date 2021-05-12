package io.github.knizamov.publishing.base

import io.github.knizamov.publishing.shared.security.Copywriter
import io.github.knizamov.publishing.shared.security.Journalist
import io.github.knizamov.publishing.shared.security.User
import io.github.knizamov.publishing.shared.security.UserContext

internal class TestUserContext(defaultUser: User? = null) : UserContext {
    private var currentUser: User? = defaultUser

    override fun authenticatedUserOrNull(): User? {
        return currentUser
    }

    fun currentUser(): User? {
        return currentUser
    }

    fun setUser(user: User?) {
        this.currentUser = user
    }

    fun setJournalist(journalist: Journalist) {
        this.currentUser = journalist
    }

    fun setCopywriter(copywriter: Copywriter) {
        this.currentUser = copywriter
    }

    fun setUnauthenticated() {
        this.currentUser = null
    }
}