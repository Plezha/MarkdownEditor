package com.plezha.markdowneditor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class Downloader {
    private val executor = Executors.newFixedThreadPool(4)

    fun downloadText(urlString: String, callback: (Result<String>) -> Unit) {
        executor.execute {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                val text = connection.inputStream.bufferedReader().use { it.readText() }
                callback(Result.success(text))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }

    fun downloadImage(urlString: String, callback: (Result<Bitmap>) -> Unit) {
        executor.execute {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                val bitmap = BitmapFactory.decodeStream(connection.inputStream)
                callback(Result.success(bitmap))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }
}