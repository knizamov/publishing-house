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
import io.github.knizamov.publishing.articles.messages.commands.EditDraftArticle
import io.github.knizamov.publishing.articles.messages.commands.SubmitDraftArticle
import io.github.knizamov.publishing.articles.messages.queries.GetArticle
import io.github.knizamov.publishing.shared.authentication.Journalist
import io.github.knizamov.publishing.shared.authentication.UserContext
import io.github.knizamov.publishing.shared.authentication.assumeRole

public class ArticleFacade internal constructor(
    private val articles: Articles,
    private val userContext: UserContext,
) {
    // Commands
    public operator fun invoke(command: SubmitDraftArticle): ArticleDto {
        summitDraftArticleCommandValidator.validateAndThrowIfInvalid(command)
        val journalist = userContext.assumeRole<Journalist>()

        val draftArticle = Article.draft(command, journalist)
        val savedArticle = articles.save(draftArticle)

        return savedArticle.toDto()
    }

    public operator fun invoke(command: EditDraftArticle): ArticleDto {
        editDraftArticleCommandValidator.validateAndThrowIfInvalid(command)
        val journalist = userContext.assumeRole<Journalist>()

        val draftArticle = articles.getById(ArticleId(command.articleId))
        draftArticle.edit(command, journalist)
        val savedArticle = articles.save(draftArticle)

        return savedArticle.toDto()
    }


    // Queries
    public operator fun invoke(query: GetArticle): ArticleDto {
        return articles.getById(ArticleId(query.articleId)).toDto()
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

