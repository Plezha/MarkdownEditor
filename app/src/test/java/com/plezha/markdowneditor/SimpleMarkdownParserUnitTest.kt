package com.plezha.markdowneditor

import com.plezha.markdowneditor.model.HeaderElement
import com.plezha.markdowneditor.model.ImageElement
import com.plezha.markdowneditor.model.MarkdownElement
import com.plezha.markdowneditor.model.ParagraphElement
import com.plezha.markdowneditor.model.Span
import com.plezha.markdowneditor.model.SpanType
import com.plezha.markdowneditor.model.SpannedText
import com.plezha.markdowneditor.model.TableElement
import org.junit.Assert.assertEquals
import org.junit.Test

class SimpleMarkdownParserTest {

    private var parser = SimpleMarkdownParser()

    @Test
    fun `parse empty string returns empty list`() {
        val elements = parser.parse("")
        assertEquals(emptyList<MarkdownElement>(), elements)
    }

    @Test
    fun `parse string with only whitespace returns empty list`() {
        val markdown = """
            
          
            
        """.trimIndent()
        val elements = parser.parse(markdown)
        assertEquals(emptyList<MarkdownElement>(), elements)
    }

    @Test
    fun `parses all header levels from H1 to H6`() {
        val markdown = """
            # H1
            ## H2
            ### H3
            #### H4
            ##### H5
            ###### H6
        """.trimIndent()

        val elements = parser.parse(markdown)

        val expected = listOf(
            HeaderElement(1, "H1"),
            HeaderElement(2, "H2"),
            HeaderElement(3, "H3"),
            HeaderElement(4, "H4"),
            HeaderElement(5, "H5"),
            HeaderElement(6, "H6")
        )
        assertEquals(expected, elements)
    }

    @Test
    fun `header with no space is treated as paragraph`() {
        val elements = parser.parse("#no-space-header")
        val expected = listOf(
            ParagraphElement(SpannedText("#no-space-header"))
        )
        assertEquals(expected, elements)
    }


    @Test
    fun `parses single line paragraph`() {
        val elements = parser.parse("This is a simple paragraph.")
        val expected = listOf(
            ParagraphElement(SpannedText("This is a simple paragraph."))
        )
        assertEquals(expected, elements)
    }

    @Test
    fun `parses multi-line paragraph joined by spaces`() {
        val markdown = """
            This is the first line.
            This is the second line.
        """.trimIndent()
        val elements = parser.parse(markdown)
        val expected = listOf(
            ParagraphElement(SpannedText("This is the first line. This is the second line."))
        )
        assertEquals(expected, elements)
    }

    @Test
    fun `parses multiple paragraphs separated by blank lines`() {
        val markdown = """
            First paragraph.
            
            Second paragraph.
            Still second paragraph.
            
            Third paragraph.
        """.trimIndent()
        val elements = parser.parse(markdown)
        val expected = listOf(
            ParagraphElement(SpannedText("First paragraph.")),
            ParagraphElement(SpannedText("Second paragraph. Still second paragraph.")),
            ParagraphElement(SpannedText("Third paragraph."))
        )
        assertEquals(expected, elements)
    }

    @Test
    fun `parses image element correctly`() {
        val elements = parser.parse("![alt text](http://example.com/image.png)")
        val expected = listOf(
            ImageElement("alt text", "http://example.com/image.png")
        )
        assertEquals(expected, elements)
    }

    @Test
    fun `parses a table`() {
        val markdown = """
            | Col A | Col B |
            |-------|-------|
            | r1c1  | r1c2  |
            | r2c1  | r2c2  |
        """.trimIndent()
        val elements = parser.parse(markdown)
        val expected = listOf(
            TableElement(
                headers = listOf(SpannedText("Col A"), SpannedText("Col B")),
                rows = listOf(
                    listOf(SpannedText("r1c1"), SpannedText("r1c2")),
                    listOf(SpannedText("r2c1"), SpannedText("r2c2"))
                )
            )
        )
        assertEquals(expected, elements)
    }

    @Test
    fun `parses a table with no data rows`() {
        val markdown = """
            | Header Only |
            |-------------|
        """.trimIndent()
        val elements = parser.parse(markdown)
        val expected = listOf(
            TableElement(
                headers = listOf(SpannedText("Header Only")),
                rows = emptyList()
            )
        )
        assertEquals(expected, elements)
    }

    @Test
    fun `table-like text without separator line is treated as paragraph`() {
        val markdown = "| Not a table header |"
        val elements = parser.parse(markdown)
        val expected = listOf(
            ParagraphElement(SpannedText("| Not a table header |"))
        )
        assertEquals(expected, elements)
    }

    @Test
    fun `parses nested formatting correctly 1`() {
        val elements = parser.parse("***bold and italic***")
        val expected = listOf(
            ParagraphElement(
                SpannedText(
                    "bold and italic",
                    listOf(
                        Span(SpanType.Bold, 0, 15),
                        Span(SpanType.Italic, 0, 15)
                    )
                )
            )
        )
        assertEquals(expected, elements)
    }

    @Test
    fun `parses nested formatting correctly 2`() {
        val elements = parser.parse("**bold and *italic* inside**")
        val expected = listOf(
            ParagraphElement(
                SpannedText(
                    "bold and italic inside",
                    listOf(
                        Span(SpanType.Bold, 0, 22),
                        Span(SpanType.Italic, 9, 15)
                    )
                )
            )
        )
        assertEquals(expected, elements)
    }

    @Test
    fun `parses inline formatting inside a table cell`() {
        val markdown = """
            | Header | *Formatted* Header |
            |--------|----------------------|
            | **Cell** | ~~Striked~~          |
        """.trimIndent()
        val elements = parser.parse(markdown)

        val expected = listOf(
            TableElement(
                headers = listOf(
                    SpannedText("Header"),
                    SpannedText("Formatted Header", listOf(Span(SpanType.Italic, 0, 9)))
                ),
                rows = listOf(
                    listOf(
                        SpannedText("Cell", listOf(Span(SpanType.Bold, 0, 4))),
                        SpannedText("Striked", listOf(Span(SpanType.Strikethrough, 0, 7)))
                    )
                )
            )
        )
        assertEquals(expected, elements)
    }


    @Test
    fun `parses a complex document with mixed elements`() {
        val markdown = """
            ## h2
            
            Paragraphs are ***separated*** by a blank line.
            
            2nd paragraph. It is slightly longer so you can see it is multi-line paragraph. 
            *Italic*, **bold**, and ~~strikethrough~~. Tables look like:
            
            |style  | looks        | third column |
            |-------| ------------ | ------------ |
            |bold   | **huge**     | so table     |
            |italic | *unsteady*   | looks        |
            |dashed | ~~striked~~  | better       |
            
            
            Also, there is a 1080x1080 image! 
            
            ![image](https://picsum.photos/1080/1080)
        """.trimIndent()

        val elements = parser.parse(markdown)

        val expected = listOf(

            HeaderElement(level = 2, text = "h2"),
            ParagraphElement(
                text = SpannedText(
                    text = "Paragraphs are separated by a blank line.",
                    spans = listOf(
                        Span(
                            type = SpanType.Bold,
                            start = 15,
                            end = 24
                        ), Span(type = SpanType.Italic, start = 15, end = 24)
                    )
                )
            ),
            ParagraphElement(
                text = SpannedText(
                    text = "2nd paragraph. It is slightly longer so you can see it is multi-line paragraph. Italic, bold, and strikethrough. Tables look like:",
                    spans = listOf(
                        Span(type = SpanType.Italic, start = 80, end = 86), Span(
                            type = SpanType.Bold,
                            start = 88,
                            end = 92
                        ), Span(type = SpanType.Strikethrough, start = 98, end = 111)
                    )
                )
            ),
            TableElement(
                headers = listOf(
                    SpannedText(
                        text = "style",
                    ), SpannedText(text = "looks"), SpannedText(
                        text = "third column",
                    )
                ),
                rows = listOf(
                    listOf(
                        SpannedText(text = "bold"), SpannedText(
                            text = "huge",
                            spans = listOf(Span(type = SpanType.Bold, start = 0, end = 4))
                        ), SpannedText(text = "so table")
                    ), listOf(
                        SpannedText(
                            text = "italic",
                        ), SpannedText(
                            text = "unsteady",
                            spans = listOf(Span(type = SpanType.Italic, start = 0, end = 8))
                        ), SpannedText(text = "looks")
                    ), listOf(
                        SpannedText(
                            text = "dashed",
                        ), SpannedText(
                            text = "striked",
                            spans = listOf(Span(type = SpanType.Strikethrough, start = 0, end = 7))
                        ), SpannedText(text = "better")
                    )
                )
            ),
            ParagraphElement(
                text = SpannedText(
                    text = "Also, there is a 1080x1080 image!",
                )
            ),
            ImageElement(altText = "image", url = "https://picsum.photos/1080/1080")
        )

        assertEquals(expected, elements)
    }
}