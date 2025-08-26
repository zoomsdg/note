package com.dailynotes.ui.screens.note_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailynotes.data.MediaItem
import com.dailynotes.data.NoteEntity
import com.dailynotes.data.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditUiState())
    val uiState = _uiState.asStateFlow()

    fun loadNote(noteId: Long) {
        if (noteId != -1L) {
            viewModelScope.launch {
                repository.getNoteById(noteId)?.let { note ->
                    _uiState.value = _uiState.value.copy(
                        noteId = note.id,
                        title = note.title,
                        content = note.content,
                        category = note.category,
                        mediaItems = note.mediaItems
                    )
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
    }

    fun updateCategory(category: String) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun addMediaItem(mediaItem: MediaItem) {
        val currentItems = _uiState.value.mediaItems.toMutableList()
        currentItems.add(mediaItem)
        _uiState.value = _uiState.value.copy(mediaItems = currentItems)
    }
    
    fun removeMediaItem(mediaItem: MediaItem) {
        val currentItems = _uiState.value.mediaItems.toMutableList()
        currentItems.remove(mediaItem)
        _uiState.value = _uiState.value.copy(mediaItems = currentItems)
        
        // 删除文件
        try {
            java.io.File(mediaItem.path).delete()
        } catch (e: Exception) {
            // 忽略文件删除错误
        }
    }
    
    fun saveNote(onSaved: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val note = if (state.noteId == -1L) {
                NoteEntity(
                    title = state.title,
                    content = state.content,
                    category = state.category,
                    mediaItems = state.mediaItems,
                    createdAt = Date(),
                    updatedAt = Date()
                )
            } else {
                repository.getNoteById(state.noteId)?.copy(
                    title = state.title,
                    content = state.content,
                    category = state.category,
                    mediaItems = state.mediaItems,
                    updatedAt = Date()
                ) ?: return@launch
            }

            if (state.noteId == -1L) {
                repository.insertNote(note)
            } else {
                repository.updateNote(note)
            }
            onSaved()
        }
    }
}

data class NoteEditUiState(
    val noteId: Long = -1L,
    val title: String = "",
    val content: String = "",
    val category: String = "其他",
    val mediaItems: List<MediaItem> = emptyList()
)