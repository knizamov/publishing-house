package io.github.knizamov.publishing.articles.review

import io.github.knizamov.publishing.articles.ArticleId
import java.lang.RuntimeException

internal interface ArticleReviews {
    fun findByArticleId(articleId: ArticleId): ArticleReview?
    fun getByArticleId(articleId: ArticleId): ArticleReview = findByArticleId(articleId) ?: throw RuntimeException()
    fun save(articleReview: ArticleReview): ArticleReview
}