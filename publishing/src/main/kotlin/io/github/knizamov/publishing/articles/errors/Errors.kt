package io.github.knizamov.publishing.articles.errors

import io.github.knizamov.publishing.shared.security.AuthError
import io.github.knizamov.publishing.shared.errors.Error


public class ArticleNotFound(
    public val articleId: String,
    override val message: String = "Article $articleId not found",
    override val cause: Throwable? = null,
) : Error()

public class PublishingPolicyNotSatisfied(
    public val articleId: String,
    public val reason: String,
    override val message: String = reason,
    override val cause: Throwable? = null
): Error()

public class ArticleDoesNotBelongToRequestedUser(
    public val articleId: String,
    override val userId: String,
    override val message: String = "Article $articleId does not belong to user $userId",
    override val cause: Throwable? = null,
) : AuthError.Unauthorized()


public class CopywriterNotAssignedToReviewArticle(
    public val articleId: String,
    override val userId: String,
    override val message: String = "Copywriter $userId is not assigned to review article $articleId",
    override val cause: Throwable? = null,
): AuthError.Unauthorized()

public class ArticleReviewClosed(
    public val articleId: String,
    override val message: String = "Article review for $articleId is closed",
    override val cause: Throwable? = null,
): Error()