package io.github.knizamov.publishing.base

import io.github.knizamov.publishing.shared.authentication.Copywriter
import io.github.knizamov.publishing.shared.authentication.Journalist
import io.github.knizamov.publishing.shared.authentication.User
import org.junit.jupiter.api.BeforeEach
import java.util.*

internal interface UserSamples {
    val testUserContext: TestUserContext

    fun randomJournalist(): Journalist = Journalist(userId = "journalist${UUID.randomUUID()}", roles = listOf("Journalist"))
    fun randomCopywriter(): Copywriter = Copywriter(userId = "copywriter${UUID.randomUUID()}", roles = listOf("Copywriter"))
    val journalistA: Journalist get() = Journalist(userId = "journalistA", roles = listOf("Journalist"))
    val journalistB: Journalist get() = Journalist(userId = "journalistB", roles = listOf("Journalist"))
    val copywriterA: Copywriter get() = Copywriter(userId = "copywriterA", roles = listOf("Copywriter"))
    val copywriterB: Copywriter get() = Copywriter(userId = "copywriterB", roles = listOf("Copywriter"))


    fun <R> asJournalist(journalist: Journalist = randomJournalist(), block: () -> R): R = `as`(journalist, block)
    fun <R> asCopywriter(copywriter: Copywriter = randomCopywriter(), block: () -> R): R = `as`(copywriter, block)
    fun <R> asJournalistA(block: () -> R): R = `as`(journalistA, block)
    fun <R> asJournalistB(block: () -> R): R = `as`(journalistB, block)
    fun <R> asCopywriterA(block: () -> R): R = `as`(copywriterA, block)
    fun <R> asCopywriterB(block: () -> R): R = `as`(copywriterB, block)


    fun <R> `as`(user: User, block: () -> R): R {
        val previousUser = testUserContext.currentUser()
        testUserContext.setUser(user)
        val result = block()
        testUserContext.setUser(previousUser)
        return result
    }
}