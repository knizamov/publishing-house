package io.github.knizamov.publishing.articles.messages.commands


public sealed class ArticleCommand

public data class SubmitDraftArticle(val title: String,
                                     val text: String,
                                     val topics: List<String>) : ArticleCommand() { public companion object }

public data class EditDraftArticle(val articleId: String,
                                   val title: String,
                                   val text: String,
                                   val topics: List<String>) : ArticleCommand() { public companion object }

public data class AssignCopywriterToArticle(val copywriterUserId: String,
                                            val articleId: String): ArticleCommand() { public companion object }

public data class SuggestChange(val articleId: String,
                                val comment: String): ArticleCommand() { public companion object }