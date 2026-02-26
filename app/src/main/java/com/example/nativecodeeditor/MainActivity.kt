package com.example.nativecodeeditor

import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.documentfile.provider.DocumentFile
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip

class MainActivity : AppCompatActivity() {

    private data class ExplorerFile(
        val label: String,
        val uri: Uri
    )

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var codeEditor: EditText
    private lateinit var lineNumbers: TextView
    private lateinit var statusBar: TextView
    private lateinit var fileList: ListView
    private lateinit var tabContainer: LinearLayout

    private val openTabs = mutableListOf<ExplorerFile>()
    private val explorerFiles = mutableListOf<ExplorerFile>()
    private var activeFile: ExplorerFile? = null

    private val treePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri == null) {
            statusBar.text = getString(R.string.status_permission_required)
            return@registerForActivityResult
        }

        runCatching {
            contentResolver.takePersistableUriPermission(
                uri,
                IntentFlags.readOnly
            )
        }

        saveTreeUri(uri)
        loadExplorerFromUri(uri)
    }

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
        setupEditor()
        setupExplorerList()

        ensureStorageAccessThenLoadFiles()
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
                    statusBar.text = getString(R.string.status_search_pending)
                    true
                }

                R.id.action_save -> {
                    val current = activeFile?.label ?: getString(R.string.status_no_file_open)
                    statusBar.text = getString(R.string.status_save_preview, current)
                    true
                }

                else -> false
            }
        }
    }

    private fun setupEditor() {
        codeEditor.typeface = Typeface.MONOSPACE
        lineNumbers.typeface = Typeface.MONOSPACE

        codeEditor.setText(getString(R.string.starter_code))
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

    private fun setupExplorerList() {
        fileList.setOnItemClickListener { _, _, position, _ ->
            val selected = explorerFiles[position]
            openFileInTab(selected)
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun ensureStorageAccessThenLoadFiles() {
        val saved = getSavedTreeUri()
        if (saved != null) {
            loadExplorerFromUri(saved)
            return
        }

        showPermissionDialog(force = false)
    }

    private fun showPermissionDialog(force: Boolean) {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_dialog_title)
            .setMessage(R.string.permission_dialog_message)
            .setCancelable(false)
            .setPositiveButton(R.string.permission_dialog_grant) { _, _ ->
                treePickerLauncher.launch(null)
            }
            .apply {
                if (!force) {
                    setNegativeButton(R.string.permission_dialog_later) { _, _ ->
                        statusBar.text = getString(R.string.status_permission_required)
                    }
                }
            }
            .show()
    }

    private fun loadExplorerFromUri(treeUri: Uri) {
        runCatching {
            val root = DocumentFile.fromTreeUri(this, treeUri)
            if (root == null || !root.canRead()) {
                statusBar.text = getString(R.string.status_cannot_read_storage)
                return
            }

            explorerFiles.clear()
            explorerFiles.addAll(collectReadableTextFiles(root, maxDepth = 4))

            val labels = explorerFiles.map { it.label }
            fileList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)

            if (explorerFiles.isEmpty()) {
                statusBar.text = getString(R.string.status_no_supported_files)
                return
            }

            statusBar.text = getString(R.string.status_files_loaded, explorerFiles.size)
        }.onFailure {
            clearSavedTreeUri()
            explorerFiles.clear()
            fileList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, emptyList<String>())
            statusBar.text = getString(R.string.status_storage_permission_revoked)
            showPermissionDialog(force = true)
        }
    }

    private fun collectReadableTextFiles(
        directory: DocumentFile,
        maxDepth: Int,
        currentDepth: Int = 0,
        parentPath: String = ""
    ): List<ExplorerFile> {
        if (currentDepth > maxDepth || !directory.isDirectory) return emptyList()

        val output = mutableListOf<ExplorerFile>()
        directory.listFiles().forEach { file ->
            val name = file.name ?: return@forEach
            val path = if (parentPath.isBlank()) name else "$parentPath/$name"

            if (file.isDirectory) {
                output += collectReadableTextFiles(file, maxDepth, currentDepth + 1, path)
            } else if (isSupportedTextFile(name)) {
                output += ExplorerFile(path, file.uri)
            }
        }
        return output.sortedBy { it.label }
    }

    private fun isSupportedTextFile(fileName: String): Boolean {
        val lower = fileName.lowercase()
        return lower.endsWith(".kt") ||
            lower.endsWith(".kts") ||
            lower.endsWith(".java") ||
            lower.endsWith(".xml") ||
            lower.endsWith(".txt") ||
            lower.endsWith(".md") ||
            lower.endsWith(".json") ||
            lower.endsWith(".yaml") ||
            lower.endsWith(".yml") ||
            lower.endsWith(".gradle")
    }

    private fun openFileInTab(file: ExplorerFile) {
        if (openTabs.none { it.uri == file.uri }) {
            openTabs.add(file)
        }
        activeFile = file
        renderTabs()
        loadFileContent(file)
    }

    private fun renderTabs() {
        tabContainer.removeAllViews()
        openTabs.forEach { file ->
            val chip = Chip(this).apply {
                text = file.label.substringAfterLast('/')
                isCheckable = true
                isChecked = activeFile?.uri == file.uri
                setOnClickListener {
                    activeFile = file
                    renderTabs()
                    loadFileContent(file)
                }
            }
            tabContainer.addView(chip)
        }
    }

    private fun loadFileContent(file: ExplorerFile) {
        runCatching {
            contentResolver.openInputStream(file.uri)?.bufferedReader()?.use { it.readText() }
        }.onSuccess { text ->
            codeEditor.setText(text ?: "")
            codeEditor.setSelection(codeEditor.text.length)
            statusBar.text = getString(R.string.status_open_file, file.label)
        }.onFailure {
            statusBar.text = getString(R.string.status_failed_open_file)
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

        statusBar.text = getString(R.string.status_cursor, line, col)
    }

    private fun saveTreeUri(uri: Uri) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(KEY_TREE_URI, uri.toString())
            .apply()
    }

    private fun getSavedTreeUri(): Uri? {
        val value = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getString(KEY_TREE_URI, null)
            ?: return null
        return runCatching { Uri.parse(value) }.getOrNull()
    }

    private fun clearSavedTreeUri() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .remove(KEY_TREE_URI)
            .apply()
    }

    private object IntentFlags {
        const val readOnly =
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
    }

    companion object {
        private const val PREFS_NAME = "editor_prefs"
        private const val KEY_TREE_URI = "tree_uri"
    }
}

private fun EditText.setOnSelectionChangedListenerCompat(onSelectionChanged: () -> Unit) {
    setOnClickListener { onSelectionChanged() }
    setOnKeyListener { _, _, _ ->
        onSelectionChanged()
        false
    }
}
