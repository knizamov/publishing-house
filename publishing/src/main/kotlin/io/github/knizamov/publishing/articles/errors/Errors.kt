package io.github.knizamov.publishing.articles.errors

import io.github.knizamov.publishing.shared.authentication.AuthError
import io.github.knizamov.publishing.shared.errors.Error


public class ArticleNotFound(
    public val articleId: String,
    override val message: String = "Article $articleId not found",
    override val cause: Throwable? = null,
) : Error()

public class ArticleDoesNotBelongToRequestedUser(
    public val articleId: String,
    override val userId: String,
    override val message: String = "Article $articleId does not belong to user $userId",
    override val cause: Throwable? = null,
) : AuthError.Unauthorized()

