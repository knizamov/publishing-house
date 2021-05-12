package io.github.knizamov.publishing.articles

import am.ik.yavi.builder.ValidatorBuilder
import am.ik.yavi.constraint.CharSequenceConstraint
import io.github.knizamov.publishing.articles.errors.ArticleDoesNotBelongToRequestedUser
import io.github.knizamov.publishing.articles.messages.ArticleDto
import io.github.knizamov.publishing.articles.messages.commands.EditDraftArticle
import io.github.knizamov.publishing.articles.messages.commands.PublishArticle
import io.github.knizamov.publishing.articles.messages.commands.SubmitDraftArticle
import io.github.knizamov.publishing.articles.messages.events.ArticleDraftCreated
import io.github.knizamov.publishing.articles.messages.events.ArticleDraftEdited
import io.github.knizamov.publishing.articles.messages.events.ArticleEvent
import io.github.knizamov.publishing.articles.messages.events.ArticlePublished
import io.github.knizamov.publishing.shared.AggregateRoot
import io.github.knizamov.publishing.shared.security.Journalist
import java.util.*


// Note for a reviewer: Although not necessarily, I experimentally used Events to do state mutations
// (without actual event sourcing, the aggregate could still be stored in a normal DB). This has multiple advantages:
// - It guarantees that the State of an Aggregate and Events is synchronized, Events are source of truth for the State, it cannot accidentally diverge (unless someone bypass it and mutates the state in DB)
// - It provides Completeness Guarantee (https://verraes.net/2019/05/patterns-for-decoupling-distsys-completeness-guarantee/)
// - Events are modelled from the beginning, it also makes it easier to switch to event sourcing later
internal class Article private constructor(
    val id: ArticleId = ArticleId(),
): AggregateRoot<ArticleEvent>() {
    private lateinit var title: Title
    private lateinit var text: Text
    private var topics: MutableList<TopicId> = mutableListOf()
    private lateinit var status: Status
    private lateinit var journalistUserId: String

    companion object {
        fun draft(command: SubmitDraftArticle, journalist: Journalist): Article {
            val article = Article()
            article.draft(command, journalist)
            return article
        }
    }
    public fun draft(command: SubmitDraftArticle, journalist: Journalist) {
        apply(ArticleDraftCreated(id = id.asString(), title = command.title, text = command.text, topics = command.topics, journalistUserId = journalist.userId))
    }
    private fun on(event: ArticleDraftCreated) {
        this.status = Status.DRAFT
        this.title = Title(event.title)
        this.text = Text(event.text)
        this.topics.addAll(event.topics.map { TopicId(it) })
        this.journalistUserId = event.journalistUserId
    }

    // Alternatively, we could split EditDraftArticle into more granular commands
    fun edit(command: EditDraftArticle, journalist: Journalist) {
        assertArticleBelongsTo(journalist)

        apply(ArticleDraftEdited(id = id.asString(), title = command.title, text = command.text, topics = command.topics))
    }
    private fun on(event: ArticleDraftEdited) {
        this.title = Title(event.title)
        this.text = Text(event.text)
        this.topics.clear()
        this.topics.addAll(event.topics.map { TopicId(it) })
    }

    private fun assertArticleBelongsTo(journalist: Journalist) {
        if (this.journalistUserId != journalist.userId) {
            throw ArticleDoesNotBelongToRequestedUser(articleId = this.id.asString(), userId = journalist.userId)
        }
    }

    fun publish(command: PublishArticle, publishingPolicy: PublishingPolicy, journalist: Journalist) {
        assertArticleBelongsTo(journalist)
        publishingPolicy.isSatisfied(this)
            .throwIfFailed()

        apply(ArticlePublished(id = id.asString(), title = title.asString(), text = text.asString(), topics = topics.map { it.asString() }, journalistUserId = journalistUserId))
    }
    private fun on(event: ArticlePublished) {
        this.status = Status.PUBLISHED
    }

    public fun toDto(): ArticleDto {
        return ArticleDto(id = id.asString(), title = title.asString(), text = text.asString(), topics = topics.map { it.asString() }, status = status.toString(), journalistUserId = journalistUserId)
    }

    override fun on(event: ArticleEvent) {
        val exhaustive = when (event) {
            is ArticleDraftCreated -> on(event)
            is ArticleDraftEdited -> on(event)
            is ArticlePublished -> on(event)
        }
    }

    private enum class Status {
        DRAFT,
        PUBLISHED
    }
}

internal data class ArticleId(val value: String = UUID.randomUUID().toString()) {
    fun asString() = value
}

internal data class Title(val value: String) {
    init { validator.validateAndThrowIfInvalid(value) }

    fun asString() = value

    companion object {
        fun <T> CharSequenceConstraint<T, String?>.titleConstraints() =
            notBlank().lessThanOrEqual(200)

        private val validator = ValidatorBuilder.of<String?>()
            .constraint(String::toString, "title") { it.titleConstraints() }
            .build()
    }
}

internal data class Text(val value: String) {
    init { validator.validateAndThrowIfInvalid(value) }

    fun asString() = value

    companion object {
        fun <T> CharSequenceConstraint<T, String?>.textConstraints() =
            notBlank()

        private val validator = ValidatorBuilder.of<String?>()
            .constraint(String::toString, "text") { it.textConstraints() }
            .build()
    }
}

internal data class TopicId(val value: String) {
    init { validator.validateAndThrowIfInvalid(value) }

    fun asString() = value

    companion object {
        fun <T> CharSequenceConstraint<T, String?>.topicConstrainsts() =
            notBlank()

        val validator = ValidatorBuilder.of<String?>()
            .constraint(String::toString, "topic") { it.topicConstrainsts() }
            .build()
    }
}




