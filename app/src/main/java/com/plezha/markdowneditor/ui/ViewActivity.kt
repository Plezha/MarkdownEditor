package com.plezha.markdowneditor.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.plezha.markdowneditor.Downloader
import com.plezha.markdowneditor.ImageCache
import com.plezha.markdowneditor.MarkdownParser
import com.plezha.markdowneditor.R
import com.plezha.markdowneditor.SimpleMarkdownParser

class ViewActivity: AppCompatActivity() {
    private var currentMarkdownText = ""

    private val downloader = Downloader()
    private val parser: MarkdownParser = SimpleMarkdownParser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.view)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<FloatingActionButton>(R.id.edit_mode_fab).setOnClickListener {
            val intent = Intent(this, EditActivity::class.java).apply {
                putExtra("markdown_text", currentMarkdownText)
            }
            startActivity(intent)
            finish()
        }

        currentMarkdownText = intent.getStringExtra("markdown_text") ?: ""
        renderMarkdown()
    }

    private fun renderMarkdown() {
        val elements = parser.parse(currentMarkdownText)
        val imageCache = ImageCache(this)
        val markdownAdapter = MarkdownViewAdapter(elements, imageCache, downloader)

        findViewById<RecyclerView>(R.id.markdown_rv).apply {
            layoutManager = LinearLayoutManager(context)

            val itemDecoration = SpacerItemDecoration(context)
            addItemDecoration(itemDecoration)

            adapter = markdownAdapter
        }
    }
}