package com.plezha.markdowneditor

import android.app.Application
import android.content.Context

class MarkdownEditorApplication : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(applicationContext)
    }
}

class AppContainer(context: Context) {
    val downloader = Downloader()
    val imageCache = ImageCache(
        context
    )
}