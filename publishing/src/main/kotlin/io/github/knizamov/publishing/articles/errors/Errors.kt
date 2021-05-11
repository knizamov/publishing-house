package io.github.knizamov.publishing.articles.errors

import io.github.knizamov.publishing.shared.authentication.AuthError


public class ArticleDoesNotBelongToRequestedUser(
    public val articleId: String,
    override val userId: String,
    override val message: String = "Article $articleId does not belong to user $userId",
    override val cause: Throwable? = null,
) : AuthError.Unauthorized()

