package io.github.knizamov.publishing.articles

import io.github.knizamov.publishing.base.*
import io.github.knizamov.publishing.base.Given
import io.github.knizamov.publishing.base.Specification
import io.github.knizamov.publishing.base.Then
import io.github.knizamov.publishing.base.When
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest

internal class ArticlesSpec : Specification() {

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
        Then("The draft article is created with the provided content and can be viewed")
    }

    @Test
    fun `A draft article is edited by a journalist`() {
        Given("A submitted draft article")
        When("The journalist edits a draft article (title, text, topics)")
        Then("The draft article is edited with the provided content")
    }

    @Test
    fun `A draft article cannot be created by a copywriter`() {
        When("A copywriter submits a draft article")
        Then("The operation is not permitted")
    }

    @Test
    fun `A draft article cannot be edited by a copywriter`() {
        Given("A submitted draft article")
        When("A copywriter edits a draft article")
        Then("The operation is not permitted")
    }

    @Test
    @ParameterizedTest
    fun `Basic draft article validation rules`(property: String, reason: String) {
        Given("A submitted draft article")
        When("$property is edited")
        Then("$property edit is rejected because $reason")
    }
    fun `Basic draft article validation rules`() = Where {
        // article                                      | property   | reason
        of(Article { title = null }                     , "title"    , "title cannot be blank")
        of(Article { title = "" }                       , "title"    , "title cannot be blank")
        of(Article { title = "   " }                    , "title"    , "title cannot be blank")
        of(Article { content = null }                   , "content"  , "content cannot be blank")
        of(Article { content = "" }                     , "content"  , "content cannot be blank")
        of(Article { content = "   " }                  , "content"  , "content cannot be blank")
        of(Article { topics = listOf(null) }            , "topics"   , "topic cannot be blank")
        of(Article { topics = listOf("") }              , "topics"   , "topic cannot be blank")
        of(Article { topics = listOf("  ") }            , "topics"   , "topic cannot be blank")
        of(Article { topics = listOf() }                , "topics"   , "article should have at least one topic")
    }

    @Test
    fun `Journalists cannot change each other's drafts`() {
        Given("A submitted draft article A belonging to a journalist A")
        And("A submitted draft article B belonging to a journalist B")
        When("The journalist A tries to change the draft article B")
        Then("This operation is rejected")
    }


    // Story 3: As a copywriter, I suggest changes to the draft article I'm assigned to
    // How would you define a role of a copywriter?
    // Who, when and how assigns a copywriter to the article?
    // Is the draft article available for a review immediately after the draft article was created or edited?
    // Is there any more elaborate reviewing process? Is there any draft article versioning?

    @Test
    fun `A copywriter suggests changes to an article as a comment`() {
        Given("A submitted draft article")
        And("A copywriter is assigned to the article")
        When("The copywriter suggests a change to the article")
        Then("The change suggestion is attached to the given article and can be viewed")
    }

    @Test
    fun `A copywriter can only suggest changes to the article they were assigned to`() {
        Given("A submitted draft article assigned to a copywriter A")
        When("A copywriter B suggests a change")
        Then("This operation is rejected")
    }

    @Test
    fun `Change suggestions are not allowed once the article is published`() {
        Given("A published article")
        When("A copywriter suggests a change")
        Then("This operation is rejected")
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
    fun `A draft article can be published with no review`() {
        Given("A submitted draft article")
        When("The journalist publishes the article")
        Then("The article is published (status is changed)")
    }

    @Test
    fun `A draft article can be published by a journalist only when all change suggestions are resolved`() {
        Given("A submitted draft article")
        And("The copywriter suggested a change")

        When("The journalist publishes the article")
        Then("This operation is rejected due to unresolved change suggestions")

        When("The copywriter marks the change suggestion as resolved")
        And("The journalist publishes the article")
        Then("The article is published (status is changed)")
    }
}