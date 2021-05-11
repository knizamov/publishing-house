package io.github.knizamov.publishing.shared


public interface EventPublisher<T: DomainEvent> {
    public fun publish(event: T)

    public fun publish(events: List<T>) {
        for (event in events) {
            publish(event)
        }
    }
}