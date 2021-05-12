package io.github.knizamov.publishing.articles.intrastructure.persistance

import io.github.knizamov.publishing.articles.Article
import io.github.knizamov.publishing.articles.ArticleId
import io.github.knizamov.publishing.articles.review.ArticleReview
import io.github.knizamov.publishing.articles.review.ArticleReviews
import java.util.concurrent.ConcurrentHashMap

internal class InMemoryArticleReviews: ArticleReviews {
    private val store: MutableMap<ArticleId, ArticleReview> = ConcurrentHashMap()

    override fun save(articleReview: ArticleReview): ArticleReview {
        store[articleReview.articleId] = articleReview
        return articleReview
    }

    override fun findByArticleId(articleId: ArticleId): ArticleReview? {
        return store[articleId]
    }
}