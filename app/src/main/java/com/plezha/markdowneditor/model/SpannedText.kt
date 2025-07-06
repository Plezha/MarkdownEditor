package com.plezha.markdowneditor.model

import android.graphics.Typeface
import android.text.ParcelableSpan
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import com.plezha.markdowneditor.model.SpanType.entries

data class SpannedText(
    val text: String,
    val spans: List<Span> = listOf()
) {
    fun toSpannableString(): SpannableString {
        val spannable = SpannableStringBuilder(text)
        for (span in spans) {
            val newSpan = span.toAndroidSpan()
            spannable
                .setSpan(newSpan, span.start, span.end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return SpannableString(spannable)
    }
}

data class Span(
    val type: SpanType,
    val start: Int,
    val end: Int
) {
    fun toAndroidSpan(): ParcelableSpan {
        return when (type) {
            SpanType.Bold -> StyleSpan(Typeface.BOLD)
            SpanType.Italic -> StyleSpan(Typeface.ITALIC)
            SpanType.Strikethrough -> StrikethroughSpan()
        }
    }
}

enum class SpanType {
    Bold,
    Strikethrough,
    Italic;

    val regex: Regex
        get() {
            return when (this) {
                Bold -> """(\*\*)([^\s*].*?[^\s*])(\*\*)""".toRegex()
                Italic -> """(\*)([^\s*].*?[^\s*])(\*)""".toRegex()
                Strikethrough -> """(~~)(.+?)(~~)""".toRegex()
            }
        }

    val delimiter: String
        get() {
            return when (this) {
                Bold -> "**"
                Italic -> "*"
                Strikethrough -> "~~"
            }
        }
}

fun String.toSpanType(): SpanType? {
    for (entry in entries) {
        if (entry.delimiter == this) {
            return entry
        }
    }
    return null
}
