package com.plezha.markdowneditor.model

sealed interface MarkdownElement

data class HeaderElement(val level: Int, val text: String) : MarkdownElement
data class ParagraphElement(val text: SpannedText) : MarkdownElement
data class ImageElement(val altText: String, val url: String) : MarkdownElement
data class TableElement(val headers: List<SpannedText>, val rows: List<List<SpannedText>>) :
    MarkdownElement