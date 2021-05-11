package io.github.knizamov.publishing.base

import kotlin.random.Random

fun <T> randomList(min: Int = 1, max: Int = 5, mapper: () -> T): List<T> {
    return (1..Random.nextInt(min, max)).map { mapper() }
}