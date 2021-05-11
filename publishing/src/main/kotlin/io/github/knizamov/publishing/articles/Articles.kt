package io.github.knizamov.publishing.articles

internal interface Articles {
    fun save(article: Article): Article
    fun findById(id: String): Article?
}
