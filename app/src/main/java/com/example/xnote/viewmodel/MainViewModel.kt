package com.example.xnote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.xnote.data.NoteSummary
import com.example.xnote.repository.NoteRepository
import com.example.xnote.utils.ExportImportUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(
    private val repository: NoteRepository
) : ViewModel() {
    
    private val _notes = MutableStateFlow<List<NoteSummary>>(emptyList())
    val notes: StateFlow<List<NoteSummary>> = _notes.asStateFlow()
    
    init {
        loadNotes()
    }
    
    private fun loadNotes() {
        viewModelScope.launch {
            repository.getNoteSummaries().collect { noteList ->
                _notes.value = noteList
            }
        }
    }
    
    suspend fun createNewNote(title: String = "无标题"): String {
        return repository.createNewNote(title)
    }
    
    suspend fun deleteNote(noteId: String) {
        repository.deleteNote(noteId)
    }
    
    suspend fun deleteNotes(noteIds: Set<String>) {
        viewModelScope.launch {
            noteIds.forEach { noteId ->
                repository.deleteNote(noteId)
            }
        }
    }
    
    suspend fun exportNotes(
        noteIds: Set<String>, 
        password: String,
        onProgress: (String) -> Unit,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        val fullNotes = noteIds.mapNotNull { repository.getFullNote(it) }
        val exportUtils = ExportImportUtils(repository.context)
        exportUtils.exportNotes(fullNotes, password, onProgress, onSuccess, onError)
    }
    
    suspend fun importNotes(
        zipFile: File,
        password: String,
        overwrite: Boolean,
        onProgress: (String) -> Unit,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val exportUtils = ExportImportUtils(repository.context)
        exportUtils.importNotes(zipFile, password, onProgress, 
            onSuccess = { importNotes ->
                viewModelScope.launch {
                    try {
                        var importedCount = 0
                        
                        if (overwrite) {
                            // 清空所有现有记事
                            repository.deleteAllNotes()
                        }
                        
                        for (importNote in importNotes) {
                            repository.importNote(importNote)
                            importedCount++
                        }
                        
                        // 清理临时文件
                        importNotes.firstOrNull()?.tempDir?.deleteRecursively()
                        
                        onSuccess(importedCount)
                    } catch (e: Exception) {
                        onError("导入失败：${e.message}")
                    }
                }
            },
            onError = onError
        )
    }
}

class MainViewModelFactory(
    private val repository: NoteRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}