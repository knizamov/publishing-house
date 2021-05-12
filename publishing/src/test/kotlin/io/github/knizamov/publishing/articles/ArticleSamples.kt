package io.github.knizamov.publishing.articles

import io.github.knizamov.publishing.articles.messages.ArticleDto
import io.github.knizamov.publishing.articles.messages.commands.EditDraftArticle
import io.github.knizamov.publishing.articles.messages.commands.SubmitDraftArticle
import io.github.knizamov.publishing.articles.messages.commands.SuggestChange
import io.github.knizamov.publishing.articles.messages.events.ArticleEvent
import io.github.knizamov.publishing.base.TestEventPublisher
import io.github.knizamov.publishing.base.randomList
import io.github.serpro69.kfaker.Faker
import kotlin.properties.Delegates

private val faker: Faker = Faker()

// Note for a reviewer: This is just a Test Fixture implemented with kind of trait/mixin interface
// It's used to not repeat data setup. Often times we just need any valid data. It also provides predefined scenarios (like an draft article that was already submitted)
internal interface ArticleSamples {
    val facade: ArticleFacade
    val eventPublisher: TestEventPublisher<ArticleEvent>

    fun submittedDraftArticle(): ArticleDto {
        val article = facade(SubmitDraftArticle.random())
        eventPublisher.clear()
        return article
    }


    fun SubmitDraftArticle.Companion.random(): SubmitDraftArticle {
        return SubmitDraftArticle(
            title = faker.book.title(),
            text = faker.quote.matz(),
            topics = randomList { faker.animal.name() })
    }

    fun EditDraftArticle.Companion.random(articleId: String): EditDraftArticle {
        return EditDraftArticle(
            articleId = articleId,
            title = faker.book.title(),
            text = faker.quote.matz(),
            topics = randomList { faker.animal.name() })
    }

    fun SuggestChange.Companion.random(articleId: String): SuggestChange {
        return SuggestChange(articleId = articleId, comment = faker.quote.yoda())
    }
}

// Note for a reviewer: Inspired by AutoDsl https://github.com/juanchosaravia/autodsl which generates those builders automatically, unfortunately it does not support Kotlin 1.4
// I don't really need those builders right now since I don't have nested structures but I still wanted to showcase them
// Those builders are very helpful when have we have immutable data classes with nested structures that we need to modify
// Using .copy(...) quickly becomes verbose and inconvenient when one needs to modify deeply nested structures
// Alternatively, one could use  https://youtrack.jetbrains.com/issue/KT-44653 https://youtrack.jetbrains.com/issue/KT-44585 when it's implemented
operator fun EditDraftArticle.Companion.invoke(block: EditDraftArticleBuilder.() -> Unit) = builder(block)
fun EditDraftArticle.Companion.builder(block: EditDraftArticleBuilder.() -> Unit): EditDraftArticle {
    return EditDraftArticleBuilder().apply(block).build()
}

operator fun EditDraftArticle.invoke(block: EditDraftArticleBuilder.() -> Unit) = modify(block)
fun EditDraftArticle.modify(block: EditDraftArticleBuilder.() -> Unit): EditDraftArticle {
    val self = this
    return EditDraftArticleBuilder()
        .apply { id = self.articleId; title = self.title; text = self.text; topics = self.topics }
        .apply(block)
        .build()
}

@BuilderDslMarker
class EditDraftArticleBuilder {
    var id: String by Delegates.notNull()
    var title: String by Delegates.notNull()
    var text: String by Delegates.notNull()
    var topics: List<String> = emptyList()

    fun build() = EditDraftArticle(id, title, text, topics)

}

// SubmitDraftArticle
operator fun SubmitDraftArticle.Companion.invoke(block: SubmitDraftArticleBuilder.() -> Unit) = builder(block)
fun SubmitDraftArticle.Companion.builder(block: SubmitDraftArticleBuilder.() -> Unit): SubmitDraftArticle {
    return SubmitDraftArticleBuilder().apply(block).build()
}

operator fun SubmitDraftArticle.invoke(block: SubmitDraftArticleBuilder.() -> Unit) = modify(block)
fun SubmitDraftArticle.modify(block: SubmitDraftArticleBuilder.() -> Unit): SubmitDraftArticle {
    val self = this
    return SubmitDraftArticleBuilder()
        .apply { title = self.title; text = self.text; topics = self.topics }
        .apply(block)
        .build()
}

@BuilderDslMarker
class SubmitDraftArticleBuilder {
    var title: String by Delegates.notNull()
    var text: String by Delegates.notNull()
    var topics: List<String> = emptyList()

    fun build() = SubmitDraftArticle(title, text, topics)

}

@DslMarker
annotation class BuilderDslMarker
