package io.github.knizamov.publishing.articles

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.knizamov.publishing.articles.messages.commands.EditDraftArticle
import io.github.knizamov.publishing.articles.messages.commands.SubmitDraftArticle
import io.github.knizamov.publishing.articles.messages.events.ArticleEvent
import io.github.knizamov.publishing.base.TestEventPublisher
import io.github.knizamov.publishing.base.TestUserContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.event.EventListener
import org.springframework.test.web.servlet.MockMvc


// Note for a reviewer: This is experimental. I already have unit tests but I wanted to make sure that the same scenarios
// pass for fully blown application, i.e. that all integrations work (e.g. serialization, RestControllers, database etc.)
// But current RemoteMockMvcArticleFacade has multiple flaws:
// - It uses MockMvc so it's not actually a mock server not a real one, there is no http communication
// - MockMvc does not support Exception Handlers, so I can't verify http exception mapping, I just rethrow normal in-process exceptions
// - By trying to use a real server I can't use Spring Security support for testing so I need the other way to mock Users for a running server (this would probably work https://stackoverflow.com/questions/61500578/how-to-mock-jwt-authenticaiton-in-a-spring-boot-unit-test)
@SpringBootTest
@AutoConfigureMockMvc
internal class ArticlesIntegrationSpec: ArticlesSpec() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var springEventCatcher: SpringEventCatcher

    override fun createFacade(
        testEventPublisher: TestEventPublisher<ArticleEvent>,
        testUserContext: TestUserContext,
    ): ArticleFacade {
        springEventCatcher.reset(testEventPublisher)
        return RemoteMockMvcArticleFacade(mockMvc, objectMapper, testUserContext)
    }

    // overriding is not necessary but it allows to debug each individual integration test
    override fun `Article drafting, reviewing and publishing acceptance scenario`() {
        super.`Article drafting, reviewing and publishing acceptance scenario`()
    }

    @Test override fun `A draft article is created by a journalist`() {
        super.`A draft article is created by a journalist`()
    }

    @Test override fun `A draft article is edited by a journalist`() {
        super.`A draft article is edited by a journalist`()
    }

    @Test override fun `A draft article cannot be created by a copywriter`() {
        super.`A draft article cannot be created by a copywriter`()
    }

    @Test override fun `A draft article cannot be edited by a copywriter`() {
        super.`A draft article cannot be edited by a copywriter`()
    }

    @ParameterizedTest @MethodSource override fun `Basic draft article validation rules when submitting`(
        submitDraftArticle: SubmitDraftArticle,
        property: String,
        rule: String,
    ) {
        super.`Basic draft article validation rules when submitting`(submitDraftArticle, property, rule)
    }

    @ParameterizedTest @MethodSource override fun `Basic draft article validation rules when editing`(
        editDraftArticle: EditDraftArticle,
        property: String,
        rule: String,
    ) {
        super.`Basic draft article validation rules when editing`(editDraftArticle, property, rule)
    }

    @Test override fun `Journalists cannot change each other's drafts`() {
        super.`Journalists cannot change each other's drafts`()
    }

    @Test override fun `A copywriter suggests changes to an article as a comment`() {
        super.`A copywriter suggests changes to an article as a comment`()
    }

    @Test override fun `A copywriter can only suggest changes to the article they were assigned to`() {
        super.`A copywriter can only suggest changes to the article they were assigned to`()
    }

    @Test override fun `Change suggestions are not allowed once the article is published`() {
        super.`Change suggestions are not allowed once the article is published`()
    }

    @Test override fun `A change suggestion can be marked as applied by a journalist`() {
        super.`A change suggestion can be marked as applied by a journalist`()
    }

    @Test override fun `A copywriter resolves change suggestions`() {
        super.`A copywriter resolves change suggestions`()
    }

    @Test override fun `A copywriter can reject applied change suggestions`() {
        super.`A copywriter can reject applied change suggestions`()
    }

    @Test override fun `A draft article can be published with no review (no unresolved suggestions)`() {
        super.`A draft article can be published with no review (no unresolved suggestions)`()
    }

    @Test override fun `A draft article cannot be published by a copywriter`() {
        super.`A draft article cannot be published by a copywriter`()
    }

    @Test override fun `A draft article can be published by a journalist only when all change suggestions are resolved`() {
        super.`A draft article can be published by a journalist only when all change suggestions are resolved`()
    }

    @ParameterizedTest @MethodSource override fun `Returns not found error when tries to invoke an operation for non existent article`(
        operationName: String,
        operation: () -> Unit,
    ) {
        super.`Returns not found error when tries to invoke an operation for non existent article`(operationName,
            operation)
    }

    @TestConfiguration
    internal class Configuration {
        @Bean fun springEventCatcher() = SpringEventCatcher()
    }
}

internal class SpringEventCatcher() {
    private lateinit var testEventPublisher: TestEventPublisher<ArticleEvent>

    @EventListener
    fun on(articleEvent: ArticleEvent) {
        testEventPublisher.publish(articleEvent)
    }

    internal fun reset(testEventPublisher: TestEventPublisher<ArticleEvent>) {
        this.testEventPublisher = testEventPublisher
    }
}


