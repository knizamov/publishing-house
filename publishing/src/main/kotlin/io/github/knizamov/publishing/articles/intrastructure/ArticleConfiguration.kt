package io.github.knizamov.publishing.articles.intrastructure

import io.github.knizamov.publishing.articles.*
import io.github.knizamov.publishing.articles.Article
import io.github.knizamov.publishing.articles.messages.events.ArticleEvent
import io.github.knizamov.publishing.articles.Articles
import io.github.knizamov.publishing.articles.intrastructure.persistance.InMemoryArticleReviews
import io.github.knizamov.publishing.articles.intrastructure.persistance.InMemoryArticles
import io.github.knizamov.publishing.articles.intrastructure.persistance.InMemoryChangeSuggestions
import io.github.knizamov.publishing.articles.review.ArticleReviewing
import io.github.knizamov.publishing.articles.review.ArticleReviews
import io.github.knizamov.publishing.articles.review.ChangeSuggestions
import io.github.knizamov.publishing.shared.DomainEvent
import io.github.knizamov.publishing.shared.EventPublisher
import io.github.knizamov.publishing.shared.security.UserContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class ArticleConfiguration {

    internal fun inMemoryArticleFacade(
        eventPublisher: EventPublisher<ArticleEvent>,
        userContext: UserContext,
    ): ArticleFacade {
        return springArticleFacade(
            articles = InMemoryArticles(),
            articleReviews = InMemoryArticleReviews(),
            changeSuggestions = InMemoryChangeSuggestions(),
            eventPublisher = eventPublisher as EventPublisher<DomainEvent>,
            userContext = userContext
        )
    }

    @Bean
    protected fun springArticleFacade(
        articles: Articles,
        articleReviews: ArticleReviews,
        changeSuggestions: ChangeSuggestions,
        eventPublisher: EventPublisher<DomainEvent>,
        userContext: UserContext,
    ): ArticleFacade {
        val articleReviewing = ArticleReviewing(articleReviews, changeSuggestions, userContext)
        return LocalArticleFacade(
            articles = EventPublishingRepository(repository = articles, eventPublisher = eventPublisher),
            articleReviewing = articleReviewing,
            publishingPolicyFactory = PublishingPolicyFactory(articleReviewing),
            userContext = userContext)
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

