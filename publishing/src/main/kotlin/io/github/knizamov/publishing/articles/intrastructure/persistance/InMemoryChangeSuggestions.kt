package io.github.knizamov.publishing.articles.intrastructure.persistance

import io.github.knizamov.publishing.articles.ArticleId
import io.github.knizamov.publishing.articles.review.ArticleReview
import io.github.knizamov.publishing.articles.review.ChangeSuggestion
import io.github.knizamov.publishing.articles.review.ChangeSuggestions
import java.util.concurrent.ConcurrentHashMap


internal class InMemoryChangeSuggestions: ChangeSuggestions {
    private val store: MutableMap<String, ChangeSuggestion> = ConcurrentHashMap()

    override fun save(changeSuggestion: ChangeSuggestion): ChangeSuggestion {
        store[changeSuggestion.id] = changeSuggestion
        return changeSuggestion
    }

    override fun findByArticleId(articleId: ArticleId): List<ChangeSuggestion> {
        return store.values.filter { it.toDto().articleId == articleId.asString() }
    }
}