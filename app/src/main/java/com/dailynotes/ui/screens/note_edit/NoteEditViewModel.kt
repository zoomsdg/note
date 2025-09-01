package com.dailynotes.ui.screens.note_edit

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailynotes.data.MediaItem
import com.dailynotes.data.MediaType
import com.dailynotes.data.NoteEntity
import com.dailynotes.data.NoteRepository
import com.dailynotes.ui.components.ContentBlock
import com.dailynotes.ui.components.ContentBlockType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
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
                    // 从保存的内容和媒体文件重新构建内容块
                    val blocks = parseContentToBlocks(note.content, note.mediaItems)
                    
                    _uiState.value = _uiState.value.copy(
                        noteId = note.id,
                        title = note.title,
                        contentField = TextFieldValue(note.content),
                        category = note.category,
                        mediaItems = note.mediaItems,
                        contentBlocks = blocks
                    )
                }
            }
        }
    }
    
    private fun parseContentToBlocks(content: String, mediaItems: List<MediaItem>): List<ContentBlock> {
        val blocks = mutableListOf<ContentBlock>()
        val lines = content.split('\n')
        
        for (line in lines) {
            if (line.matches(Regex("\\[图片:.*]")) || line.matches(Regex("\\[音频:.*]"))) {
                // 找到对应的媒体文件
                val fileName = line.substringAfter(':').substringBefore(']').substringBefore(' ')
                val mediaItem = mediaItems.find { it.path.endsWith(fileName) }
                
                mediaItem?.let {
                    val blockType = if (line.startsWith("[图片:")) ContentBlockType.IMAGE else ContentBlockType.AUDIO
                    blocks.add(ContentBlock(type = blockType, mediaItem = it))
                }
            } else if (line.isNotBlank()) {
                // 文本内容
                blocks.add(ContentBlock(type = ContentBlockType.TEXT, content = line, textFieldValue = TextFieldValue(line)))
            }
        }
        
        // 确保最后有一个空的文本块用于编辑
        if (blocks.isEmpty() || blocks.last().type != ContentBlockType.TEXT || blocks.last().content.isNotEmpty()) {
            blocks.add(ContentBlock(type = ContentBlockType.TEXT))
        }
        
        return blocks
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
        addMediaItemAtCurrentPosition(mediaItem)
    }

    fun addMediaItemAtCurrentPosition(mediaItem: MediaItem) {
        val currentItems = _uiState.value.mediaItems.toMutableList()
        currentItems.add(mediaItem)
        
        // 创建新的媒体块
        val mediaBlock = when (mediaItem.type) {
            MediaType.IMAGE -> ContentBlock(type = ContentBlockType.IMAGE, mediaItem = mediaItem)
            MediaType.AUDIO -> ContentBlock(type = ContentBlockType.AUDIO, mediaItem = mediaItem)
        }
        
        // 智能插入媒体块的逻辑
        val currentBlocks = _uiState.value.contentBlocks.toMutableList()
        val insertionIndex = findBestInsertionPoint(currentBlocks)
        
        // 插入媒体块
        currentBlocks.add(insertionIndex, mediaBlock)
        
        // 确保媒体块后面有文本块可供继续编辑
        if (insertionIndex >= currentBlocks.size - 1 || 
            currentBlocks.getOrNull(insertionIndex + 1)?.type != ContentBlockType.TEXT) {
            currentBlocks.add(insertionIndex + 1, ContentBlock(type = ContentBlockType.TEXT))
        }
        
        // 从内容块重新生成内容文本用于标题提取
        val contentText = generateContentFromBlocks(currentBlocks)
        val extractedTitle = extractTitleFromContent(contentText)
        
        _uiState.value = _uiState.value.copy(
            title = extractedTitle,
            mediaItems = currentItems,
            contentBlocks = currentBlocks
        )
    }
    
    private fun findBestInsertionPoint(blocks: List<ContentBlock>): Int {
        if (blocks.isEmpty()) return 0
        
        // 找到最后一个有内容的块
        val lastNonEmptyIndex = blocks.indexOfLast { block ->
            block.type != ContentBlockType.TEXT || block.content.isNotEmpty()
        }
        
        // 如果所有文本块都是空的，插入到开头
        if (lastNonEmptyIndex == -1) return 0
        
        // 插入到最后一个有内容的块之后
        return lastNonEmptyIndex + 1
    }
    
    private fun generateContentFromBlocks(blocks: List<ContentBlock>): String {
        return blocks.joinToString("\n") { block ->
            when (block.type) {
                ContentBlockType.TEXT -> block.content
                ContentBlockType.IMAGE -> "[图片:${block.mediaItem?.path?.substringAfterLast('/') ?: ""}]"
                ContentBlockType.AUDIO -> {
                    val fileName = block.mediaItem?.path?.substringAfterLast('/') ?: ""
                    val duration = if (block.mediaItem?.duration ?: 0 > 0) {
                        " ${formatDuration(block.mediaItem?.duration ?: 0)}"
                    } else ""
                    "[音频:$fileName$duration]"
                }
            }
        }
    }
    
    fun updateContentBlocks(blocks: List<ContentBlock>) {
        val contentText = generateContentFromBlocks(blocks)
        val extractedTitle = extractTitleFromContent(contentText)
        
        _uiState.value = _uiState.value.copy(
            contentBlocks = blocks,
            contentField = TextFieldValue(contentText),
            title = extractedTitle
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
        
        // 从内容块中删除对应的媒体块
        val currentBlocks = _uiState.value.contentBlocks.toMutableList()
        currentBlocks.removeAll { block -> 
            (block.type == ContentBlockType.IMAGE || block.type == ContentBlockType.AUDIO) &&
            block.mediaItem?.path == mediaItem.path 
        }
        
        val contentText = generateContentFromBlocks(currentBlocks)
        val extractedTitle = extractTitleFromContent(contentText)
        
        _uiState.value = _uiState.value.copy(
            mediaItems = currentItems,
            contentBlocks = currentBlocks,
            contentField = TextFieldValue(contentText),
            title = extractedTitle
        )
        
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
    
    // 移动光标到媒体结束位置（媒体下一行开头）
    fun moveCursorToMediaEnd(mediaItem: MediaItem) {
        val currentState = _uiState.value
        val currentText = currentState.contentField.text
        
        // 简化版本：将光标移动到文本末尾
        // 在实际应用中，这里需要计算媒体项目在文本中的实际位置
        val newSelection = TextRange(currentText.length)
        val newTextFieldValue = currentState.contentField.copy(selection = newSelection)
        
        _uiState.value = currentState.copy(contentField = newTextFieldValue)
    }
    
    // 在当前光标位置插入媒体的标记文本
    fun insertMediaAtCursor(mediaItem: MediaItem) {
        val currentState = _uiState.value
        val currentTextFieldValue = currentState.contentField
        val cursorPosition = currentTextFieldValue.selection.start
        
        // 插入媒体标记（简化版本）
        val beforeCursor = currentTextFieldValue.text.substring(0, cursorPosition)
        val afterCursor = currentTextFieldValue.text.substring(cursorPosition)
        val mediaPlaceholder = when (mediaItem.type) {
            MediaType.IMAGE -> "\n[图片]\n"
            MediaType.AUDIO -> "\n[音频]\n"
        }
        
        val newText = beforeCursor + mediaPlaceholder + afterCursor
        val newCursorPosition = cursorPosition + mediaPlaceholder.length
        val newTextFieldValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPosition)
        )
        
        // 同时更新媒体项目列表
        val updatedMediaItems = currentState.mediaItems.toMutableList()
        updatedMediaItems.add(mediaItem)
        
        _uiState.value = currentState.copy(
            contentField = newTextFieldValue,
            mediaItems = updatedMediaItems
        )
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
    val mediaItems: List<MediaItem> = emptyList(),
    val contentBlocks: List<ContentBlock> = listOf(ContentBlock(type = ContentBlockType.TEXT))
) {
    // 向后兼容性：从TextFieldValue获取纯文本内容
    val content: String get() = contentField.text
}