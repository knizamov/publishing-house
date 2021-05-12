package io.github.knizamov.publishing.instrastructure.eventpublisher

import io.github.knizamov.publishing.shared.DomainEvent
import io.github.knizamov.publishing.shared.EventPublisher
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
private open class EventPublisherConfiguration {

    @Bean
    protected fun eventPublisher(applicationEventPublisher: ApplicationEventPublisher): EventPublisher<DomainEvent> {
        return SpringEventPublisher(applicationEventPublisher)
    }

    class SpringEventPublisher(private val applicationEventPublisher: ApplicationEventPublisher) :
        EventPublisher<DomainEvent> {

        override fun publish(event: DomainEvent) {
            applicationEventPublisher.publishEvent(event)
        }
    }
}