package com.example.xnote

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteAdapter: NoteAdapter
    private var isDeleteMode = false
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(NoteRepository(this))
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
        
        // 初始隐藏删除操作栏
        binding.deleteActionBar.visibility = View.GONE
        
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
        menu?.findItem(R.id.action_delete_mode)?.isVisible = !isDeleteMode
        menu?.findItem(R.id.action_cancel_delete)?.isVisible = isDeleteMode
        return super.onPrepareOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
        if (isDeleteMode) {
            exitDeleteMode()
        } else {
            super.onBackPressed()
        }
    }
}