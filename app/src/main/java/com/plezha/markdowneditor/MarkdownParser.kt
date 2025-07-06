package com.plezha.markdowneditor

import com.plezha.markdowneditor.model.HeaderElement
import com.plezha.markdowneditor.model.ImageElement
import com.plezha.markdowneditor.model.MarkdownElement
import com.plezha.markdowneditor.model.ParagraphElement
import com.plezha.markdowneditor.model.Span
import com.plezha.markdowneditor.model.SpanType
import com.plezha.markdowneditor.model.SpannedText
import com.plezha.markdowneditor.model.TableElement

interface MarkdownParser {
    fun parse(markdownText: String): List<MarkdownElement>
}

class SimpleMarkdownParser : MarkdownParser {
    private val headerRegex = "^(#{1,6})\\s+(.+)".toRegex()
    private val imageRegex = "^!\\[(.*?)]\\((.*?)\\)".toRegex()
    private val tableRowRegex = "^\\|.*\\|$".toRegex()
    private val tableSeparatorRegex = "^\\|([\\s:?-]+\\|)+$".toRegex()

    override fun parse(markdownText: String): List<MarkdownElement> {
        val elements = mutableListOf<MarkdownElement>()
        val lines = markdownText.split('\n').map { it.trimEnd() }
        var i = 0

        while (i < lines.size) {
            val line = lines[i]

            if (line.isBlank()) {
                i++
                continue
            }

            when {
                isTableStart(lines, i) -> {
                    val (tableElement, linesConsumed) = parseTable(lines, i)
                    elements.add(tableElement)
                    i += linesConsumed
                }

                headerRegex.matches(line) -> {
                    val match = headerRegex.find(line)!!
                    val level = match.groupValues[1].length
                    val text = match.groupValues[2]
                    elements.add(HeaderElement(level, text))
                    i++
                }

                imageRegex.matches(line) -> {
                    val match = imageRegex.find(line)!!
                    val altText = match.groupValues[1]
                    val url = match.groupValues[2]
                    elements.add(ImageElement(altText, url))
                    i++
                }

                else -> {
                    val paragraphLines = mutableListOf(line)
                    var j = i + 1
                    while (
                        j < lines.size
                        && lines[j].isNotEmpty()
                        && !isBlockElementStart(lines[j])
                    ) {
                        paragraphLines.add(lines[j])
                        j++
                    }
                    val paragraphText = paragraphLines.joinToString(" ")
                    elements.add(ParagraphElement(parseInlineFormatting(paragraphText)))
                    i = j
                }
            }
        }
        return elements
    }

    private fun isTableStart(lines: List<String>, index: Int): Boolean {
        if (index + 1 >= lines.size) return false
        val currentLine = lines[index]
        val nextLine = lines[index + 1]
        return tableRowRegex.matches(currentLine) && tableSeparatorRegex.matches(nextLine)
    }

    private fun parseTable(lines: List<String>, startIndex: Int): Pair<TableElement, Int> {
        val headerLine = lines[startIndex]
        val headers = parseTableRow(headerLine)

        val rows = mutableListOf<List<SpannedText>>()
        var currentIndex = startIndex + 2

        while (currentIndex < lines.size && tableRowRegex.matches(lines[currentIndex])) {
            rows.add(parseTableRow(lines[currentIndex]))
            currentIndex++
        }

        val table = TableElement(headers, rows)
        val linesConsumed = currentIndex - startIndex
        return Pair(table, linesConsumed)
    }

    private fun parseTableRow(line: String): List<SpannedText> {
        return line
            .removeSurrounding("|")
            .split("|")
            .map { parseInlineFormatting(it.trim()) }
    }

    private fun isBlockElementStart(line: String): Boolean {
        return headerRegex.matches(line)
                || imageRegex.matches(line)
                || tableRowRegex.matches(line)
    }

    private fun parseInlineFormatting(text: String): SpannedText {
        val textBuilder = StringBuilder(text)
        val spans = mutableListOf<Span>()

        val formatters = SpanType.entries.map {
            it to it.regex
        }

        for ((spanType, regex) in formatters) {
            var match = regex.find(textBuilder)
            while (match != null) {
                val delimiter = match.groupValues[1]
                val content = match.groupValues[2]
                val range = match.range

                textBuilder.replace(range.first, range.last + 1, content)

                val spanStart = range.first
                val spanEnd = spanStart + content.length

                val spansIterator = spans.listIterator()
                while (spansIterator.hasNext()) {
                    val existingSpan = spansIterator.next()
                    val startOffset =
                        if (existingSpan.start > range.last)
                            delimiter.length * 2
                        else if (existingSpan.start > range.first)
                            delimiter.length
                        else
                            0
                    val endOffset =
                        if (existingSpan.end > range.last)
                            delimiter.length * 2
                        else if (existingSpan.end > range.first)
                            delimiter.length
                        else
                            0
                    spansIterator.set(
                        existingSpan.copy(
                            start = existingSpan.start - startOffset,
                            end = existingSpan.end - endOffset
                        )
                    )
                }

                spans.add(Span(spanType, spanStart, spanEnd))

                match = regex.find(textBuilder, range.first)
            }
        }

        return SpannedText(textBuilder.toString(), spans.sortedBy { it.start })
    }
}
