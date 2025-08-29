package com.dailynotes.ui.screens.note_edit

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailynotes.data.MediaItem
import com.dailynotes.data.MediaType
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
    
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories = _categories.asStateFlow()

    fun loadNote(noteId: Long) {
        if (noteId != -1L) {
            viewModelScope.launch {
                repository.getNoteById(noteId)?.let { note ->
                    _uiState.value = _uiState.value.copy(
                        noteId = note.id,
                        title = note.title,
                        contentField = TextFieldValue(note.content),
                        category = note.category,
                        mediaItems = note.mediaItems
                    )
                }
            }
        }
    }
    
    fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                val defaultCategories = listOf("日常", "工作", "旅行", "心情", "其他")
                val allCategories = (defaultCategories + categories).distinct()
                _categories.value = allCategories
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateContent(contentField: TextFieldValue) {
        val extractedTitle = extractTitleFromContent(contentField.text)
        _uiState.value = _uiState.value.copy(
            contentField = contentField,
            title = extractedTitle
        )
    }
    
    private fun extractTitleFromContent(content: String): String {
        if (content.isBlank()) return ""
        
        // 获取第一行
        val firstLine = content.lines().firstOrNull()?.trim() ?: ""
        if (firstLine.isEmpty()) return ""
        
        // 获取第一句（以句号、问号、感叹号分割）
        val sentences = firstLine.split(Regex("[。！？.!?]"))
        val firstSentence = sentences.firstOrNull()?.trim() ?: ""
        
        // 限制标题长度，避免过长
        return if (firstSentence.length > 30) {
            firstSentence.take(30) + "..."
        } else {
            firstSentence
        }
    }

    fun updateCategory(category: String) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun addMediaItem(mediaItem: MediaItem) {
        val currentItems = _uiState.value.mediaItems.toMutableList()
        currentItems.add(mediaItem)
        
        // 在光标位置插入媒体标记
        val currentContent = _uiState.value.contentField
        val cursorPosition = currentContent.selection.start
        
        val mediaTag = when (mediaItem.type) {
            MediaType.IMAGE -> "\n[图片:${mediaItem.path.substringAfterLast('/')}]\n"
            MediaType.AUDIO -> "\n[音频:${mediaItem.path.substringAfterLast('/')}${if (mediaItem.duration > 0) " ${formatDuration(mediaItem.duration)}" else ""}]\n"
        }
        
        val newText = StringBuilder(currentContent.text)
            .insert(cursorPosition, mediaTag)
            .toString()
        
        val newCursorPosition = cursorPosition + mediaTag.length
        val newContentField = TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPosition)
        )
        
        val extractedTitle = extractTitleFromContent(newText)
        
        _uiState.value = _uiState.value.copy(
            contentField = newContentField,
            title = extractedTitle,
            mediaItems = currentItems
        )
    }
    
    private fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return "%d:%02d".format(minutes, remainingSeconds)
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
    
    fun saveNote(onSaved: (() -> Unit)? = null) {
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
            onSaved?.invoke()
        }
    }
    
    fun saveAndExit(onNavigateBack: () -> Unit) {
        saveNote(onNavigateBack)
    }
}

data class NoteEditUiState(
    val noteId: Long = -1L,
    val title: String = "",
    val contentField: TextFieldValue = TextFieldValue(""),
    val category: String = "其他",
    val mediaItems: List<MediaItem> = emptyList()
) {
    // 向后兼容性：从TextFieldValue获取纯文本内容
    val content: String get() = contentField.text
}