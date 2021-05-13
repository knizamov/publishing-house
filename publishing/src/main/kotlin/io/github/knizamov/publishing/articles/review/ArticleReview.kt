package io.github.knizamov.publishing.articles.review

import io.github.knizamov.publishing.articles.ArticleId
import io.github.knizamov.publishing.articles.errors.ArticleReviewClosed
import io.github.knizamov.publishing.articles.errors.CopywriterNotAssignedToReviewArticle
import io.github.knizamov.publishing.articles.messages.commands.SuggestChange
import io.github.knizamov.publishing.shared.security.Copywriter
import java.time.Instant
import java.util.*

// Note for a reviewer: Not really sure about this approach, but my reasoning behind Change Suggestion requirements is that
// it's pretty similar to normal comments. And storing potentially a huge number of comments under the Article aggregate doesn't seems wise and might cause contention issues
// The reason for separating ArticleReview class is that I tried to respect potential Content/Article Review boundaries
// that I sketched during my analysis (see readme) but now that I look at it, maybe ArticleReview could be merged into Article aggregate
// but ChangeSuggestion and comments should still be handled separately
internal class ArticleReview private constructor(
    public val articleId: ArticleId,
    private var status: Status,
    private var copywriterUserId: String? = null,
) {

    companion object {
        fun begin(articleId: ArticleId): ArticleReview {
            return ArticleReview(articleId = articleId, status = Status.OPEN)
        }
    }

    fun assignCopywriter(userId: String) {
        this.copywriterUserId = userId
    }

    fun suggestChange(command: SuggestChange, copywriter: Copywriter): ChangeSuggestion {
        assertCopywriterIsAssignedToReviewArticle(copywriter)
        assertReviewNotClosed()

        return ChangeSuggestion.new(command, copywriter)
    }

    private fun assertReviewNotClosed() {
        if (this.status == Status.CLOSED) {
            throw ArticleReviewClosed(articleId = articleId.asString())
        }
    }

    fun close() {
        this.status = Status.CLOSED
    }

    private fun assertCopywriterIsAssignedToReviewArticle(copywriter: Copywriter) {
        if (this.copywriterUserId != copywriter.userId) {
            throw CopywriterNotAssignedToReviewArticle(articleId = articleId.asString(), userId = copywriter.userId)
        }
    }

    private enum class Status {
        OPEN,
        CLOSED
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

    fun isUnresolved() = status == Status.UNRESOLVED
    fun isApplied() = status == Status.APPLIED
    fun isResolved() = status == Status.RESOLVED


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