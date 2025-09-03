package com.example.xnote.repository

import android.content.Context
import com.example.xnote.data.*
import com.example.xnote.utils.ExportImportUtils
import com.example.xnote.utils.FileUtils
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.UUID

/**
 * 记事仓库
 */
class NoteRepository(val context: Context) {
    
    private val noteDao = NoteDatabase.getDatabase(context).noteDao()
    
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    
    fun getNoteSummaries(): Flow<List<NoteSummary>> = noteDao.getNoteSummaries()
    
    suspend fun getNoteById(noteId: String): Note? = noteDao.getNoteById(noteId)
    
    suspend fun getFullNote(noteId: String): FullNote? = noteDao.getFullNote(noteId)
    
    suspend fun saveNote(note: Note) = noteDao.insertNote(note)
    
    suspend fun saveFullNote(fullNote: FullNote) = noteDao.saveFullNote(fullNote)
    
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    
    suspend fun deleteNote(noteId: String) = noteDao.deleteFullNote(noteId)
    
    suspend fun createNewNote(title: String = "无标题"): String {
        val noteId = UUID.randomUUID().toString()
        val note = Note(
            id = noteId,
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        // 创建初始文本块
        val initialBlock = NoteBlock(
            id = UUID.randomUUID().toString(),
            noteId = noteId,
            type = BlockType.TEXT,
            order = 0,
            text = ""
        )
        
        val fullNote = FullNote(note, listOf(initialBlock))
        saveFullNote(fullNote)
        return noteId
    }
    
    suspend fun addBlock(noteId: String, blockType: BlockType, order: Int, data: Map<String, Any?> = emptyMap()): String {
        val blockId = UUID.randomUUID().toString()
        val block = NoteBlock(
            id = blockId,
            noteId = noteId,
            type = blockType,
            order = order,
            text = data["text"] as? String,
            url = data["url"] as? String,
            alt = data["alt"] as? String,
            duration = data["duration"] as? Long,
            width = data["width"] as? Int,
            height = data["height"] as? Int
        )
        noteDao.insertBlock(block)
        
        // 更新记事的修改时间
        val note = getNoteById(noteId)
        if (note != null) {
            updateNote(note.copy(updatedAt = System.currentTimeMillis()))
        }
        
        return blockId
    }
    
    suspend fun updateBlock(blockId: String, data: Map<String, Any?>) {
        val blocks = noteDao.getBlocksByNoteId("") // 需要优化：直接获取block
        // 这里应该有根据blockId获取block的方法
        // 暂时简化处理
    }
    
    suspend fun deleteBlock(blockId: String) {
        noteDao.deleteBlock(blockId)
    }
    
    suspend fun deleteAllNotes() {
        noteDao.deleteAllNotes()
        noteDao.deleteAllBlocks()
    }
    
    suspend fun importNote(importNote: ExportImportUtils.ImportNote) {
        // 创建新的记事
        val note = Note(
            id = UUID.randomUUID().toString(), // 生成新的ID避免冲突
            title = importNote.title,
            createdAt = importNote.createdAt,
            updatedAt = System.currentTimeMillis(), // 更新为当前时间
            version = 1
        )
        
        noteDao.insertNote(note)
        
        // 导入记事块
        val blocks = mutableListOf<NoteBlock>()
        for (importBlock in importNote.blocks) {
            val block = when (importBlock.type) {
                BlockType.TEXT -> NoteBlock(
                    id = UUID.randomUUID().toString(),
                    noteId = note.id,
                    type = importBlock.type,
                    order = importBlock.order,
                    text = importBlock.text
                )
                BlockType.IMAGE -> {
                    var filePath: String? = null
                    
                    // 复制媒体文件到应用私有目录
                    if (importBlock.mediaFile?.exists() == true) {
                        val fileName = "imported_image_${System.currentTimeMillis()}_${importBlock.mediaFile.name}"
                        val targetFile = File(context.filesDir, "images/$fileName")
                        targetFile.parentFile?.mkdirs()
                        importBlock.mediaFile.copyTo(targetFile, overwrite = true)
                        filePath = targetFile.absolutePath
                    }
                    
                    NoteBlock(
                        id = UUID.randomUUID().toString(),
                        noteId = note.id,
                        type = importBlock.type,
                        order = importBlock.order,
                        url = filePath,
                        alt = importBlock.alt,
                        width = importBlock.width,
                        height = importBlock.height
                    )
                }
                BlockType.AUDIO -> {
                    var filePath: String? = null
                    
                    // 复制媒体文件到应用私有目录
                    if (importBlock.mediaFile?.exists() == true) {
                        val fileName = "imported_audio_${System.currentTimeMillis()}_${importBlock.mediaFile.name}"
                        val targetFile = File(context.filesDir, "audio/$fileName")
                        targetFile.parentFile?.mkdirs()
                        importBlock.mediaFile.copyTo(targetFile, overwrite = true)
                        filePath = targetFile.absolutePath
                    }
                    
                    NoteBlock(
                        id = UUID.randomUUID().toString(),
                        noteId = note.id,
                        type = importBlock.type,
                        order = importBlock.order,
                        url = filePath,
                        duration = importBlock.duration
                    )
                }
            }
            blocks.add(block)
        }
        
        // 批量插入块
        blocks.forEach { noteDao.insertBlock(it) }
    }
}