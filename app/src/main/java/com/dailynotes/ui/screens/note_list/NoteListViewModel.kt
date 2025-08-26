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
    
    fun exportNotesToUserLocation(uri: Uri) {
        viewModelScope.launch {
            val result = exportImportManager.exportNotesToUserLocation(context, uri)
            _exportResult.value = result
        }
    }
    
    fun importNotes(zipUri: Uri) {
        viewModelScope.launch {
            val result = exportImportManager.importNotes(context, zipUri)
            _importResult.value = result
        }
    }
    
    fun clearExportResult() {
        _exportResult.value = null
    }
    
    fun clearImportResult() {
        _importResult.value = null
    }
}