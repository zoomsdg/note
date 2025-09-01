package com.dailynotes.ui.screens.note_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailynotes.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class BlockBasedNoteEditViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlockBasedNoteEditUiState())
    val uiState = _uiState.asStateFlow()
    
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories = _categories.asStateFlow()

    fun loadNote(noteId: Long) {
        if (noteId != -1L) {
            viewModelScope.launch {
                repository.getNoteById(noteId)?.let { note ->
                    val blocks = if (note.blocks.isNotEmpty()) {
                        note.blocks
                    } else {
                        // 从旧格式转换
                        convertLegacyContentToBlocks(note.content, note.mediaItems)
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        noteId = note.id,
                        title = note.title,
                        blocks = blocks,
                        category = note.category
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

    fun updateBlocks(blocks: List<ContentBlock>) {
        // 重新排序blocks
        val reorderedBlocks = blocks.mapIndexed { index, block ->
            when (block) {
                is ContentBlock.TextBlock -> block.copy(order = index)
                is ContentBlock.ImageBlock -> block.copy(order = index)
                is ContentBlock.AudioBlock -> block.copy(order = index)
            }
        }
        
        // 自动生成标题
        val extractedTitle = extractTitleFromBlocks(reorderedBlocks)
        
        _uiState.value = _uiState.value.copy(
            blocks = reorderedBlocks,
            title = if (_uiState.value.title.isEmpty()) extractedTitle else _uiState.value.title
        )
    }

    fun updateCategory(category: String) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun addBlock(type: BlockType, position: Int) {
        val currentBlocks = _uiState.value.blocks.toMutableList()
        
        // 确保不会覆盖现有块，插入到指定位置之后
        val insertPosition = minOf(position, currentBlocks.size)
        
        val newBlock = when (type) {
            BlockType.TEXT -> ContentBlock.TextBlock(order = insertPosition)
            BlockType.IMAGE -> {
                // TODO: 实际应用中这里应该打开图片选择器
                ContentBlock.ImageBlock(
                    order = insertPosition,
                    url = "",
                    localPath = ""
                )
            }
            BlockType.AUDIO -> {
                // TODO: 实际应用中这里应该打开音频录制器
                ContentBlock.AudioBlock(
                    order = insertPosition,
                    url = "",
                    localPath = ""
                )
            }
        }
        
        currentBlocks.add(insertPosition, newBlock)
        
        // 如果添加的是媒体块，自动在其后添加一个空文本块用于继续输入
        if (type != BlockType.TEXT) {
            val followingTextBlock = ContentBlock.TextBlock(order = insertPosition + 1)
            currentBlocks.add(insertPosition + 1, followingTextBlock)
        }
        
        updateBlocks(currentBlocks)
    }
    
    fun removeBlock(blockId: String) {
        val currentBlocks = _uiState.value.blocks
        val blockToRemove = currentBlocks.find { it.id == blockId }
        
        // 如果是媒体块，删除对应的文件
        when (blockToRemove) {
            is ContentBlock.ImageBlock -> {
                try {
                    if (blockToRemove.localPath.isNotEmpty()) {
                        java.io.File(blockToRemove.localPath).delete()
                    }
                } catch (e: Exception) {
                    // 忽略文件删除错误
                }
            }
            is ContentBlock.AudioBlock -> {
                try {
                    if (blockToRemove.localPath.isNotEmpty()) {
                        java.io.File(blockToRemove.localPath).delete()
                    }
                } catch (e: Exception) {
                    // 忽略文件删除错误
                }
            }
            else -> {}
        }
        
        val updatedBlocks = currentBlocks.filter { it.id != blockId }
        updateBlocks(updatedBlocks)
    }
    
    fun moveBlock(fromIndex: Int, toIndex: Int) {
        val currentBlocks = _uiState.value.blocks.toMutableList()
        if (fromIndex >= 0 && fromIndex < currentBlocks.size && 
            toIndex >= 0 && toIndex < currentBlocks.size) {
            val block = currentBlocks.removeAt(fromIndex)
            currentBlocks.add(toIndex, block)
            updateBlocks(currentBlocks)
        }
    }
    
    fun saveNote(onSaved: (() -> Unit)? = null) {
        viewModelScope.launch {
            val state = _uiState.value
            
            // 确保至少有一个文本块
            val finalBlocks = if (state.blocks.isEmpty()) {
                listOf(ContentBlock.TextBlock(order = 0))
            } else {
                state.blocks
            }
            
            // 为向后兼容生成content字符串
            val legacyContent = blocksToLegacyContent(finalBlocks)
            val legacyMediaItems = blocksToLegacyMediaItems(finalBlocks)
            
            val note = if (state.noteId == -1L) {
                NoteEntity(
                    title = state.title,
                    content = legacyContent,
                    blocks = finalBlocks,
                    category = state.category,
                    mediaItems = legacyMediaItems,
                    createdAt = Date(),
                    updatedAt = Date()
                )
            } else {
                repository.getNoteById(state.noteId)?.copy(
                    title = state.title,
                    content = legacyContent,
                    blocks = finalBlocks,
                    category = state.category,
                    mediaItems = legacyMediaItems,
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
    
    // 导出为纯文本
    fun exportToPlainText(): String {
        return _uiState.value.blocks.joinToString("\n\n") { block ->
            when (block) {
                is ContentBlock.TextBlock -> block.text
                is ContentBlock.ImageBlock -> "[图片: ${block.alt.ifEmpty { "图片" }}]"
                is ContentBlock.AudioBlock -> "[音频: ${formatDuration(block.duration)}]"
            }
        }
    }
    
    // 导出为Markdown
    fun exportToMarkdown(): String {
        return _uiState.value.blocks.joinToString("\n\n") { block ->
            when (block) {
                is ContentBlock.TextBlock -> block.text
                is ContentBlock.ImageBlock -> {
                    val alt = block.alt.ifEmpty { "图片" }
                    "![${alt}](${block.url})"
                }
                is ContentBlock.AudioBlock -> {
                    val duration = formatDuration(block.duration)
                    "[音频: ${duration}](${block.url})"
                }
            }
        }
    }
    
    private fun extractTitleFromBlocks(blocks: List<ContentBlock>): String {
        val firstTextBlock = blocks.filterIsInstance<ContentBlock.TextBlock>().firstOrNull()
        val text = firstTextBlock?.text ?: ""
        
        if (text.isBlank()) return ""
        
        val sentences = text.split(Regex("[。！？.!?]"))
        val firstSentence = sentences.firstOrNull()?.trim() ?: ""
        
        return if (firstSentence.length > 30) {
            firstSentence.take(30) + "..."
        } else {
            firstSentence
        }
    }
    
    private fun convertLegacyContentToBlocks(content: String, mediaItems: List<MediaItem>): List<ContentBlock> {
        val blocks = mutableListOf<ContentBlock>()
        val lines = content.split('\n')
        var mediaIndex = 0
        
        for ((lineIndex, line) in lines.withIndex()) {
            when {
                line.matches(Regex("\\[图片:.*]")) -> {
                    val mediaItem = mediaItems.getOrNull(mediaIndex)
                    if (mediaItem != null && mediaItem.type == MediaType.IMAGE) {
                        blocks.add(
                            ContentBlock.ImageBlock(
                                order = lineIndex,
                                localPath = mediaItem.path,
                                url = mediaItem.path
                            )
                        )
                        mediaIndex++
                    }
                }
                line.matches(Regex("\\[音频:.*]")) -> {
                    val mediaItem = mediaItems.getOrNull(mediaIndex)
                    if (mediaItem != null && mediaItem.type == MediaType.AUDIO) {
                        blocks.add(
                            ContentBlock.AudioBlock(
                                order = lineIndex,
                                localPath = mediaItem.path,
                                url = mediaItem.path,
                                duration = mediaItem.duration
                            )
                        )
                        mediaIndex++
                    }
                }
                line.isNotBlank() -> {
                    blocks.add(
                        ContentBlock.TextBlock(
                            order = lineIndex,
                            text = line
                        )
                    )
                }
            }
        }
        
        if (blocks.isEmpty()) {
            blocks.add(ContentBlock.TextBlock(order = 0))
        }
        
        return blocks
    }
    
    private fun blocksToLegacyContent(blocks: List<ContentBlock>): String {
        return blocks.joinToString("\n") { block ->
            when (block) {
                is ContentBlock.TextBlock -> block.text
                is ContentBlock.ImageBlock -> "[图片:${block.url.substringAfterLast('/')}]"
                is ContentBlock.AudioBlock -> "[音频:${block.url.substringAfterLast('/')}]"
            }
        }
    }
    
    private fun blocksToLegacyMediaItems(blocks: List<ContentBlock>): List<MediaItem> {
        return blocks.mapNotNull { block ->
            when (block) {
                is ContentBlock.ImageBlock -> MediaItem(
                    type = MediaType.IMAGE,
                    path = block.localPath.ifEmpty { block.url }
                )
                is ContentBlock.AudioBlock -> MediaItem(
                    type = MediaType.AUDIO,
                    path = block.localPath.ifEmpty { block.url },
                    duration = block.duration
                )
                else -> null
            }
        }
    }
    
    private fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return "%d:%02d".format(minutes, remainingSeconds)
    }
}

data class BlockBasedNoteEditUiState(
    val noteId: Long = -1L,
    val title: String = "",
    val blocks: List<ContentBlock> = listOf(ContentBlock.TextBlock(order = 0)),
    val category: String = "其他"
)