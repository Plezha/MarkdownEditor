package com.plezha.markdowneditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import java.io.File
import java.io.IOException

class ImageCache(context: Context) {
    private val memoryCache: LruCache<String, Bitmap>
    private val diskCacheDir: File

    private companion object {
        private const val TAG = "ImageCache"
    }

    init {
        val memoryCacheSize = (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()
        memoryCache = object : LruCache<String, Bitmap>(memoryCacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }

        diskCacheDir = context.cacheDir
    }

    @Synchronized
    fun get(key: String): Bitmap? {
        val fromMemory = memoryCache.get(key)
        if (fromMemory != null) {
            return fromMemory
        }

        val file = getFileForKey(key)
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap != null) {
                memoryCache.put(key, bitmap)
                return bitmap
            }
        }

        return null
    }

    @Synchronized
    fun put(key: String, bitmap: Bitmap) {
        if (memoryCache.get(key) == null) {
            memoryCache.put(key, bitmap)
        }

        val file = getFileForKey(key)
        try {
            file.outputStream().use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error saving bitmap to disk for key: $key", e)
        }
    }

    @Synchronized
    fun remove(key: String) {
        memoryCache.remove(key)

        val file = getFileForKey(key)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun getFileForKey(key: String): File {
        val filename = key.hashCode().toString()
        return File(diskCacheDir, filename)
    }
}