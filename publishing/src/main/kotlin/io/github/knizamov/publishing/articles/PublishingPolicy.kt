package io.github.knizamov.publishing.articles

import io.github.knizamov.publishing.articles.errors.PublishingPolicyNotSatisfied
import io.github.knizamov.publishing.articles.messages.queries.GetChangeSuggestions
import io.github.knizamov.publishing.articles.review.ArticleReviewing

// Note for a reviewer: Introducing an interface was not necessarily for the given requirements but I wanted to show a strategy pattern in domain code
// This would be useful if we had more checks before publishing. Factory could provide some dynamic or static configuration.
// There could be CompositePublishingPolicy running multiple checks possibly asynchronously, or in some specific sequence, etc.
// And the code needs to know only about PublishingPolicy interface without the need to change it with new rules.
internal interface PublishingPolicy {
    fun isSatisfied(article: Article): Result

    sealed class Result {
        open val policyName: String = this::class.simpleName!!
        abstract val articleId: ArticleId;

        class Passed(override val articleId: ArticleId): Result()
        class Failed(val reason: String, override val articleId: ArticleId): Result()

        fun throwIfFailed() {
            if (this is Failed) {
                throw PublishingPolicyNotSatisfied(articleId = articleId.asString(), reason = this.reason)
            }
        }
    }

}

internal class PublishingPolicyFactory(
    private val articleReviewing: ArticleReviewing
) {

    fun create(): PublishingPolicy {
        return AllChangeSuggestionsResolvedPublishingPolicy(articleReviewing)
    }
}

internal class AllChangeSuggestionsResolvedPublishingPolicy(
    private val articleReviewing: ArticleReviewing
): PublishingPolicy {

    override fun isSatisfied(article: Article): PublishingPolicy.Result {
        val changeSuggestions = articleReviewing.getChangeSuggestions(query = GetChangeSuggestions(article.id.asString()))
        val allChangeSuggestionsResolved = changeSuggestions.all { it.isResolved() }

        return if (allChangeSuggestionsResolved) {
            PublishingPolicy.Result.Passed(articleId = article.id)
        } else {
            PublishingPolicy.Result.Failed(articleId = article.id, reason = "Article has unresolved change suggestions")
        }
    }
}