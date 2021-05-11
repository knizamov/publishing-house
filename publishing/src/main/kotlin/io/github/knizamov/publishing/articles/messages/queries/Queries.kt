package io.github.knizamov.publishing.articles.messages.queries

public sealed class ArticleQuery

public data class GetArticle(val articleId: String): ArticleQuery()