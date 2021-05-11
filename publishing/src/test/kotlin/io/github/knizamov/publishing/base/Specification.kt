package io.github.knizamov.publishing.base

import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

internal open class Specification {
}

// Just labels for BDD style scenarios with nicer yellow highlighting of extension methods by IDEA
internal fun Specification.Given(description: String) = Unit
internal fun Specification.When(description: String) = Unit
internal fun Specification.Then(description: String) = Unit
internal fun Specification.And(description: String) = Unit
internal fun Specification.Where(block: WhereArguments.() -> Unit): Stream<Arguments> {
    val arguments = mutableListOf<Arguments>()
    block(WhereArguments(arguments))
    return arguments.stream()
}

internal class WhereArguments(
    private val arguments: MutableList<Arguments>,
) {
    fun of(vararg arguments: Any?) {
        this.arguments.add(Arguments.of(*arguments));
    }
}