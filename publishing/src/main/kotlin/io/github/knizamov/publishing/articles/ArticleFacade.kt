package io.github.knizamov.publishing.articles

import am.ik.yavi.builder.ValidatorBuilder
import am.ik.yavi.builder.forEach
import am.ik.yavi.builder.konstraint
import am.ik.yavi.constraint.CollectionConstraint
import am.ik.yavi.core.ConstraintViolationsException
import am.ik.yavi.core.Validator
import io.github.knizamov.publishing.articles.Text.Companion.textConstraints
import io.github.knizamov.publishing.articles.Title.Companion.titleConstraints
import io.github.knizamov.publishing.articles.messages.ArticleDto
import io.github.knizamov.publishing.articles.messages.commands.*
import io.github.knizamov.publishing.articles.messages.queries.GetArticle
import io.github.knizamov.publishing.articles.messages.queries.GetChangeSuggestions
import io.github.knizamov.publishing.articles.review.ArticleReviewing
import io.github.knizamov.publishing.articles.review.ChangeSuggestionDto
import io.github.knizamov.publishing.shared.security.Journalist
import io.github.knizamov.publishing.shared.security.UserContext
import io.github.knizamov.publishing.shared.security.assumeRole


// Not for a reviewer: I know an interface is not needed, but I wanted to reuse the same Unit Test for Integration tests
// that's why I also created RemoteMockMvcArticleFacade (see ArticlesIntegrationSpec for more details)

// I generally follow Hexagonal Architecture (although not sure about it fully whether it's a good fit)
// and I was heavily inspired by Jakub Nabrdalik's talks: Keep It Clean and Improving your Test Driven Development
// I try to keep an eye on visibility modifiers and module boundaries. Modules could be extracted into a separate gradle project to enforce internal visibility
public interface ArticleFacade {
    public fun submitDraftArticle(command: SubmitDraftArticle): ArticleDto
    public fun editDraftArticle(command: EditDraftArticle): ArticleDto
    public fun publishArticle(command: PublishArticle): ArticleDto
    public fun assignCopywriterToArticle(command: AssignCopywriterToArticle)
    public fun suggestChange(command: SuggestChange)

    public fun getArticle(query: GetArticle): ArticleDto
    public fun getChangeSuggestions(query: GetChangeSuggestions): List<ChangeSuggestionDto>
}

internal class LocalArticleFacade internal constructor(
    private val articles: Articles,
    private val articleReviewing: ArticleReviewing,
    private val publishingPolicyFactory: PublishingPolicyFactory,
    private val userContext: UserContext,
) : ArticleFacade {

    override fun submitDraftArticle(command: SubmitDraftArticle): ArticleDto {
        summitDraftArticleCommandValidator.validateAndThrowIfInvalid(command)
        val journalist = userContext.assumeRole<Journalist>()

        val draftArticle = Article.draft(command, journalist)
        val savedArticle = articles.save(draftArticle)

        articleReviewing.beginArticleReviewing(savedArticle.id)

        return savedArticle.toDto()
    }

    override fun editDraftArticle(command: EditDraftArticle): ArticleDto {
        editDraftArticleCommandValidator.validateAndThrowIfInvalid(command)
        val journalist = userContext.assumeRole<Journalist>()

        val draftArticle = articles.getById(ArticleId(command.articleId))
        draftArticle.edit(command, journalist)
        val savedArticle = articles.save(draftArticle)

        return savedArticle.toDto()
    }

    override fun publishArticle(command: PublishArticle): ArticleDto {
        val journalist = userContext.assumeRole<Journalist>()

        val article = articles.getById(ArticleId(command.articleId))
        val publishingPolicy: PublishingPolicy = publishingPolicyFactory.create()
        article.publish(command, publishingPolicy, journalist)
        val savedArticle = articles.save(article)

        articleReviewing.closeArticleReviewing(article.id)

        return savedArticle.toDto()
    }

    override fun assignCopywriterToArticle(command: AssignCopywriterToArticle) {
        articleReviewing.assignCopywriter(command)
    }

    override fun suggestChange(command: SuggestChange) {
        articleReviewing.suggestChange(command)
    }


    override fun getArticle(query: GetArticle): ArticleDto {
        return articles.getById(ArticleId(query.articleId)).toDto()
    }

    override fun getChangeSuggestions(query: GetChangeSuggestions): List<ChangeSuggestionDto> {
        return articleReviewing.getChangeSuggestions(query).map { it.toDto() }
    }
}

internal fun <T : Any> Validator<T>.validateAndThrowIfInvalid(target: T) {
    this.validate(target).throwIfInvalid(::ConstraintViolationsException)
}

private val summitDraftArticleCommandValidator = ValidatorBuilder.of<SubmitDraftArticle>()
    .konstraint(SubmitDraftArticle::title) { titleConstraints() }
    .konstraint(SubmitDraftArticle::text) { textConstraints() }
    .konstraint(SubmitDraftArticle::topics) { topicCollectionConstraints() }
    .forEach(SubmitDraftArticle::topics) { TopicId.validator }
    .build()

private val editDraftArticleCommandValidator = ValidatorBuilder.of<EditDraftArticle>()
    .konstraint(EditDraftArticle::title) { titleConstraints() }
    .konstraint(EditDraftArticle::text) { textConstraints() }
    .konstraint(EditDraftArticle::topics) { topicCollectionConstraints() }
    .forEach(EditDraftArticle::topics) { TopicId.validator }
    .build()

private fun <T, L : Collection<String>?> CollectionConstraint<T, L, String>.topicCollectionConstraints() {
    greaterThanOrEqual(1)
}

