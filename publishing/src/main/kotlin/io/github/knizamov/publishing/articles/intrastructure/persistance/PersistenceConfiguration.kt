package io.github.knizamov.publishing.articles.intrastructure.persistance

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
// Not for a reviewer: those will be replaced later by a proper repositories implementation (e.g. JPA) once the persistence is implemented
private open class PersistenceConfiguration {

    @Bean
    protected fun articles(): InMemoryArticles {
        return InMemoryArticles()
    }

    @Bean
    protected fun articleReviews(): InMemoryArticleReviews {
        return InMemoryArticleReviews()
    }

    @Bean
    protected fun changeSuggestions(): InMemoryChangeSuggestions {
        return InMemoryChangeSuggestions()
    }
}