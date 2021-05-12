package io.github.knizamov.publishing.articles.review

import io.github.knizamov.publishing.articles.ArticleId

internal interface ChangeSuggestions {
    fun save(changeSuggestion: ChangeSuggestion): ChangeSuggestion
    fun findByArticleId(articleId: ArticleId): List<ChangeSuggestion>
}