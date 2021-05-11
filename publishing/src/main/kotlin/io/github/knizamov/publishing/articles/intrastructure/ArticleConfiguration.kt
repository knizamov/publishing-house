package io.github.knizamov.publishing.articles.intrastructure

import io.github.knizamov.publishing.articles.*
import io.github.knizamov.publishing.articles.Article
import io.github.knizamov.publishing.articles.messages.events.ArticleEvent
import io.github.knizamov.publishing.articles.Articles
import io.github.knizamov.publishing.articles.intrastructure.persistance.InMemoryArticles
import io.github.knizamov.publishing.shared.DomainEvent
import io.github.knizamov.publishing.shared.EventPublisher
import io.github.knizamov.publishing.shared.authentication.UserContext

internal class ArticleConfiguration {

    internal fun inMemoryArticleFacade(
        eventPublisher: EventPublisher<ArticleEvent>,
        userContext: UserContext,
    ): ArticleFacade {
        val inMemoryArticles = InMemoryArticles()
        return createArticleFacade(
            articles = inMemoryArticles,
            eventPublisher = eventPublisher,
            userContext = userContext)
    }

    private fun createArticleFacade(
        articles: Articles,
        eventPublisher: EventPublisher<ArticleEvent>,
        userContext: UserContext,
    ): ArticleFacade {
        val eventPublishingRepository: Articles = EventPublishingRepository(repository = articles, eventPublisher = eventPublisher as EventPublisher<DomainEvent>)
        return ArticleFacade(articles = eventPublishingRepository, userContext = userContext)
    }
}


internal class EventPublishingRepository(
    private val repository: Articles,
    private val eventPublisher: EventPublisher<DomainEvent>,
) : Articles by repository {
    override fun save(article: Article): Article {
        eventPublisher.publish(article.domainEvents)
        article.clearDomainEvents()
        return repository.save(article)
    }
}

