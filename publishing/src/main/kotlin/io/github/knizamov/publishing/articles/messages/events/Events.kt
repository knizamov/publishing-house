package io.github.knizamov.publishing.articles.messages.events

import io.github.knizamov.publishing.shared.DomainEvent

public sealed class ArticleEvent: DomainEvent
internal data class ArticleDraftCreated(val id: String, val title: String, val text: String, val topics: List<String>, val journalistUserId: String) : ArticleEvent()
internal data class ArticleDraftEdited(val id: String, val title: String, val text: String, val topics: List<String>) : ArticleEvent()