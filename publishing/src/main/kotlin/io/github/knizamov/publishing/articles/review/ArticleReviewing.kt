package io.github.knizamov.publishing.articles.review

import io.github.knizamov.publishing.articles.ArticleId
import io.github.knizamov.publishing.articles.messages.commands.AssignCopywriterToArticle
import io.github.knizamov.publishing.articles.messages.commands.SuggestChange
import io.github.knizamov.publishing.articles.messages.queries.GetChangeSuggestions
import io.github.knizamov.publishing.shared.authentication.Copywriter
import io.github.knizamov.publishing.shared.authentication.UserContext
import io.github.knizamov.publishing.shared.authentication.assumeRole

internal class ArticleReviewing internal constructor(
    private val articleReviews: ArticleReviews,
    private val changeSuggestions: ChangeSuggestions,
    private val userContext: UserContext,
) {

    internal fun beginArticleReviewing(articleId: ArticleId) {
        val articleReview = articleReviews.findByArticleId(articleId)
        if (articleReview == null) {
            val startedArticleReview = ArticleReview.begin(articleId)
            articleReviews.save(startedArticleReview)
        }
    }

    internal fun assignCopywriter(command: AssignCopywriterToArticle) {
        val articleReview = articleReviews.getByArticleId(ArticleId(command.articleId))
        articleReview.assignCopywriter(command.copywriterUserId)
    }

    internal fun suggestChange(command: SuggestChange) {
        val copywriter = userContext.assumeRole<Copywriter>()

        val articleReview = articleReviews.getByArticleId(ArticleId(command.articleId))
        val changeSuggestion = articleReview.suggestChange(command, copywriter)

        changeSuggestions.save(changeSuggestion)
    }

    internal fun getChangeSuggestions(query: GetChangeSuggestions): List<ChangeSuggestionDto> {
        return this.changeSuggestions.findByArticleId(ArticleId(query.articleId))
            .map { it.toDto() }
    }
}