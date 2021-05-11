package io.github.knizamov.publishing.base

import io.github.knizamov.publishing.shared.EventPublisher

internal class TestEventPublisher<DomainEvent> : EventPublisher<DomainEvent>
        where DomainEvent : io.github.knizamov.publishing.shared.DomainEvent {
    val events: MutableList<DomainEvent> = mutableListOf()

    override fun publish(event: DomainEvent) {
        events.add(event)
    }

    inline fun <reified T : DomainEvent> get(index: Int): T {
        val event: T? = getOrNull(index)

        assert(event != null) { "${T::class.java.simpleName} was not published at $index position" }
        return event!!
    }

    inline fun <reified T : DomainEvent> getOrNull(index: Int): T? {
        val event: DomainEvent? = events.getOrNull(index)
        if (event == null) {
            return null
        }

        assert(event is T) { "Expected ${T::class.java.simpleName} at $index position but actual event is ${event.javaClass.simpleName}" }
        return event as T
    }

    fun clear() {
        events.clear()
    }
}