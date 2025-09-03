package com.example.xnote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.xnote.data.NoteSummary
import com.example.xnote.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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