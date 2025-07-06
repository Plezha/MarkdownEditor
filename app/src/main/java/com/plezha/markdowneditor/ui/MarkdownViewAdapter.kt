package com.plezha.markdowneditor.ui

import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.plezha.markdowneditor.Downloader
import com.plezha.markdowneditor.ImageCache
import com.plezha.markdowneditor.R
import com.plezha.markdowneditor.model.*


class MarkdownViewAdapter(
    private val elements: List<MarkdownElement>,
    private val imageCache: ImageCache,
    private val downloader: Downloader
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val TYPE_HEADER = 0
        const val TYPE_PARAGRAPH = 1
        const val TYPE_IMAGE = 2
        const val TYPE_TABLE = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (elements[position]) {
            is HeaderElement -> TYPE_HEADER
            is ParagraphElement -> TYPE_PARAGRAPH
            is ImageElement -> TYPE_IMAGE
            is TableElement -> TYPE_TABLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(TextView(context))
            TYPE_PARAGRAPH -> ParagraphViewHolder(TextView(context))
            TYPE_IMAGE -> ImageViewHolder(ImageView(context), downloader, imageCache)
            TYPE_TABLE -> TableViewHolder(TableLayout(context))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val element = elements[position]
        when (holder) {
            is HeaderViewHolder -> holder.bind(element as HeaderElement)
            is ParagraphViewHolder -> holder.bind(element as ParagraphElement)
            is ImageViewHolder -> holder.bind(element as ImageElement)
            is TableViewHolder -> holder.bind(element as TableElement)
        }
    }

    override fun getItemCount(): Int = elements.size
}

private class HeaderViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
    fun bind(element: HeaderElement) {
        textView.text = element.text
        val textSize = when (element.level) {
            1 -> 32f
            2 -> 28f
            3 -> 24f
            4 -> 20f
            5 -> 18f
            else -> 16f
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        textView.setTypeface(null, Typeface.BOLD)
    }
}

private class ParagraphViewHolder(
    private val textView: TextView
) : RecyclerView.ViewHolder(textView) {
    fun bind(element: ParagraphElement) {
        textView.text = element.text.toSpannableString()
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
    }
}

private class ImageViewHolder(
    private val imageView: ImageView,
    private val downloader: Downloader,
    private val imageCache: ImageCache
) : RecyclerView.ViewHolder(imageView) {
    fun bind(element: ImageElement) {
        val cachedBitmap = imageCache.get(element.url)
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap)
        } else {
            downloader.downloadImage(element.url) { result ->
                if (result.isSuccess) {
                    val bitmap = result.getOrThrow()
                    imageCache.put(element.url, bitmap)
                    imageView.post { imageView.setImageBitmap(bitmap) }
                } else {
                    val context = imageView.context
                    imageView.post {
                        Toast
                            .makeText(
                                context,
                                context.getString(
                                    R.string.error_while_downloading_image,
                                    element.url
                                ),
                                Toast.LENGTH_LONG
                            ).show()
                        imageView.setImageResource(R.drawable.baseline_error_outline_24)
                    }
                }
            }
        }
    }
}

private class TableViewHolder(private val tableLayout: TableLayout) :
    RecyclerView.ViewHolder(tableLayout) {
    fun bind(element: TableElement) {
        val context = tableLayout.context
        tableLayout.isStretchAllColumns = true

        val headerRow = TableRow(context)
        element.headers.forEach { headerText ->
            val textView = TextView(context).apply {
                text = headerText.toSpannableString()
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(8, 8, 8, 8)
                background =
                    ContextCompat.getDrawable(context, R.drawable.table_cell_borders)
            }
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)

        element.rows.forEach { rowData ->
            val tableRow = TableRow(context)
            rowData.forEach { cellText ->
                val textView = TextView(context).apply {
                    text = cellText.toSpannableString()
                    gravity = Gravity.CENTER
                    setPadding(8, 8, 8, 8)
                    background =
                        ContextCompat.getDrawable(context, R.drawable.table_cell_borders)
                }
                tableRow.addView(textView)
            }
            tableLayout.addView(tableRow)
        }
    }
}
