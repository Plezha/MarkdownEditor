package com.plezha.markdowneditor.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.plezha.markdowneditor.Downloader
import com.plezha.markdowneditor.R

class MainActivity : AppCompatActivity() {
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val text = contentResolver.openInputStream(it)?.bufferedReader()?.use { it.readText() }
            if (text != null) {
                showModeSelectionDialog(text)
            }
        }
    }

    private val downloader = Downloader()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonSelectFile = findViewById<Button>(R.id.select_file_button)
        val buttonLoadFile = findViewById<Button>(R.id.load_file_button)
        val editTextUrl = findViewById<EditText>(R.id.url_et)

        buttonSelectFile.setOnClickListener {
            filePickerLauncher.launch("text/markdown")
        }

        buttonLoadFile.setOnClickListener {
            val url = editTextUrl.text.toString()
            downloader.downloadText(url) { result ->
                runOnUiThread {
                    result
                        .onSuccess { text -> showModeSelectionDialog(text) }
                        .onFailure { error -> /* TODO */ }
                }
            }
        }
    }

    private fun showModeSelectionDialog(markdownText: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_choose_mode))
            .setItems(
                arrayOf(
                    getString(R.string.dialog_view),
                    getString(R.string.dialog_edit)
                )
            ) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, ViewActivity::class.java).apply {
                            putExtra("markdown_text", markdownText)
                        }
                        startActivity(intent)
                    }

                    1 -> {
                        val intent = Intent(this, EditActivity::class.java).apply {
                            putExtra("markdown_text", markdownText)
                        }
                        startActivity(intent)
                    }
                }
            }
            .show()
    }
}