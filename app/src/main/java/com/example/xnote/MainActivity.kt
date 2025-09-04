package com.example.xnote

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xnote.adapter.NoteAdapter
import com.example.xnote.data.NoteSummary
import com.example.xnote.databinding.ActivityMainBinding
import com.example.xnote.repository.NoteRepository
import com.example.xnote.viewmodel.MainViewModel
import com.example.xnote.viewmodel.MainViewModelFactory
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteAdapter: NoteAdapter
    private var isDeleteMode = false
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(NoteRepository(this))
    }
    
    private val importFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleImportFile(it) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        
        binding.fabNewNote.setOnClickListener {
            createNewNote()
        }
        
        // 初始隐藏删除操作栏和搜索栏
        binding.deleteActionBar.visibility = View.GONE
        binding.searchLayout.visibility = View.GONE
        
        // 搜索相关事件
        setupSearchFunctionality()
        
        // 底部操作栏点击事件
        binding.btnSelectAll.setOnClickListener {
            noteAdapter.selectAll()
        }
        
        binding.btnDeselectAll.setOnClickListener {
            noteAdapter.deselectAll()
        }
        
        binding.btnDeleteSelected.setOnClickListener {
            showDeleteConfirmDialog()
        }
    }
    
    private fun setupSearchFunctionality() {
        // 搜索框文本变化监听
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                viewModel.search(query)
                
                // 显示/隐藏清除按钮
                binding.btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            }
        })
        
        // 搜索框键盘搜索按钮监听
        binding.searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                hideKeyboard()
                true
            } else {
                false
            }
        }
        
        // 清除搜索按钮
        binding.btnClearSearch.setOnClickListener {
            binding.searchEditText.setText("")
            hideKeyboard()
        }
    }
    
    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            onNoteClick = { noteSummary ->
                if (!isDeleteMode) {
                    openNote(noteSummary.id)
                }
            },
            onNoteLongClick = { noteSummary ->
                enterDeleteMode()
                noteAdapter.toggleSelection(noteSummary.id)
            },
            onSelectionChanged = { selectedNotes ->
                updateDeleteModeUI(selectedNotes)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = noteAdapter
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.notes.collect { notes ->
                noteAdapter.submitList(notes)
                updateEmptyState(notes.isEmpty())
            }
        }
        
        lifecycleScope.launch {
            viewModel.isSearchMode.collect { isSearchMode ->
                updateSearchModeUI(isSearchMode)
            }
        }
    }
    
    private fun updateSearchModeUI(isSearchMode: Boolean) {
        binding.searchLayout.visibility = if (isSearchMode) View.VISIBLE else View.GONE
        if (isSearchMode) {
            binding.searchEditText.requestFocus()
            showKeyboard()
        } else {
            binding.searchEditText.setText("")
            hideKeyboard()
        }
    }
    
    private fun showKeyboard() {
        val imm = getSystemService(InputMethodManager::class.java)
        imm.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }
    
    private fun hideKeyboard() {
        val imm = getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    private fun createNewNote() {
        lifecycleScope.launch {
            val noteId = viewModel.createNewNote()
            openNote(noteId)
        }
    }
    
    private fun openNote(noteId: String) {
        val intent = Intent(this, NoteEditActivity::class.java).apply {
            putExtra(NoteEditActivity.EXTRA_NOTE_ID, noteId)
        }
        startActivity(intent)
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val isSearchMode = viewModel.isSearchMode.value
        menu?.findItem(R.id.action_search)?.isVisible = !isDeleteMode && !isSearchMode
        menu?.findItem(R.id.action_delete_mode)?.isVisible = !isDeleteMode && !isSearchMode
        menu?.findItem(R.id.action_cancel_delete)?.isVisible = isDeleteMode
        menu?.findItem(R.id.action_export)?.isVisible = !isDeleteMode && !isSearchMode
        menu?.findItem(R.id.action_import)?.isVisible = !isDeleteMode && !isSearchMode
        menu?.findItem(R.id.action_export_selected)?.isVisible = isDeleteMode
        return super.onPrepareOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                viewModel.enterSearchMode()
                invalidateOptionsMenu()
                true
            }
            R.id.action_export -> {
                showExportDialog()
                true
            }
            R.id.action_import -> {
                showImportOptions()
                true
            }
            R.id.action_export_selected -> {
                showExportSelectedDialog()
                true
            }
            R.id.action_delete_mode -> {
                enterDeleteMode()
                true
            }
            R.id.action_cancel_delete -> {
                exitDeleteMode()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun enterDeleteMode() {
        isDeleteMode = true
        noteAdapter.setSelectionMode(true)
        binding.deleteActionBar.visibility = View.VISIBLE
        binding.fabNewNote.visibility = View.GONE
        invalidateOptionsMenu()
        
        // 更新标题
        supportActionBar?.title = "选择记事"
    }
    
    private fun exitDeleteMode() {
        isDeleteMode = false
        noteAdapter.setSelectionMode(false)
        binding.deleteActionBar.visibility = View.GONE
        binding.fabNewNote.visibility = View.VISIBLE
        invalidateOptionsMenu()
        
        // 恢复标题
        supportActionBar?.title = getString(R.string.app_name)
    }
    
    private fun updateDeleteModeUI(selectedNotes: Set<String>) {
        val count = selectedNotes.size
        supportActionBar?.title = if (count > 0) "已选择 $count 项" else "选择记事"
        
        // 更新按钮状态
        binding.btnDeleteSelected.isEnabled = count > 0
        binding.btnSelectAll.isEnabled = count < noteAdapter.itemCount
        binding.btnDeselectAll.isEnabled = count > 0
    }
    
    private fun showDeleteConfirmDialog() {
        val selectedNotes = noteAdapter.getSelectedNotes()
        val count = selectedNotes.size
        
        AlertDialog.Builder(this)
            .setTitle("删除记事")
            .setMessage("确定要删除这 $count 条记事吗？此操作不可恢复。")
            .setPositiveButton("删除") { _, _ ->
                deleteSelectedNotes(selectedNotes)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun deleteSelectedNotes(noteIds: Set<String>) {
        lifecycleScope.launch {
            viewModel.deleteNotes(noteIds)
            exitDeleteMode()
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            viewModel.isSearchMode.value -> {
                viewModel.exitSearchMode()
                invalidateOptionsMenu()
            }
            isDeleteMode -> {
                exitDeleteMode()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
    
    private fun showExportDialog() {
        val notes = viewModel.notes.value
        if (notes.isEmpty()) {
            Toast.makeText(this, "没有记事可以导出", Toast.LENGTH_SHORT).show()
            return
        }
        
        val noteIds = notes.map { it.id }.toSet()
        showExportPasswordDialog(noteIds)
    }
    
    private fun showExportSelectedDialog() {
        val selectedNotes = noteAdapter.getSelectedNotes()
        if (selectedNotes.isEmpty()) {
            Toast.makeText(this, "请选择要导出的记事", Toast.LENGTH_SHORT).show()
            return
        }
        
        showExportPasswordDialog(selectedNotes)
    }
    
    private fun showExportPasswordDialog(noteIds: Set<String>) {
        val editText = EditText(this).apply {
            hint = "请输入导出密码"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        
        AlertDialog.Builder(this)
            .setTitle("导出记事")
            .setMessage("请设置一个密码来加密您的记事文件")
            .setView(editText)
            .setPositiveButton("导出") { _, _ ->
                val password = editText.text.toString()
                if (password.isNotEmpty()) {
                    performExport(noteIds, password)
                } else {
                    Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun performExport(noteIds: Set<String>, password: String) {
        lifecycleScope.launch {
            viewModel.exportNotes(
                noteIds = noteIds,
                password = password,
                onProgress = { progress ->
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, progress, Toast.LENGTH_SHORT).show()
                    }
                },
                onSuccess = { file ->
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "导出成功：${file.name}", Toast.LENGTH_LONG).show()
                        exitDeleteMode()
                    }
                },
                onError = { error ->
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }
    
    private fun showImportOptions() {
        importFileLauncher.launch("application/zip")
    }
    
    private fun handleImportFile(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File(cacheDir, "import_temp.zip")
            tempFile.outputStream().use { output ->
                inputStream?.copyTo(output)
            }
            
            showImportPasswordDialog(tempFile)
        } catch (e: Exception) {
            Toast.makeText(this, "读取文件失败：${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun showImportPasswordDialog(zipFile: File) {
        val editText = EditText(this).apply {
            hint = "请输入导入密码"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        
        AlertDialog.Builder(this)
            .setTitle("导入记事")
            .setMessage("请输入文件的解密密码")
            .setView(editText)
            .setPositiveButton("导入") { _, _ ->
                val password = editText.text.toString()
                if (password.isNotEmpty()) {
                    showImportModeDialog(zipFile, password)
                } else {
                    Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消") { _, _ ->
                zipFile.delete()
            }
            .show()
    }
    
    private fun showImportModeDialog(zipFile: File, password: String) {
        AlertDialog.Builder(this)
            .setTitle("导入模式")
            .setMessage("请选择导入模式：\n\n覆盖导入：删除所有现有记事，导入新记事\n追加导入：保留现有记事，添加新记事")
            .setPositiveButton("覆盖导入") { _, _ ->
                performImport(zipFile, password, true)
            }
            .setNeutralButton("追加导入") { _, _ ->
                performImport(zipFile, password, false)
            }
            .setNegativeButton("取消") { _, _ ->
                zipFile.delete()
            }
            .show()
    }
    
    private fun performImport(zipFile: File, password: String, overwrite: Boolean) {
        lifecycleScope.launch {
            viewModel.importNotes(
                zipFile = zipFile,
                password = password,
                overwrite = overwrite,
                onProgress = { progress ->
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, progress, Toast.LENGTH_SHORT).show()
                    }
                },
                onSuccess = { count ->
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "成功导入 $count 条记事", Toast.LENGTH_LONG).show()
                        zipFile.delete()
                    }
                },
                onError = { error ->
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                        zipFile.delete()
                    }
                }
            )
        }
    }
}