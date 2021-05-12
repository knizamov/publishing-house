package io.github.knizamov.publishing.articles


import am.ik.yavi.core.ConstraintViolationsException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.knizamov.publishing.articles.messages.ArticleDto
import io.github.knizamov.publishing.articles.messages.commands.*
import io.github.knizamov.publishing.articles.messages.queries.GetArticle
import io.github.knizamov.publishing.articles.messages.queries.GetChangeSuggestions
import io.github.knizamov.publishing.articles.review.ChangeSuggestionDto
import io.github.knizamov.publishing.base.TestUserContext
import io.github.knizamov.publishing.shared.errors.Error
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post


internal class RemoteMockMvcArticleFacade(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val testUserContext: TestUserContext,
) : ArticleFacade {

    override fun submitDraftArticle(command: SubmitDraftArticle): ArticleDto {
        return mockMvc.post("/articles/submitDraftArticle", command)
    }

    override fun editDraftArticle(command: EditDraftArticle): ArticleDto {
        return mockMvc.post("/articles/${command.articleId}/editDraftArticle", command)
    }

    override fun publishArticle(command: PublishArticle): ArticleDto {
        return mockMvc.post("/articles/${command.articleId}/publishArticle", command)
    }

    override fun assignCopywriterToArticle(command: AssignCopywriterToArticle) {
        return mockMvc.post("/articles/${command.articleId}/assignCopywriterToArticle", command)
    }

    override fun suggestChange(command: SuggestChange) {
        return mockMvc.post("/articles/${command.articleId}/suggestChange", command)
    }

    override fun getArticle(query: GetArticle): ArticleDto {
        return mockMvc.get("/articles/${query.articleId}")
    }

    override fun getChangeSuggestions(query: GetChangeSuggestions): List<ChangeSuggestionDto> {
        return mockMvc.get("/articles/${query.articleId}/changeSuggestions")
    }


    private inline fun <reified R> MockMvc.post(urlTemplate: String, body: Any): R {
        try {
            val result = post(urlTemplate = urlTemplate, dsl = {
                mockJwtUserBasedOnCurrentTestUserContext()
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(body)
                accept = MediaType.APPLICATION_JSON
            }).andReturn()

            println(result)

            if (Unit is R) {
                // no response body is expected
                return Unit
            }
            val responseBody = result.response.contentAsString
            return objectMapper.readValue(responseBody, object : TypeReference<R>() {})
        } catch (ex: Exception) {
            if (ex.cause is Error || ex.cause is ConstraintViolationsException) throw ex.cause!!
            else throw ex
        }
    }

    private inline fun <reified R> MockMvc.get(urlTemplate: String): R {
        try {
            val responseBody = this.get(urlTemplate = urlTemplate, dsl = {
                mockJwtUserBasedOnCurrentTestUserContext()
                accept = MediaType.APPLICATION_JSON
            }).andReturn().response.contentAsString

            return objectMapper.readValue(responseBody, object : TypeReference<R>() {})
        } catch (ex: Exception) {
            if (ex.cause is Error || ex.cause is ConstraintViolationsException) throw ex.cause!!
            else throw ex
        }
    }


    private fun MockHttpServletRequestDsl.mockJwtUserBasedOnCurrentTestUserContext() {
        val currentTestUser = testUserContext.currentUser()
        if (currentTestUser != null) {
            with(jwt()
                .authorities(currentTestUser.roles.map { SimpleGrantedAuthority(it) })
                .jwt { it.subject(currentTestUser.userId) })
        }

    }
}





