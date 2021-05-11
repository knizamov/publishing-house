package io.github.knizamov.publishing.base

import io.github.knizamov.publishing.shared.authentication.Copywriter
import io.github.knizamov.publishing.shared.authentication.Journalist
import io.github.knizamov.publishing.shared.authentication.User
import java.util.*

internal interface UserSamples {
    val testUserContext: TestUserContext

    fun randomJournalist(): Journalist = Journalist(userId = "journalist${UUID.randomUUID()}", roles = listOf("Journalist"))
    fun randomCopywriter(): Copywriter = Copywriter(userId = "copywriter${UUID.randomUUID()}", roles = listOf("Copywriter"))
    fun journalistA(): Journalist = Journalist(userId = "journalistA", roles = listOf("Journalist"))
    fun journalistB(): Journalist = Journalist(userId = "journalistB", roles = listOf("Journalist"))
    fun copywriterA(): Copywriter = Copywriter(userId = "copywriterA", roles = listOf("Copywriter"))
    fun copywriterB(): Copywriter = Copywriter(userId = "copywriterB", roles = listOf("Copywriter"))


    fun <R> asJournalist(journalist: Journalist = randomJournalist(), block: () -> R): R = `as`(journalist, block)
    fun <R> asCopywriter(copywriter: Copywriter = randomCopywriter(), block: () -> R): R = `as`(copywriter, block)
    fun <R> asJournalistA(block: () -> R): R = `as`(journalistA(), block)
    fun <R> asJournalistB(block: () -> R): R = `as`(journalistB(), block)
    fun <R> asCopywriterA(block: () -> R): R = `as`(copywriterA(), block)
    fun <R> asCopywriterB(block: () -> R): R = `as`(copywriterB(), block)


    fun <R> `as`(user: User, block: () -> R): R {
        val previousUser = testUserContext.currentUser()
        testUserContext.setUser(user)
        val result = block()
        testUserContext.setUser(previousUser)
        return result
    }
}