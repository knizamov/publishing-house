package io.github.knizamov.publishing.articles

import io.github.knizamov.publishing.articles.errors.ArticleNotFound

internal interface Articles {
    fun save(article: Article): Article
    fun findById(id: ArticleId): Article?
    fun getById(id: ArticleId): Article = findById(id) ?: throw ArticleNotFound(articleId = id.asString())
}
