package io.github.knizamov.publishing.articles.intrastructure.http

import io.github.knizamov.publishing.articles.ArticleFacade
import io.github.knizamov.publishing.articles.messages.ArticleDto
import io.github.knizamov.publishing.articles.messages.commands.*
import io.github.knizamov.publishing.articles.messages.queries.GetArticle
import io.github.knizamov.publishing.articles.messages.queries.GetChangeSuggestions
import io.github.knizamov.publishing.articles.review.ChangeSuggestionDto
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/articles")
private open class ArticleController(
    private val articleFacade: ArticleFacade
) {

    @PostMapping("/submitDraftArticle")
    fun submitDraftArticle(@RequestBody command: SubmitDraftArticle): ArticleDto {
        return articleFacade.submitDraftArticle(command)
    }
    @PostMapping("/{articleId}/editDraftArticle")
    fun editDraftArticle(@RequestBody command: EditDraftArticle): ArticleDto {
        return articleFacade.editDraftArticle(command)
    }
    @PostMapping("/{articleId}/publishArticle")
    fun publishArticle(@RequestBody command: PublishArticle): ArticleDto {
        return articleFacade.publishArticle(command)
    }
    @PostMapping("/{articleId}/assignCopywriterToArticle")
    fun assignCopywriterToArticle(@RequestBody command: AssignCopywriterToArticle): Unit {
        return articleFacade.assignCopywriterToArticle(command)
    }
    @PostMapping("/{articleId}/suggestChange")
    fun suggestChange(@RequestBody command: SuggestChange) {
        return articleFacade.suggestChange(command)
    }

    @GetMapping("/{articleId}")
    fun getArticle(@PathVariable articleId: String): ArticleDto {
        return articleFacade.getArticle(GetArticle(articleId))
    }

    @GetMapping("/{articleId}/changeSuggestions")
    fun getChangeSuggestions(@PathVariable articleId: String): List<ChangeSuggestionDto> {
        return articleFacade.getChangeSuggestions(GetChangeSuggestions(articleId))
    }
}