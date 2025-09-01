package com.dailynotes.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

@Entity(tableName = "notes")
@TypeConverters(Converters::class)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String = "", // 保持向后兼容
    val blocks: List<ContentBlock> = emptyList(), // 新的块状结构
    val category: String = "其他",
    val mediaItems: List<MediaItem> = emptyList(), // 保持向后兼容
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

// 内容块数据结构
sealed class ContentBlock {
    abstract val id: String
    abstract val type: BlockType
    abstract val order: Int
    
    data class TextBlock(
        override val id: String = generateId(),
        override val order: Int,
        val text: String = ""
    ) : ContentBlock() {
        override val type: BlockType = BlockType.TEXT
    }
    
    data class ImageBlock(
        override val id: String = generateId(),
        override val order: Int,
        val url: String,
        val alt: String = "",
        val width: Int = 0,
        val height: Int = 0,
        val localPath: String = ""
    ) : ContentBlock() {
        override val type: BlockType = BlockType.IMAGE
    }
    
    data class AudioBlock(
        override val id: String = generateId(),
        override val order: Int,
        val url: String,
        val duration: Long = 0,
        val localPath: String = ""
    ) : ContentBlock() {
        override val type: BlockType = BlockType.AUDIO
    }
    
    companion object {
        fun generateId(): String = "block_${System.currentTimeMillis()}_${(0..1000).random()}"
    }
}

enum class BlockType {
    TEXT, IMAGE, AUDIO
}

