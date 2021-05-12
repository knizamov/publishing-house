package io.github.knizamov.publishing.articles

import am.ik.yavi.core.ConstraintViolationsException
import io.github.knizamov.publishing.articles.errors.*
import io.github.knizamov.publishing.articles.intrastructure.ArticleConfiguration
import io.github.knizamov.publishing.articles.messages.ArticleDto
import io.github.knizamov.publishing.articles.messages.commands.*
import io.github.knizamov.publishing.articles.messages.events.ArticleDraftCreated
import io.github.knizamov.publishing.articles.messages.events.ArticleDraftEdited
import io.github.knizamov.publishing.articles.messages.events.ArticleEvent
import io.github.knizamov.publishing.articles.messages.queries.GetArticle
import io.github.knizamov.publishing.articles.messages.queries.GetChangeSuggestions
import io.github.knizamov.publishing.base.*
import io.github.knizamov.publishing.base.And
import io.github.knizamov.publishing.base.Given
import io.github.knizamov.publishing.base.Specification
import io.github.knizamov.publishing.base.TestEventPublisher
import io.github.knizamov.publishing.base.TestUserContext
import io.github.knizamov.publishing.base.Then
import io.github.knizamov.publishing.base.When
import io.github.knizamov.publishing.shared.authentication.Journalist
import io.github.knizamov.publishing.shared.authentication.AuthError
import io.github.knizamov.publishing.shared.authentication.Copywriter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.lang.RuntimeException
import java.time.Instant
import java.util.*

internal class ArticlesSpec : Specification(),
    ArticleSamples, UserSamples {

    override lateinit var eventPublisher: TestEventPublisher<ArticleEvent>
    override lateinit var testUserContext: TestUserContext
    override lateinit var facade: ArticleFacade

    @BeforeEach
    fun setUp() {
        this.eventPublisher = TestEventPublisher()
        this.testUserContext = TestUserContext(defaultUser = journalistA)
        this.facade = ArticleConfiguration().inMemoryArticleFacade(eventPublisher, testUserContext)
    }

    @Test
    fun `Article drafting, reviewing and publishing acceptance scenario`() {
        When("A journalist submits a draft article (title, text, topics)")
        Then("The draft article is created with the provided content and can be viewed")

        Given("A copywriter is assigned to the article")
        When("The copywriter suggests a change to the article")
        Then("The change suggestion is attached to the given article and can be viewed")

        When("A journalist tries to publish the article")
        Then("The operation is rejected due to unresolved change suggestions")

        When("The journalist views the article change suggestions")
        Then("They can see the new change suggestion")

        When("The journalist makes the change suggestion by editing the article")
        Then("The article is edited with the provided content")
        When("The journalist marks the change suggestion as applied")
        Then("The change suggestion is marked and can be viewed as applied")

        When("The copywriter reviews the applied change suggestion")
        And("Marks it as resolved")
        Then("The change suggestion is marked and can be viewed as resolved")

        When("The journalist publishes the article")
        Then("The article is published (status is changed)")
    }

    // Story 2: Journalist submits article drafts
    // Submit vs create a draft article. Is there something in between summiting and creating a draft?
    // Can an article be connected to a non-existent topic?
    // What happens to the draft/published article when the topic is deleted?
    // Can journalists view each other's draft articles?
    // Can journalists edit each other's draft articles?

    @Test
    fun `A draft article is created by a journalist`() {
        When("A journalist submits a draft article (title, text, topics)")
        val journalist = randomJournalist()
        val submitDraftArticle = SubmitDraftArticle.random()
        val (id) = `as`(journalist) { facade(submitDraftArticle) }

        Then("The draft article is created with the provided content and can be viewed")
        val article = facade(GetArticle(id))
        assertDraftArticleIsCreated(command = submitDraftArticle, by = journalist, article)
        assertArticleDraftCreatedEventIsPublished(article, journalist = journalist)
    }

    private fun assertDraftArticleIsCreated(command: SubmitDraftArticle, by: Journalist, createdArticle: ArticleDto) {
        assert(command.title == createdArticle.title)
        assert(command.text == createdArticle.text)
        assert(command.topics == createdArticle.topics)
        assert(createdArticle.journalistUserId == by.userId)
        assert(createdArticle.status == "DRAFT")
    }

    private fun assertArticleDraftCreatedEventIsPublished(article: ArticleDto, journalist: Journalist) {
        val event = eventPublisher.get<ArticleDraftCreated>(0)
        assert(event.id == article.id)
        assert(event.title == article.title)
        assert(event.text == article.text)
        assert(event.topics == article.topics)
        assert(event.journalistUserId == journalist.userId)
    }

    @Test
    fun `A draft article is edited by a journalist`() {
        Given("A submitted draft article")
        val article = submittedDraftArticle()

        When("The journalist edits a draft article (title, text, topics)")
        val editDraftArticle = EditDraftArticle.random(articleId = article.id)
        facade(editDraftArticle)

        Then("The draft article is edited with the provided content")
        val editedArticle = facade(GetArticle(article.id))
        assertDraftArticleIsEditedFor(command = editDraftArticle, editedArticle)
        assertArticledDraftEditedEventIsPublished(editedArticle)
    }

    private fun assertDraftArticleIsEditedFor(command: EditDraftArticle, editedArticle: ArticleDto) {
        assert(command.articleId == editedArticle.id)
        assert(editedArticle.title == command.title)
        assert(editedArticle.text == command.text)
        assert(editedArticle.topics == command.topics)
        assert(editedArticle.status == "DRAFT")
    }

    private fun assertArticledDraftEditedEventIsPublished(article: ArticleDto) {
        val event = eventPublisher.get<ArticleDraftEdited>(0)
        assert(event.id == article.id)
        assert(event.text == article.text)
        assert(event.title == article.title)
        assert(event.topics == article.topics)
    }

    @Test
    fun `A draft article cannot be created by a copywriter`() {
        When("A copywriter submits a draft article")
        val result = catch { asCopywriter { facade(SubmitDraftArticle.random()) } }

        Then("The operation is not permitted")
        assert(result is AuthError.MissingRole)
    }

    @Test
    fun `A draft article cannot be edited by a copywriter`() {
        Given("A submitted draft article")
        val article = asJournalist { submittedDraftArticle() }

        When("A copywriter edits a draft article")
        val result = catch { asCopywriter { facade(EditDraftArticle.random(articleId = article.id)) } }

        Then("The operation is not permitted")
        assert(result is AuthError.MissingRole)
    }

    @ParameterizedTest
    @MethodSource
    fun `Basic draft article validation rules when submitting`(
        submitDraftArticle: SubmitDraftArticle,
        property: String,
        rule: String,
    ) {
        When("A journalist submits a draft article with invalid $property")
        val result = catch { facade(submitDraftArticle) }

        Then("Submission is rejected due to $rule")
        assert(result is ConstraintViolationsException)
        val violations = (result as ConstraintViolationsException).violations()
        assert(violations.first().message().contains(rule))
    }

    fun `Basic draft article validation rules when submitting`() = Where {
        val submitDraftArticle = SubmitDraftArticle.random()
        // article                                                    | property    | rule
//        of(editDraftArticle { title = null }                        , "title"     , """title cannot be blank""")
        of(submitDraftArticle { title = "" }                          , "title"     , """"title" must not be blank""")
        of(submitDraftArticle { title = "" }                          , "title"     , """"title" must not be blank""")
        of(submitDraftArticle { title = "   " }                       , "title"     , """"title" must not be blank""")
        of(submitDraftArticle { title = "a".repeat(201) }          , "title"     , """The size of "title" must be less than or equal to 200""")
//        of(editDraftArticle { text = null }                       , "text"      , """"text" must not be blank""")
        of(submitDraftArticle { text = "" }                           , "text"      , """"text" must not be blank""")
        of(submitDraftArticle { text = "   " }                        , "text"      , """"text" must not be blank""")
//        of(editDraftArticle { topics = listOf(null) }               , "topics"    , """topic must not be blank""")
        of(submitDraftArticle { topics = listOf("") }                 , "topics"    , """"topic" must not be blank""")
        of(submitDraftArticle { topics = listOf("  ") }               , "topics"    , """"topic" must not be blank""")
        of(submitDraftArticle { topics = listOf() }                   , "topics"    , """The size of "topics" must be greater than or equal to 1""")
    }

    @ParameterizedTest
    @MethodSource
    fun `Basic draft article validation rules when editing`(editDraftArticle: EditDraftArticle, property: String, rule: String) {
        Given("A submitted draft article")
        val article = submittedDraftArticle()

        When("$property is edited")
        val result = catch { facade(editDraftArticle.copy(articleId = article.id)) }

        Then("$property edit is rejected because $rule")
        assert(result is ConstraintViolationsException)
        val violations = (result as ConstraintViolationsException).violations()
        assert(violations.first().message().contains(rule))
    }
    fun `Basic draft article validation rules when editing`() = Where {
        val editDraftArticle = EditDraftArticle.random(articleId = "")
        // article                                                  | property    | rule
//        of(editDraftArticle { title = null }                        , "title"     , """title cannot be blank""")
        of(editDraftArticle { title = "" }                          , "title"     , """"title" must not be blank""")
        of(editDraftArticle { title = "" }                          , "title"     , """"title" must not be blank""")
        of(editDraftArticle { title = "   " }                       , "title"     , """"title" must not be blank""")
        of(editDraftArticle { title = "a".repeat(201) }          , "title"     , """The size of "title" must be less than or equal to 200""")
//        of(editDraftArticle { text = null }                       , "text"      , """"text" must not be blank""")
        of(editDraftArticle { text = "" }                           , "text"      , """"text" must not be blank""")
        of(editDraftArticle { text = "   " }                        , "text"      , """"text" must not be blank""")
//        of(editDraftArticle { topics = listOf(null) }               , "topics"    , """topic must not be blank""")
        of(editDraftArticle { topics = listOf("") }                 , "topics"    , """"topic" must not be blank""")
        of(editDraftArticle { topics = listOf("  ") }               , "topics"    , """"topic" must not be blank""")
        of(editDraftArticle { topics = listOf() }                   , "topics"    , """The size of "topics" must be greater than or equal to 1""")
    }


    @Test
    fun `Journalists cannot change each other's drafts`() {
        Given("A submitted draft article A belonging to a journalist A")
        val articleOfJournalistA = asJournalistA { submittedDraftArticle() }

        And("A submitted draft article B belonging to a journalist B")
        val articleOfJournalistB = asJournalistB { submittedDraftArticle() }

        When("The journalist A tries to change the draft article B")
        val result = catch { asJournalistA { facade(EditDraftArticle.random(articleId = articleOfJournalistB.id)) } }

        Then("This operation is rejected")
        assert(result is ArticleDoesNotBelongToRequestedUser)
    }


    // Story 3: As a copywriter, I suggest changes to the draft article I'm assigned to
    // How would you define a role of a copywriter?
    // Who, when and how assigns a copywriter to the article?
    // Is the draft article available for a review immediately after the draft article was created or edited?
    // Is there any more elaborate reviewing process? Is there any draft article versioning?

    @Test
    fun `A copywriter suggests changes to an article as a comment`() {
        Given("A submitted draft article")
        val article = submittedDraftArticle()

        And("A copywriter is assigned to the article")
        val copywriter = randomCopywriter()
        facade(AssignCopywriterToArticle(copywriterUserId = copywriter.userId, articleId = article.id))

        When("The copywriter suggests a change to the article")
        val suggestChange = SuggestChange.random(articleId = article.id)
        `as`(copywriter) { facade(suggestChange) }

        Then("The change suggestion is attached to the given article and can be viewed")
        val changeSuggestions = facade(GetChangeSuggestions(articleId = article.id))
        assert(changeSuggestions.first().articleId == article.id)
        assert(changeSuggestions.first().copywriterUserId == copywriter.userId)
        assert(changeSuggestions.first().comment == suggestChange.comment)
        assert(changeSuggestions.first().createdAt <= Instant.now())
        assert(changeSuggestions.first().status == "UNRESOLVED")
    }

    @ParameterizedTest
    @MethodSource
    fun `A Change suggestion is a non empty chunk of text`(comment: String, rule: String) {
        Given("A submitted draft article")
        val article = submittedDraftArticle()

        And("A copywriter is assigned to the article")
        facade(AssignCopywriterToArticle(copywriterUserId = copywriterA.userId, articleId = article.id))

        When("The copywriter suggests a change with a blank comment")
        val result =  catch { asCopywriterA { facade(SuggestChange(articleId = article.id, comment = comment)) } }

        Then("This operation is rejected")
        assert(result is ConstraintViolationsException)
        val violations = (result as ConstraintViolationsException).violations()
        assert(violations.first().message().contains(rule))
    }

    fun `A Change suggestion is a non empty chunk of text`() = Where {
//        comment  | rule
        of(""      , """"comment" must not be blank""")
        of("  "    , """"comment" must not be blank""")
    }


    @Test
    fun `A copywriter can only suggest changes to the article they were assigned to`() {
        Given("A submitted draft article assigned to a copywriter A")
        val article = submittedDraftArticle()
        facade(AssignCopywriterToArticle(copywriterUserId = copywriterA.userId, articleId = article.id))

        When("A copywriter B suggests a change")
        val result = catch { asCopywriterB { facade(SuggestChange.random(articleId = article.id)) } }

        Then("This operation is rejected")
        assert(result is CopywriterNotAssignedToReviewArticle)
    }

    @Test
    fun `Change suggestions are not allowed once the article is published`() {
        Given("A published article")
        val article = submittedDraftArticle()
        facade(AssignCopywriterToArticle(copywriterUserId = copywriterA.userId, articleId = article.id))
        facade(PublishArticle(articleId = article.id))

        When("A copywriter suggests a change")
        val result = catch { asCopywriterA { facade(SuggestChange.random(articleId = article.id)) } }

        Then("This operation is rejected")
        assert(result is ArticleReviewClosed)
    }

    // Story 4: As a journalist, I respond to suggestions by making the change suggestions
    @Test
    fun `A change suggestion can be marked as applied by a journalist`() {
        Given("A submitted draft article")
        And("A copywriter suggested a change")
        When("The journalist makes the change suggestion by editing the article")
        Then("The article is edited with the provided content")
        When("The journalist marks the change suggestion as applied")
        Then("The change suggestion is marked and can be viewed as applied")
    }

    // Story 5: As a copywriter, I resolve suggestions that the journalist applied
    @Test
    fun `A copywriter resolves change suggestions`() {
        Given("A submitted draft article")
        And("The copywriter suggested a change")
        And("The journalist marked the change suggestion as applied")

        When("The copywriter resolves the change suggestion")
        Then("The change suggestion is marked and can be viewed as resolved")
    }

    @Test
    fun `A copywriter can reject applied change suggestions`() {
        Given("A submitted draft article")
        And("The copywriter suggested a change")
        And("The journalist marked the change suggestion as applied")

        When("The copywriter rejects the change with a comment")
        Then("The change suggestion is marked as rejected")
        And("The comment is appended to the current change suggestion")
    }

    // Story 6: Journalist publishes articles
    // What is meant by publishing an article? Where are they published to?
    // Is it possible to publish an article without a review?
    // What happens when the journalist tries to publish the article after all the suggestions were resolved but right after
    // Can article be edited after if was published? How can it be republished?
    // Are there any additional rules that needs to be checked before publishing an article in addition to checking
    @Test
    fun `A draft article can be published with no review (no unresolved suggestions)`() {
        Given("A submitted draft article")
        val article = submittedDraftArticle()

        When("The journalist publishes the article")
        facade(PublishArticle(articleId = article.id))

        Then("The article is published (status is changed)")
        val publishedArticle = facade(GetArticle(article.id))
        assert(publishedArticle.status == "PUBLISHED")
    }

    @Test
    fun `A draft article cannot be published by a copywriter`() {
        Given("A submitted draft article")
        val article = submittedDraftArticle()

        When("The journalist publishes the article")
        val result = catch { asCopywriter { facade(PublishArticle(articleId = article.id)) } }

        Then("This operation is rejected")
        assert(result is AuthError.MissingRole)
    }

    @Test
    fun `A draft article can be published by a journalist only when all change suggestions are resolved`() {
        Given("A submitted draft article")
        val article = asJournalistA { submittedDraftArticle() }

        And("The copywriter suggested a change")
        facade(AssignCopywriterToArticle(copywriterUserId = copywriterA.userId, articleId = article.id))
        asCopywriterA { facade(SuggestChange.random(articleId = article.id)) }

        When("The journalist publishes the article")
        val result = catch { asJournalistA { facade(PublishArticle(articleId = article.id)) } }

        Then("This operation is rejected due to unresolved change suggestions")
        assert(result is PublishingPolicyNotSatisfied)

        When("The copywriter marks the change suggestion as resolved")
        And("The journalist publishes the article")
        Then("The article is published (status is changed)")
    }

    // Misc
    @ParameterizedTest
    @MethodSource
    fun `Returns not found error when tries to invoke an operation for non existent article`(operationName: String, operation: () -> Unit) {
        When("Tries to invoke an operation on non existent article")
        val result = catch { operation() }
        Then("Article Not Found error is returned")
        assert(result is ArticleNotFound)
    }
    fun `Returns not found error when tries to invoke an operation for non existent article`() = Where {
        val randomArticleId = UUID.randomUUID().toString()
        of(EditDraftArticle::class.simpleName, { facade.invoke(EditDraftArticle.random(articleId = randomArticleId)) })
        of(GetArticle::class.simpleName,{ facade.invoke(GetArticle(articleId = randomArticleId)) })
    }
}