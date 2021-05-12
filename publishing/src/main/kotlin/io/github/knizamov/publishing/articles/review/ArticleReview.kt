package io.github.knizamov.publishing.articles.review

import io.github.knizamov.publishing.articles.ArticleId
import io.github.knizamov.publishing.articles.errors.CopywriterNotAssignedToReviewArticle
import io.github.knizamov.publishing.articles.messages.commands.SuggestChange
import io.github.knizamov.publishing.shared.authentication.Copywriter
import java.time.Instant
import java.util.*

internal class ArticleReview private constructor(
    public val articleId: ArticleId,
    private var copywriterUserId: String? = null,
) {

    companion object {
        fun begin(articleId: ArticleId): ArticleReview {
            return ArticleReview(articleId)
        }
    }

    fun assignCopywriter(userId: String) {
        this.copywriterUserId = userId
    }

    fun suggestChange(command: SuggestChange, copywriter: Copywriter): ChangeSuggestion {
        assertCopywriterIsAssignedToReviewArticle(copywriter)

        return ChangeSuggestion.new(command, copywriter)
    }

    private fun assertCopywriterIsAssignedToReviewArticle(copywriter: Copywriter) {
        if (this.copywriterUserId != copywriter.userId) {
            throw CopywriterNotAssignedToReviewArticle(articleId = articleId.asString(), userId = copywriter.userId)
        }
    }
}

internal class ChangeSuggestion private constructor(
    public val id: String = UUID.randomUUID().toString(),
    private val articleId: ArticleId,
    private val copywriterUserId: String,
    private val createdAt: Instant = Instant.now(),
    private var comment: String,
    private var status: Status,
) {

    companion object {
        fun new(command: SuggestChange, copywriter: Copywriter): ChangeSuggestion {
            return ChangeSuggestion(comment = command.comment, articleId = ArticleId(command.articleId), copywriterUserId = copywriter.userId, status = Status.UNRESOLVED)
        }
    }

    fun toDto(): ChangeSuggestionDto {
        return ChangeSuggestionDto(id = id, articleId = this.articleId.asString(), copywriterUserId = copywriterUserId, comment = comment, createdAt = createdAt, status = status.toString())
    }

    private enum class Status {
        UNRESOLVED,
        APPLIED,
        RESOLVED
    }
}

public data class ChangeSuggestionDto(
    val id: String,
    val articleId: String,
    val copywriterUserId: String,
    val comment: String,
    val createdAt: Instant,
    val status: String,
)