package com.dailynotes.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    
    @TypeConverter
    fun fromMediaItems(mediaItems: List<MediaItem>): String {
        return Gson().toJson(mediaItems)
    }

    @TypeConverter
    fun toMediaItems(mediaItemsString: String): List<MediaItem> {
        val listType = object : TypeToken<List<MediaItem>>() {}.type
        return Gson().fromJson(mediaItemsString, listType) ?: emptyList()
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