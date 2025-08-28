package com.dailynotes.ui.screens.note_list

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailynotes.data.NoteEntity
import com.dailynotes.data.NoteRepository
import com.dailynotes.utils.ExportImportManager
import com.dailynotes.utils.ExportResult
import com.dailynotes.utils.ImportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val exportImportManager: ExportImportManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("全部")
    val selectedCategory = _selectedCategory.asStateFlow()

    val categories = repository.getAllCategories()
        .map { categories -> 
            listOf("全部") + categories 
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf("全部"))

    val notes = combine(
        searchQuery,
        selectedCategory
    ) { query, category ->
        when {
            query.isNotEmpty() -> repository.searchNotes(query)
            category != "全部" -> repository.getNotesByCategory(category)
            else -> repository.getAllNotes()
        }
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        _searchQuery.value = ""
    }

    private val _exportResult = MutableStateFlow<ExportResult?>(null)
    val exportResult = _exportResult.asStateFlow()
    
    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult = _importResult.asStateFlow()
    
    // 批量删除状态
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()
    
    private val _selectedNotes = MutableStateFlow<Set<Long>>(emptySet())
    val selectedNotes = _selectedNotes.asStateFlow()
    
    private val _customExportIds = MutableStateFlow<List<Long>?>(null)
    val customExportIds = _customExportIds.asStateFlow()
    
    private val _exportPassword = MutableStateFlow<String?>(null)
    val exportPassword = _exportPassword.asStateFlow()

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
    
    fun exportNotes() {
        viewModelScope.launch {
            val result = exportImportManager.exportNotes(context)
            _exportResult.value = result
        }
    }
    
    fun exportNotesToUserLocation(uri: Uri, selectedNoteIds: List<Long>? = null) {
        viewModelScope.launch {
            // 使用自定义导出ID（如果存在）或传入的selectedNoteIds
            val exportIds = _customExportIds.value ?: selectedNoteIds
            val password = _exportPassword.value
            
            val result = if (password != null) {
                exportImportManager.exportNotesToUserLocationWithPassword(context, uri, password, exportIds)
            } else {
                exportImportManager.exportNotesToUserLocation(context, uri, exportIds)
            }
            _exportResult.value = result
            // 清除自定义导出ID和密码
            _customExportIds.value = null
            _exportPassword.value = null
        }
    }
    
    fun setCustomExportIds(ids: List<Long>) {
        _customExportIds.value = ids
    }
    
    fun setExportPassword(password: String) {
        _exportPassword.value = password
    }
    
    fun exportNotesToUserLocationWithPassword(uri: Uri, password: String, selectedNoteIds: List<Long>? = null) {
        viewModelScope.launch {
            // 使用自定义导出ID（如果存在）或传入的selectedNoteIds
            val exportIds = _customExportIds.value ?: selectedNoteIds
            val result = exportImportManager.exportNotesToUserLocationWithPassword(context, uri, password, exportIds)
            _exportResult.value = result
            // 清除自定义导出ID
            _customExportIds.value = null
        }
    }
    
    fun importNotes(zipUri: Uri, replaceExisting: Boolean = false) {
        viewModelScope.launch {
            val result = exportImportManager.importNotes(context, zipUri, replaceExisting)
            _importResult.value = result
        }
    }
    
    fun importNotesWithPassword(zipUri: Uri, password: String, replaceExisting: Boolean = false) {
        viewModelScope.launch {
            val result = exportImportManager.importNotesWithPassword(context, zipUri, password, replaceExisting)
            _importResult.value = result
        }
    }
    
    fun validatePassword(password: String): Boolean {
        return exportImportManager.validatePassword(password)
    }
    
    fun getPasswordRequirements(): String {
        return exportImportManager.getPasswordRequirements()
    }
    
    fun clearExportResult() {
        _exportResult.value = null
    }
    
    fun clearImportResult() {
        _importResult.value = null
    }
    
    // 批量删除相关方法
    fun enterSelectionMode() {
        _isSelectionMode.value = true
        _selectedNotes.value = emptySet()
    }
    
    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedNotes.value = emptySet()
    }
    
    fun toggleNoteSelection(noteId: Long) {
        val currentSelection = _selectedNotes.value.toMutableSet()
        if (currentSelection.contains(noteId)) {
            currentSelection.remove(noteId)
        } else {
            currentSelection.add(noteId)
        }
        _selectedNotes.value = currentSelection
    }
    
    fun selectAllNotes() {
        viewModelScope.launch {
            val allNotes = notes.value
            _selectedNotes.value = allNotes.map { it.id }.toSet()
        }
    }
    
    fun deleteSelectedNotes() {
        viewModelScope.launch {
            val selectedIds = _selectedNotes.value
            val allNotes = notes.value
            val notesToDelete = allNotes.filter { selectedIds.contains(it.id) }
            
            notesToDelete.forEach { note ->
                repository.deleteNote(note)
            }
            
            exitSelectionMode()
        }
    }
    
    fun getSelectedCount(): Int = _selectedNotes.value.size
}