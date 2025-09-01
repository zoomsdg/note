package com.dailynotes.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

// 重命名为NoteConverters，避免与NoteEntity中的冲突
class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromBlocks(blocks: List<ContentBlock>): String {
        return gson.toJson(blocks)
    }
    
    @TypeConverter
    fun toBlocks(json: String): List<ContentBlock> {
        return try {
            val type = object : TypeToken<List<ContentBlock>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    @TypeConverter
    fun fromMediaItems(mediaItems: List<MediaItem>): String {
        return gson.toJson(mediaItems)
    }

    @TypeConverter
    fun toMediaItems(mediaItemsString: String): List<MediaItem> {
        val listType = object : TypeToken<List<MediaItem>>() {}.type
        return gson.fromJson(mediaItemsString, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun toDate(timestamp: Long): Date {
        return Date(timestamp)
    }
}