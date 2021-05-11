package io.github.knizamov.publishing.articles.messages

public data class ArticleDto(val id: String,
                             val title: String,
                             val text: String,
                             val topics: List<String>,
                             val status: String,
                             val journalistUserId: String)