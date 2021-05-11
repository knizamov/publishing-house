package io.github.knizamov.publishing.articles.messages.commands


public sealed class ArticleCommand

public data class SubmitDraftArticle(val title: String,
                                     val text: String,
                                     val topics: List<String>) : ArticleCommand() { public companion object }

public data class EditDraftArticle(val id: String,
                                   val title: String,
                                   val text: String,
                                   val topics: List<String>) : ArticleCommand() { public companion object }
