package io.github.knizamov.publishing.shared

public abstract class AggregateRoot<DomainEvent> where DomainEvent : io.github.knizamov.publishing.shared.DomainEvent {
    private val _domainEvents: MutableList<DomainEvent> = mutableListOf()
    public val domainEvents: List<DomainEvent>
        get() = _domainEvents

    public fun clearDomainEvents() {
        _domainEvents.clear()
    }

    protected fun apply(event: DomainEvent) {
        registerEvent(event)
        on(event)
    }

    private fun registerEvent(event: DomainEvent) {
        _domainEvents.add(event)
    }

    protected abstract fun on(event: DomainEvent)
}