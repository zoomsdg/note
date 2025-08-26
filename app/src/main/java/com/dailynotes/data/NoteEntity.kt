package com.dailynotes.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "其他",
    val mediaItems: List<MediaItem> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)