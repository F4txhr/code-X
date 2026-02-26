package com.example.nativecodeeditor

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    private lateinit var codeEditor: EditText
    private lateinit var lineNumbers: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        codeEditor = findViewById(R.id.codeEditor)
        lineNumbers = findViewById(R.id.lineNumbers)

        codeEditor.typeface = Typeface.MONOSPACE
        lineNumbers.typeface = Typeface.MONOSPACE

        if (savedInstanceState == null) {
            codeEditor.setText(getString(R.string.starter_code))
        }

        updateLineNumbers(codeEditor.text)
        codeEditor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                updateLineNumbers(s)
            }
        })
    }

    private fun updateLineNumbers(text: CharSequence?) {
        val lineCount = (text?.count { it == '\n' } ?: 0) + 1
        val builder = StringBuilder()
        for (line in 1..lineCount) {
            builder.append(line)
            if (line != lineCount) builder.append('\n')
        }
        lineNumbers.text = builder.toString()
    }
}
