package io.github.knizamov.publishing.articles.intrastructure.persistance

import io.github.knizamov.publishing.articles.Article
import io.github.knizamov.publishing.articles.Articles
import java.util.concurrent.ConcurrentHashMap

internal class InMemoryArticles: Articles {
    private val store: MutableMap<String, Article> = ConcurrentHashMap()

    override fun save(article: Article): Article {
        store[article.id.asString()] = article
        return article
    }

    override fun findById(id: String): Article? {
        return store[id]
    }
}