package com.example.nativecodeeditor

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var codeEditor: EditText
    private lateinit var lineNumbers: TextView
    private lateinit var statusBar: TextView
    private lateinit var fileList: ListView
    private lateinit var tabContainer: LinearLayout

    private val openFiles = listOf("MainActivity.kt", "activity_main.xml", "strings.xml")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        codeEditor = findViewById(R.id.codeEditor)
        lineNumbers = findViewById(R.id.lineNumbers)
        statusBar = findViewById(R.id.statusBar)
        fileList = findViewById(R.id.fileList)
        tabContainer = findViewById(R.id.tabContainer)

        setupToolbar()
        setupExplorer()
        setupTabs()
        setupEditor(savedInstanceState)
    }

    private fun setupToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_search -> {
                    statusBar.text = "Search belum aktif (fase berikutnya)"
                    true
                }

                R.id.action_save -> {
                    statusBar.text = "Simulasi save berhasil"
                    true
                }

                else -> false
            }
        }
    }

    private fun setupExplorer() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            openFiles
        )
        fileList.adapter = adapter
        fileList.setOnItemClickListener { _, _, position, _ ->
            val selected = openFiles[position]
            statusBar.text = "Open: $selected"
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun setupTabs() {
        openFiles.forEachIndexed { index, fileName ->
            val chip = Chip(this).apply {
                text = fileName
                isCheckable = true
                isChecked = index == 0
                setOnClickListener {
                    statusBar.text = "Tab aktif: $fileName"
                    uncheckOtherTabs(this)
                }
            }
            tabContainer.addView(chip)
        }
    }

    private fun uncheckOtherTabs(activeChip: Chip) {
        for (i in 0 until tabContainer.childCount) {
            val child = tabContainer.getChildAt(i)
            if (child is Chip && child != activeChip) {
                child.isChecked = false
            }
        }
        activeChip.isChecked = true
    }

    private fun setupEditor(savedInstanceState: Bundle?) {
        codeEditor.typeface = Typeface.MONOSPACE
        lineNumbers.typeface = Typeface.MONOSPACE

        if (savedInstanceState == null) {
            codeEditor.setText(getString(R.string.starter_code))
        }

        updateLineNumbers(codeEditor.text)
        updateCursorStatus(codeEditor.text, codeEditor.selectionStart)

        codeEditor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                updateLineNumbers(s)
                updateCursorStatus(s, codeEditor.selectionStart)
            }
        })

        codeEditor.setOnSelectionChangedListenerCompat {
            updateCursorStatus(codeEditor.text, codeEditor.selectionStart)
        }
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

    private fun updateCursorStatus(text: CharSequence?, cursorPosition: Int) {
        val safeText = text ?: ""
        val safeCursor = cursorPosition.coerceIn(0, safeText.length)

        var line = 1
        var col = 1
        for (i in 0 until safeCursor) {
            if (safeText[i] == '\n') {
                line++
                col = 1
            } else {
                col++
            }
        }

        statusBar.text = "UTF-8  •  Kotlin  •  Ln $line, Col $col"
    }
}

private fun EditText.setOnSelectionChangedListenerCompat(onSelectionChanged: () -> Unit) {
    setOnClickListener { onSelectionChanged() }
    setOnKeyListener { _, _, _ ->
        onSelectionChanged()
        false
    }
}
