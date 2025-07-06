package com.plezha.markdowneditor.ui

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.plezha.markdowneditor.R

class EditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edit)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val markdownText = intent.getStringExtra("markdown_text")
        val editText = findViewById<EditText>(R.id.markdown_et)

        editText.setText(markdownText)

        val buttonSave = findViewById<FloatingActionButton>(R.id.button_save)

        buttonSave.setOnClickListener {
            val intent = Intent(this, ViewActivity::class.java).apply {
                putExtra("markdown_text", editText.text.toString())
            }
            startActivity(intent)
            finish()
        }
    }
}